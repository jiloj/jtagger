package model.base

import java.time.LocalDate

/**
  * A tagger reference object in memory. This points to where the tagger is persisted.
  *
  * @param filepath The filepath location where the tagger is persisted in memory.
  * @param created The timestamp for the creation of this tagger .
  * @param id The id for this tagger.
  */
case class TaggerRef(filepath: String, created: LocalDate, id: Int)
