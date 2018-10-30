package resource.taggerref

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

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
  def process: Action[JsValue] = TaggerRefAction(parse.json) { implicit request =>
    logger.trace("TaggerRefController#process")

    (request.body \ "data").asOpt[JsObject].map { data =>
      Ok(data.value)
      // TODO: Here is where I have to start the training.
    }.getOrElse {
      BadRequest("No `data` field in the request json.")
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
