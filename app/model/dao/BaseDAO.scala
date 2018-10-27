package model.dao

import scala.concurrent.Future

/**
  * The iterable requirement for a persistence layer type.
  *
  * @tparam A The model type to interface with.
  */
trait AllDAO[A] {
  /**
    * Provides all elements in the persistence layer.
    *
    * @return A Future that resolves to all elements in the persistence layer.
    */
  def all: Future[Iterable[A]]
}

/**
  * The insertable requirement for a persistence layer type.
  *
  * @tparam A The model to type to interface with.
  */
trait InsertableDAO[A] {
  /**
    * Inserts a given type in the persistence layer.
    *
    * @param obj The object to insert item the persistence layer.
    * @return A Future that resolves to the inserted reference of the object.
    */
  def insert(obj: A): Future[A]
}

/**
  * The lookupable requirement for a persistence layer type.
  *
  * @tparam A The model type to interface with.
  */
trait LookupableDAO[A] {
  /**
    * Lookup a given item in the persistence layer by its id.
    *
    * @param id The id to lookup the item by.
    * @return A Future that resolves to the found item if properly found.
    */
  def lookup(id: Int): Future[Option[A]]
}
