package resource.taggerref

import javax.inject.Inject
import ml.spark.NaiveBayesTagger
import model.base.Clue
import org.apache.spark.ml.classification.NaiveBayes
import play.api.Logger
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

    (request.body \ "data").asOpt[JsObject].map { data =>
      val training = data.value.map { kvp =>
        val clueId = kvp._1.toInt
        val semcatLabel = kvp._2.as[String]

        (jnode.clue(clueId.toInt), semcatLabel)
      }

      FutureUtil.mappingKey[Clue, String](training).map { resolved =>
        val tagger = NaiveBayesTagger.create(resolved)
        Ok(Json.obj("matias" -> "grioni"))
      }
    }.getOrElse {
      Future.successful {
        BadRequest(Json.obj("msg" -> "No `data` field in the request json."))
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
}
