package controller

import java.nio.file.Paths

import javax.inject.Inject
import ml.spark.NaiveBayesTagger
import model.base.Clue
import model.dao.AppDAO
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  *  The main AppController to interface with the different taggers.
  */
class AppController @Inject()(appDAO: AppDAO, config: Configuration, cc: ControllerComponents)(
    implicit ec: ExecutionContext
) extends AbstractController(cc) {
  private val logger = Logger("jtagger")

  def f(a: Int) = 10

  /**
    * Tags a given question.
    *
    * @return The tagged category result.
    */
  def tag: Action[JsValue] = Action(parse.json) { implicit request =>
    logger.info("AppController#tag")

    val taggerNameOpt = (request.body \ "tagger").asOpt[String]

    val j = (request.body \ "clue").as[JsValue]

    val categoryOpt = (j \ "category").asOpt[String]
    val questionOpt = (j \ "question").asOpt[String]
    val answerOpt = (j \ "answer").asOpt[String]
    val roundOpt = (j \ "round").asOpt[Int]
    val valueOpt = (j \ "value").asOpt[Int]

    val clueOpt = for {
      c <- categoryOpt
      q <- questionOpt
      a <- answerOpt
      r <- roundOpt
      v <- valueOpt
    } yield {
      Clue(q, a, c, v, r)
    }

    val tagResultOpt = for {
      taggerName <- taggerNameOpt
      clue <- clueOpt
    } yield {
      val taggerRoot = config.get[String]("tagger.path")
      val taggerPath = Paths.get(taggerRoot, taggerName).toString
      val tagger = NaiveBayesTagger.load(taggerPath)
      tagger(clue)
    }

    tagResultOpt
      .map { tagResult =>
        Ok(Json.obj("semanticcategory" -> tagResult))
      }
      .getOrElse {
        BadRequest(Json.obj("success" -> false, "msg" -> "There was an error in the request."))
      }
  }

  /**
    * Creates the schemas and databases for the given app.
    *
    * @return A Future which resolves to a successful json response.
    */
  def create: Action[AnyContent] = Action.async { implicit request =>
    logger.info("AppcController#create")

    appDAO.create().map { _ =>
      Ok(Json.obj("success" -> true, "msg" -> "jtagger schema successfully created"))
    }
  }
}
