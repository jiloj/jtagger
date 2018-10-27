package controller

import javax.inject.Inject

import play.api.mvc._

/**
  *  The main AppController to interface with the different taggers.
  */
class AppController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  /**
    * Tags a given question.
    *
    * @return The tagged category result.
    */
  def tag = Action { implicit request =>
    Ok("Matias")
  }
}
