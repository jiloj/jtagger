package controller

import java.util.UUID

import com.google.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import task.TaskTracker

import scala.concurrent.ExecutionContext

/**
  * The TaskController surfaces information about long running task in the jtagger system.
  *
  * @param taskTracker The task tracker dependency that holds references to tasks in the jtagger system.
  * @param cc Dependency on common controller components.
  * @param ec Dependency on an execution context.
  */
class TaskController @Inject()(taskTracker: TaskTracker, cc: ControllerComponents)(
    implicit ec: ExecutionContext
) extends AbstractController(cc) {
  private val logger = Logger("jtagger")

  /**
    * Checks on the status of a task providing the tracking status and completion status which is valid if tracked.
    *
    * @param id The id of the task to check the status of.
    * @return The tracked status of a task with the given id, and the completion status of the task.
    */
  def show(id: String): Action[AnyContent] = Action { implicit request =>
    logger.info("TaskController#check")

    val uuid = UUID.fromString(id)
    val (tracked, completed) = taskTracker
      .completed(uuid)
      .map((true, _))
      .getOrElse((false, false))

    Ok(Json.obj("tracked" -> tracked, "completed" -> completed))
  }
}
