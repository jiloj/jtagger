package ml

/**
  * Defines a Tagger interface to label a given input to an output label.
  *
  * @tparam A The input type.
  */
trait Tagger[A] {
  /**
    * Tag the input to a corresponding output label.
    *
    * @param obj The input object to label.
    * @return The output category label.
    */
  def tag(obj: A): String
}
