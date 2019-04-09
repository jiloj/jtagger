package ml

import scala.collection.Map

/**
  * Defines the Model interface beyond simply application, such as marshalling, training, and other domain problems.
  *
  * @tparam A The Model type to define
  * @tparam B The input type to the model.
  * @tparam C The output type of the model.
  */
trait ModelDefinition[A <: Model[B, C], B, C] {

  /**
    * Creates a new model given the provided training data.
    *
    * @param train The map from input to output to serve as training data.
    * @return The created Model from the provided training data.
    */
  def create(train: Map[B, C]): A

  /**
    * Marshal a given Model to a file.
    *
    * @param obj The Model to marshall.
    * @param filename The file to marshall the model into.
    */
  def persist(obj: A, filename: String)

  /**
    * Loads a Model from a file.
    *
    * @param filename The filename to load from.
    * @return The loaded Model.
    */
  def load(filename: String): A
}
