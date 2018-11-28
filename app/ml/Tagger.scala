package ml

/**
  * Defines a Tagger interface to convert an input to an output.
  *
  * @tparam A The input type.
  */
trait Tagger[A] {
  /**
    * Tag or transform the input to the output.
    *
    * @param obj The input object to transform.
    * @return The id of the output category.
    */
  def tag(obj: A): Int
}
