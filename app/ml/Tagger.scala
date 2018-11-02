package ml

/**
  * Defines a Tagger interface to convert an input to an output.
  *
  * @tparam A The input type.
  * @tparam B The output type.
  */
trait Tagger[A, B] {
  /**
    * Tag or transform the input to the output.
    *
    * @param obj The input object to transform.
    * @return The output object that is the result of the transformation.
    */
  def tag(obj: A): B
}
