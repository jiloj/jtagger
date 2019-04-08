package model.base

import java.time.LocalDate

/**
  * A reference to a tagger that is persisted.
  *
  * @param name The name of the tagger.
  * @param filepath The filepath location where the tagger is persisted in memory.
  * @param created The timestamp for the creation of this tagger .
  * @param id The id for this tagger. When creating a new instance, this will be default 0.
  */
case class Tagger(name: String, filepath: String, created: LocalDate, id: Int = 0)
