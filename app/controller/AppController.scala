package controller

import javax.inject.Inject
import model.base.SemanticCategory
import model.dao.{AppDAO, SemanticCategoryDAO}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  *  The main AppController to interface with the different taggers.
  */
class AppController @Inject()(semcatDAO: SemanticCategoryDAO, appDAO: AppDAO, cc: ControllerComponents)
                             (implicit ec: ExecutionContext) extends AbstractController(cc) {
  private val logger = Logger("jnode")

  /**
    * Tags a given question.
    *
    * @return The tagged category result.
    */
  def tag: Action[AnyContent] = Action { implicit request =>
    Ok("Matias")
  }

  /**
    *
    * @return
    */
  def create: Action[AnyContent] = Action.async { implicit request =>
    logger.trace("AppcController#create")

    appDAO.create().map { _ =>
      Ok(Json.obj("success" -> true, "msg" -> "jtagger schema successfully created"))
    }
  }

  /**
    * Seed the initial app with the appropriate semantic categories.
    *
    * @return A successful result message when the operation is complete.
    */
  def seed: Action[AnyContent] = Action.async { implicit request =>
    val categoryTexts = List(
      "HISTORY",
      "SPORTS",
      "GEOGRAPHY",
      "BUSINESS",
      "CULTURE",
      "SCIENCE",
      "POLITICS",
      "RELIGION",
      "WORDS",
      "MUSIC",
      "ART",
      "FOOD",
      "OPERA",
      "LITERATURE",
      "TV/FILM"
    )

    val inserts = categoryTexts.map { text =>
      val sc = SemanticCategory(text)
      semcatDAO.insert(sc)
    }

    Future.sequence(inserts).map { _ =>
      Ok(Json.obj("success" -> true, "msg" -> "jtagger seeding successful."))
    }
  }
}
