package resource.tagger

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes the different url requests to the controller actions. The available actions are to get tagger information,
  * create a tagger, and list all tagger information.
  */
class TaggerRouter @Inject()(controller: TaggerController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case GET(p"/${int(id)}") =>
      controller.show(id)
  }

}
