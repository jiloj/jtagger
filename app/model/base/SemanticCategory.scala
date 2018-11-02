package model.base

/**
  * A semantic category. This is not a category by jeopardy standards, but rather a general category under which many
  * jeopardy categories can be pulled from.
  *
  * @param text The text of the category, such as Science, or Art.
  * @param id The id of the category. This by default is 0, and is set on insert.
  */
case class SemanticCategory(text: String, id: Int = 0)
