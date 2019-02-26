package model.dao

import model.base.SemanticCategory
import model.store.SemanticCategoryStore

import scala.concurrent.Future


/**
  * The DAO to interface between the application and the data layer.
  */
class SemanticCategoryDAO extends AllDAO[SemanticCategory]
    with LookupableDAO[SemanticCategory] {
  /**
    * Retrieves all SemanticCategories in the database.
    *
    * @return A Future that resolves to the SemanticCategories in the db.
    */
  def all: Future[Iterable[SemanticCategory]] = {
    Future.successful(SemanticCategoryStore.categories)
  }

  /**
    * Lookup a SemanticCategory reference in the db by its id.
    *
    * @param id The id of the SemanticCategory to lookup.
    * @return A future that resolves to a SemanticCategory if found.
    */
  def lookup(id: Int): Future[Option[SemanticCategory]] = {
    Future.successful(SemanticCategoryStore.lookup(id))
  }

  /**
    * Find a semantic category in the data layer by its text.
    *
    * @param text The text of the desired SemanticCategory.
    * @return The SemanticCategory with the desired text if found.
    */
  def index(text: String): Future[Option[SemanticCategory]] = {
    Future.successful(SemanticCategoryStore.index(text))
  }
}
