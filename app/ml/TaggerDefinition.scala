package ml

import scala.collection.Map

/**
  * Defines the Tagger interface beyond simply tagging, such as marshalling, training, and other domain problems.
  *
  * @tparam A The Tagger type to output.
  * @tparam B The input type to the tagger.
  */
trait TaggerDefinition[A <: Tagger[B], B] {
  /**
    * Creates a new tagger given the provided training data.
    *
    * @param train The map from input to category id to serve as training data.
    * @return The created Tagger from the provided training data.
    */
  def create(train: Map[B, Int]): A

  /**
    * Marshal a given Tagger to a file.
    *
    * @param obj The Tagger to marshall.
    * @param filename The file to marshall the tagger into.
    */
  def persist(obj: A, filename: String)

  /**
    * Loads a Tagger from a file.
    *
    * @param filename The filename to load from.
    * @return The loaded Tagger.
    */
  def load(filename: String): A
}
