package service

import javax.inject.Inject
import model.base.Clue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * A class for interfacing with the JNode service by retrieving desired resources into this problem domain.
  *
  * @param ws The ws client from the play framework to make requests with.
  * @param ec The execution context to run the service requests on.
  */
class JNode @Inject()(ws: WSClient) (implicit ec: ExecutionContext) {
  def clue(id: Int): Future[Clue] = {
    for {
      clueResponse <- ws.url(JNode.clueURL(id)).get
    } yield {
      val clueJson = clueResponse.json
      val question = (clueJson \ "question").as[String]
      val answer = (clueJson \ "answer").as[String]
      val category = (clueResponse.json \ "category").as[String]
      val value = (clueJson \ "value").as[Int]
      val round = (clueJson \ "round").as[Int]

      Clue(question, answer, category, value, round)
    }
  }
}

/**
  * The object instance for the Jnode service.
  */
object JNode {
  /**
    * Returns the resource URL for a clue with a given id.
    *
    * @param id The id of the clue to get the resource url of.
    * @return The resource url for the desired clue.
    */
  private def clueURL(id: Int): String = {
    s"http://localhost:9000/clues/$id"
  }
}
