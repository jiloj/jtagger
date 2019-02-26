package controller

import javax.inject.Inject
import ml.Tagger
import ml.spark.NaiveBayesTagger
import model.base.{Clue, SemanticCategory}
import model.dao.{AppDAO, SemanticCategoryDAO}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  *  The main AppController to interface with the different taggers.
  */
class AppController @Inject()(semcatDAO: SemanticCategoryDAO, appDAO: AppDAO, cc: ControllerComponents)
                             (implicit ec: ExecutionContext) extends AbstractController(cc) {
  private val logger = Logger("jnode")
  private val taggers = mutable.Map.empty[String, Tagger[Clue]]

  /**
    * Tags a given question.
    *
    * @return The tagged category result.
    */
  def tag: Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.trace("AppController#tag")
    val semanticCatsIdToTextFuture = semcatDAO.all.map { semcats =>
      semcats.map { semcat =>
        semcat.id -> semcat.text
      }.toMap
    }

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
      val tagger = if (taggers.contains(taggerName)) {
        taggers(taggerName)
      } else {
        val tagger = NaiveBayesTagger.load(taggerName)
        taggers += taggerName -> tagger
        tagger
      }

      tagger.tag(clue)
    }

    semanticCatsIdToTextFuture.map { semanticCatsIdToText =>
      tagResultOpt.map { tagResult =>
        val semcatText = semanticCatsIdToText(tagResult)
        Ok(Json.obj("semanticcategory" -> semcatText))
      }.getOrElse {
        BadRequest(Json.obj("success" -> false, "msg" -> "There was an error in the request."))
      }
    }
  }

  /**
    * Creates the schemas and databases for the given app.
    *
    * @return A Future which resolves to a successful json response.
    */
  def create: Action[AnyContent] = Action.async { implicit request =>
    logger.trace("AppcController#create")

    appDAO.create().map { _ =>
      Ok(Json.obj("success" -> true, "msg" -> "jtagger schema successfully created"))
    }
  }
}
