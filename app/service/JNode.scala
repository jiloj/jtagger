package service

import javax.inject.Inject
import model.base.Clue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class JNode @Inject()(ws: WSClient) (implicit ec: ExecutionContext) {
  def clue(id: Int): Future[Clue] = {
    for {
      clueResponse <- ws.url(JNode.clueURL(id)).get

      categoryId = (clueResponse.json \ "categoryid").as[Int]
      categoryResponse <- ws.url(JNode.categoryURL(categoryId)).get
    } yield {
      val clueJson = clueResponse.json
      val question = (clueJson \ "question").as[String]
      val answer = (clueJson \ "answer").as[String]
      val value = (clueJson \ "value").as[Int]
      val round = (clueJson \ "round").as[Int]

      val category = (categoryResponse.json \ "text").as[String]
      Clue(question, answer, category, value, round)
    }
  }
}

/**
  *
  */
object JNode {
  /**
    *
    * @param id
    * @return
    */
  private def clueURL(id: Int): String = {
    s"http://localhost:9000/clues/$id"
  }

  /**
    *
    * @param id
    * @return
    */
  private def categoryURL(id: Int): String = {
    s"http://localhost:9000/categories/$id"
  }
}
