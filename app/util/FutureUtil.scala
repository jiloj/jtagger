package util

import scala.collection.Map
import scala.concurrent.{ExecutionContext, Future}

/**
  * A set of utilities for transforming futures.
  */
object FutureUtil {
  /**
    * Utility to transform a map with future keys to a future that resolves to a map.
    *
    * @param map The map to transform.
    * @param ec The execution context to perform the operation under.
    * @tparam A The key type, not including the future.
    * @tparam B The value type.
    * @return The future that resolves to the map.
    */
  def mappingKey[A, B](map: Map[Future[A], B])(implicit ec: ExecutionContext): Future[Map[A, B]] = {
    Future.traverse(map) { case (f, v) =>
      f.map(_ -> v)
    }.map(_.toMap)
  }
}
