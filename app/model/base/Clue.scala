package model.base

/**
  * A Clue definition that will be used, for example in the ml input to provide a semantic category outputs.
  *
  * @param question The question text that a player is prompted with.
  * @param answer The answer that the player provides.
  * @param category The category text that is provided by Jeopardy!
  * @param value The value of the clue on a scale of 1 to 5 and 0 which means unvalued (a wagerable question in FJ).
  * @param round The round of the clue, ranging from 1 to 3.
  */
case class Clue(question: String, answer: String, category: String, value: Int, round: Int)
