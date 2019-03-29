package resource.taggerref

import java.nio.file.Paths
import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import javax.inject.Inject
import ml.spark.NaiveBayesTagger
import model.base.{Clue, TaggerRef}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import util.FutureUtil

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

/**
  * Defines the behavior for the TaggerRef API endpoints, such as listing, retrieving, and creating.
  *
  * @param cc The controller components.
  * @param ec The execution context for this controller.
  */
class TaggerRefController @Inject()(cc: TaggerRefControllerComponents)(implicit ec: ExecutionContext, actorSystem: ActorSystem)
    extends TaggerRefBaseController(cc) {
  private val logger = Logger("jtagger")
  private implicit val mat = ActorMaterializer()

  /**
    * List all the available TaggerRefs on the store.
    *
    * @return All the TaggerRefs information.
    */
  def index: Action[AnyContent] = TaggerRefAction.async { implicit request =>
    logger.trace(s"TaggerRefController#index: ")

    resourceHandler.all.map { taggerRefs =>
      Ok(Json.toJson(taggerRefs))
    }
  }

  /**
    * Create a new TaggerRef using provided data.
    *
    * @return The info of the new created TaggerRef.
    */
  def process: Action[JsValue] = TaggerRefAction(parse.json) { implicit request =>
    logger.trace("TaggerRefController#process")

    val nameOpt = (request.body \ "name").asOpt[String]
    val dataOpt = (request.body \ "data").asOpt[JsObject]

    val taggerResult = for {
      name <- nameOpt
      data <- dataOpt
    } yield {
      createTagger(name, data)
      Ok(Json.obj("success" -> true, "msg" -> s"$name tagger successfully created."))
    }

    taggerResult.getOrElse {
      BadRequest(Json.obj("msg" -> "No `data` field in the request json."))
    }
  }

  /**
    * Logic to create a tagger given a specified name and training data.
    *
    * @param name The name to give the tagger.
    * @param data The data to train the tagger.
    */
  def createTagger(name: String, data: JsObject) {
    scheduleLongRunningTask {
      val training = Source
        .fromIterator(() => data.value.iterator)
        .map(tup => (tup._1.toInt, tup._2.as[String]))
        .mapAsync(4) { case (clueId, label) =>
          jnode.clue(clueId).map((_, label))
        }
        .runWith(Sink.seq)
        .map(_.toMap)

      val x = training.map { resolved =>
        println("Now in here")
        val tagger = NaiveBayesTagger.create(resolved)
        val taggerPath = determineTaggerPath(name)
        NaiveBayesTagger.persist(tagger, taggerPath)
        resourceHandler.insert(TaggerRef(name, LocalDate.now()))

        logger.trace(s"Finished creating tagger $name")
      }

      Await.ready(x, Duration.Inf)
    }
  }

  /**
    * Provides the requested TaggerRef resource info corresponding to the id.
    *
    * @param id The id of the requested Tagger.
    * @return The appropriate resource view of the Tagger.
    */
  def show(id: Int): Action[AnyContent] = TaggerRefAction.async { implicit request =>
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

  /**
    * Schedules a code block on a long running thread through the akka actor system.
    *
    * @param f The task to run in a long running thread.
    */
  private def scheduleLongRunningTask(f: => Unit) {
    actorSystem.scheduler.scheduleOnce(0 seconds)(f)
  }
}
