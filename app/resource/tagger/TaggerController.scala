package resource.tagger

import java.io.File
import java.nio.file.Paths
import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.tototoshi.csv.CSVReader
import javax.inject.Inject
import ml.spark.NaiveBayesTagger
import model.base.Tagger
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import task.TaskTracker

import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}

/**
  * Defines the behavior for the TaggerRef API endpoints, such as listing, retrieving, and creating.
  *
  * @param cc The controller components.
  * @param ec The execution context for this controller.
  */
class TaggerController @Inject()(taskTracker: TaskTracker, cc: TaggerControllerComponents)(
    implicit ec: ExecutionContext,
    actorSystem: ActorSystem
) extends TaggerBaseController(cc) {
  private val logger = Logger("jtagger")
  private implicit val mat = ActorMaterializer()

  /**
    * List all the available TaggerRefs on the store.
    *
    * @return All the TaggerRefs information.
    */
  def index: Action[AnyContent] = TaggerAction.async { implicit request =>
    logger.trace(s"TaggerRefController#index: ")

    resourceHandler.all.map { taggerRefs =>
      Ok(Json.toJson(taggerRefs))
    }
  }

  /**
    * Create a new Tagger using provided data.
    *
    * @return The info of the new created Tagger.
    */
  def process: Action[JsValue] = TaggerAction(parse.json) { implicit request =>
    logger.trace("TaggerController#process")

    val nameOpt = (request.body \ "name").asOpt[String]
    val dataPathOpt = (request.body \ "datapath").asOpt[String]

    val taggerResult = for {
      name <- nameOpt
      dataPath <- dataPathOpt
    } yield {
      val taggerPipeline = createTagger(name, dataPath)
      val creationTaskId = taskTracker.add(taggerPipeline)

      Ok(
        Json.obj(
          "success" -> true,
          "msg" -> s"$name tagger successfully created.",
          "task" -> creationTaskId
        )
      )
    }

    taggerResult.getOrElse {
      BadRequest(Json.obj("msg" -> "No `data` field in the request json."))
    }
  }

  /**
    * Logic to create a tagger given a specified name and training data.
    *
    * @param name The name to give the tagger.
    * @param dataPath The path to the data to train the tagger.
    * @return A future that resolves when the tagger has finished being created.
    */
  def createTagger(name: String, dataPath: String): Future[Unit] = {
    // Convert the data source of clue ids to labels, to a populated / hydrated dataset using the jnode service
    val reader = CSVReader.open(new File(dataPath))

    val populatedTraining = Source
      .fromIterator(() => reader.iteratorWithHeaders)
      .map(row => (row("id").toInt, row("semanticcategory")))
      .mapAsync(4) {
        case (clueId, label) =>
          jnode.clue(clueId).map((_, label))
      }
      .runWith(Sink.seq)
      .map(_.toMap)

    // Once the entire dataset is resolved with the jnode information, then map this information into a trained tagger.
    populatedTraining.flatMap { resolved =>
      val tagger = NaiveBayesTagger.create(resolved)
      val taggerPath = determineTaggerPath(name)
      NaiveBayesTagger.persist(tagger, taggerPath)
      logger.trace(s"Finished creating tagger $name")

      val insertTaggerAction = resourceHandler.insert(Tagger(name, name, LocalDate.now()))
      insertTaggerAction.map { _ => logger.trace(s"Inserted tagger $name") }
    }
  }

  /**
    * Provides the requested TaggerRef resource info corresponding to the id.
    *
    * @param id The id of the requested Tagger.
    * @return The appropriate resource view of the Tagger.
    */
  def show(id: Int): Action[AnyContent] = TaggerAction.async { implicit request =>
    logger.trace(s"TaggerRefController#show\\$id: ")

    resourceHandler.lookup(id).map { taggerRefResource =>
      Ok(Json.toJson(taggerRefResource))
    }
  }

  /**
    * Determines the path for a Tagger with a given name given the current configuration settings.
    *
    * @param name The name of the Tagger we are getting the path for.
    * @return The string representation of the Tagger's path.
    */
  private def determineTaggerPath(name: String): String = {
    val path = cc.config.get[String]("tagger.path")
    Paths.get(path, name).toAbsolutePath.toString
  }
}
