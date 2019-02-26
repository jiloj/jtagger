package model.store

import model.base.SemanticCategory

/**
  * The data layer for SemanticCategories. As there are just a fixed number of semantic categories, and the api on top
  * of them is very simple, these will reside in memory to have simpler code. In the case where categories become more
  * complex, such as allowing for subcategories, this will need to be revisted.
  */
object SemanticCategoryStore {
  private val CategoryTexts = List(
    "history",
    "sports",
    "geography",
    "culture",
    "science",
    "politics",
    "religion",
    "words",
    "music",
    "art",
    "food",
    "opera",
    "literature",
    "tv/film",
    "theatre",
    "classics"
  )

  private val DataLayer = CategoryTexts.zipWithIndex.map { t =>
    SemanticCategory(t._1, t._2)
  }

  private val IdToCategory = DataLayer.map { semcat =>
    semcat.id -> semcat
  }.toMap

  private val CategoryToId = DataLayer.map { semcat =>
    semcat.text -> semcat
  }.toMap

  /**
    * Provide all the categories in the store.
    *
    * @return All the categories in the data store.
    */
  def categories: Iterable[SemanticCategory] = DataLayer

  /**
    * Lookup the semantic category given the id.
    *
    * @param id The id of the category to lookup.
    * @return The semantic category associated with the given id, if it exists.
    */
  def lookup(id: Int): Option[SemanticCategory] = IdToCategory.get(id)

  /**
    * Search for the semantic category associated with its text.
    *
    * @param text The label of the semantic category.
    * @return The semantic category with the associated text, if it exists.
    */
  def index(text: String): Option[SemanticCategory] = CategoryToId.get(text)
}
