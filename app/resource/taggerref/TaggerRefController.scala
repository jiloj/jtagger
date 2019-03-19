package resource.taggerref

import java.nio.file.Paths
import java.time.LocalDate

import com.typesafe.config.Config
import javax.inject.Inject
import ml.spark.NaiveBayesTagger
import model.base.{Clue, TaggerRef}
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import util.FutureUtil

import scala.concurrent.{ExecutionContext, Future}

/**
  * Defines the behavior for the TaggerRef API endpoints, such as listing, retrieving, and creating.
  *
  * @param cc The controller components.
  * @param ec The execution context for this controller.
  */
class TaggerRefController @Inject()(cc: TaggerRefControllerComponents)(implicit ec: ExecutionContext)
    extends TaggerRefBaseController(cc) {
  private val logger = Logger("jtagger")

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
  def process: Action[JsValue] = TaggerRefAction.async(parse.json) { implicit request =>
    logger.trace("TaggerRefController#process")

    val semanticCatTextToIdFuture = semanticCatDAO.all.map { semcats =>
      semcats.map { semcat =>
        semcat.text -> semcat.id
      }.toMap
    }

    val nameOpt = (request.body \ "name").asOpt[String]
    val dataOpt = (request.body \ "data").asOpt[JsObject]

    semanticCatTextToIdFuture.flatMap { semanticCatsTextToId =>
      val taggerResult = for {
        data <- dataOpt
        name <- nameOpt
      } yield {
        val training = data.value.map { kvp =>
          val clueId = kvp._1.toInt
          val semcatLabel = kvp._2.as[String]

          (jnode.clue(clueId), semanticCatsTextToId(semcatLabel))
        }

        FutureUtil.mappingKey[Clue, Int](training).map { resolved =>
          val tagger = NaiveBayesTagger.create(resolved)
          val taggerPath = determineTaggerPath(name)
          NaiveBayesTagger.persist(tagger, taggerPath)
          resourceHandler.insert(TaggerRef(name, LocalDate.now()))

          Ok(Json.obj("success" -> true, "msg" -> s"$name tagger successfully created."))
        }
      }

      taggerResult.getOrElse {
        Future.successful {
          BadRequest(Json.obj("msg" -> "No `data` field in the request json."))
        }
      }
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
    val path = cc.config.get[Configuration]("tagger").get[String]("path")
    Paths.get(path, name).toAbsolutePath.toString
  }
}
