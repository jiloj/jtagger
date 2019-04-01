package task

import java.util.UUID

import com.google.inject.Singleton

import scala.collection.mutable
import scala.concurrent.Future

/**
  * A helper class to track the status of a Future. This is useful if a future is used in a fire and forget manner, with
  * side effects. If the status is desired, then it can be added to a task tracker.
  */
@Singleton
class TaskTracker {
  private val futures: mutable.Map[UUID, Future[Any]] = mutable.Map.empty[UUID, Future[Any]]

  /**
    * Adds a task to be tracked.
    *
    * @param task The new task to track.
    * @return The tracking id of the new task.
    */
  def add(task: Future[Any]): UUID = {
    val id = UUID.randomUUID()
    futures(id) = task
    id
  }

  /**
    * Checks if a future with the given id is currently tracked.
    *
    * @param id The id of the task to check for.
    * @return True if the task is currently being tracked.
    */
  def tracked(id: UUID): Boolean = {
    futures.contains(id)
  }

  /**
    * Checks if a future with the given is completed, if it exists.
    *
    * @param id The id of the task to check for completion of.
    * @return An option which contains the completion status of the task if it exists.
    */
  def completed(id: UUID): Option[Boolean] = {
    futures.get(id).map(_.isCompleted)
  }
}
