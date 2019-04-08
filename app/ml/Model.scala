package ml

/**
  * Defines a Model interface to define general transformation logic from one domain to another.
  *
  * @tparam A The input type.
  * @tparam B The output type.
  */
trait Model[A, B] {
  /**
    * Use the model to compute some output from some input.
    *
    * @param obj The input object to for this model application.
    * @return The output from the model application to the input.
    */
  def apply(obj: A): B
}
