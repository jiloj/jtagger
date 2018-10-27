package resource.tagger

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  *
  * @param cc
  * @param ec
  */
class TaggerController @Inject()(cc: TaggerControllerComponents)(implicit ec: ExecutionContext)
    extends TaggerBaseController(cc) {
  private val logger = Logger("jtagger")

  /**
    *
    * @return
    */
  def index: Action[AnyContent] = TaggerAction.async { implicit request =>
    logger.trace(s"TaggerController#index: ")
    resourceHandler.all.map { taggers =>
      Ok(Json.toJson(taggers))
    }
  }

  /**
    * Create a new Tagger using provided data.
    *
    * @return The info of the new created Tagger.
    */
  def process: Action[JsValue] = TaggerAction(parse.json) { implicit request =>
    logger.trace("TaggerController#process")
    (request.body \ "data").asOpt[JsObject].map { data =>
      Ok(data.value)
      // TODO: Here is where I have to start the training.
    }.getOrElse {
      BadRequest("No `data` field in the request json.")
    }
  }

  /**
    * Provides the requested Tagger resource info corresponding to the id.
    *
    * @param id The id of the requested Tagger.
    * @return The appropriate resource view of the Tagger.
    */
  def show(id: Int): Action[AnyContent] = TaggerAction.async { implicit request =>
    logger.trace(s"TaggerController#show\\$id: ")
    resourceHandler.lookup(id).map { taggerRefResource =>
      Ok(Json.toJson(taggerRefResource))
    }
  }
}
