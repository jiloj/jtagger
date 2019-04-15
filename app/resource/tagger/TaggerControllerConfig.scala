package resource.tagger

import javax.inject.Inject
import model.base.Tagger
import model.dao.TaggerDAO
import net.logstash.logback.marker.LogstashMarker
import play.api.{Configuration, Logger, MarkerContext}
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import resource.WritableResourceHandler
import service.JNode

import scala.concurrent.{ExecutionContext, Future}

/**
  * A wrapped request for TaggerRef resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait TaggerRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class TaggerRequest[A](request: Request[A], val messagesApi: MessagesApi)
    extends WrappedRequest(request)
    with TaggerRequestHeader

/**
  * Provides an implicit marker that will show the request in all logger statements.
  */
trait RequestMarkerContext {
  import net.logstash.logback.marker.Markers

  private def marker(tuple: (String, Any)) = Markers.append(tuple._1, tuple._2)

  private implicit class RichLogstashMarker(marker1: LogstashMarker) {
    def &&(marker2: LogstashMarker): LogstashMarker = marker1.and(marker2)
  }

  implicit def requestHeaderToMarkerContext(implicit request: RequestHeader): MarkerContext = {
    MarkerContext {
      marker("id" -> request.id) && marker("host" -> request.host) && marker(
        "remoteAddress" -> request.remoteAddress
      )
    }
  }

}

/**
  * The action builder for the Post resource.
  *
  * This is the place to put logging, metrics, to augment
  * the request with contextual data, and manipulate the
  * result.
  */
class TaggerActionBuilder @Inject()(messagesApi: MessagesApi, playBodyParsers: PlayBodyParsers)(
    implicit val executionContext: ExecutionContext
) extends ActionBuilder[TaggerRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {
  private val logger = Logger("jtagger")

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type TaggerRequestBlock[A] = TaggerRequest[A] => Future[Result]

  override def invokeBlock[A](request: Request[A], block: TaggerRequestBlock[A]): Future[Result] = {
    // Convert to marker context and use request in block
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(request)
    logger.trace(s"TaggerRefActionBuilder#invokeBlock: ")

    val future = block(new TaggerRequest(request, messagesApi))

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }
}

/**
  * Packages up the component dependencies for the post controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class TaggerControllerComponents @Inject()(
    taggerActionBuilder: TaggerActionBuilder,
    taggerRefDAO: TaggerDAO,
    jnode: JNode,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: scala.concurrent.ExecutionContext,
    config: Configuration
) extends ControllerComponents

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class TaggerBaseController @Inject()(tcc: TaggerControllerComponents)(implicit ec: ExecutionContext)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = tcc

  def TaggerAction: TaggerActionBuilder = tcc.taggerActionBuilder
  def jnode: JNode = tcc.jnode

  def resourceHandler: WritableResourceHandler[Tagger, TaggerResource] =
    new WritableResourceHandler[Tagger, TaggerResource](tcc.taggerRefDAO, createTaggerResource)

  /**
    * Creates a resource object from the raw database object.
    *
    * @param tr The tagger to create a resource reference to.
    * @return The resource object which is what is provided to clients rather than the raw database objects.
    */
  def createTaggerResource(tr: Tagger): TaggerResource = {
    TaggerResource(tr.id, tr.filepath, tr.created)
  }
}
