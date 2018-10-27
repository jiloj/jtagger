package model.dao

import javax.inject.Inject
import model.base.SemanticCategory
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future


/**
  * The DAO to interface between the application and the persistence layer (the db).
  *
  * @param dbConfigProvider The database config provider injection to automatically interface with the persistence
  *                         layer.
  */
class SemanticCategoryDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with AllDAO[SemanticCategory]
    with InsertableDAO[SemanticCategory]
    with LookupableDAO[SemanticCategory] {
  private val InsertSemanticCategoryQuery = (SemanticCategories returning SemanticCategories.map(_.id)
    into ((category, id) => category.copy(id=id)))

  /**
    * Retrieves all SemanticCategories in the database.
    *
    * @return A Future that resolves to the SemanticCategories in the db.
    */
  def all: Future[Iterable[SemanticCategory]] = {
    db.run(SemanticCategories.result)
  }

  /**
    * Inserts a SemanticCategory into the database. The id of the SemanticCategory will be ignored.
    *
    * @param sc The SemanticCategory to insert.
    * @return The inserted SemanticCategory reference.
    */
  def insert(sc: SemanticCategory): Future[SemanticCategory] = {
    val query = InsertSemanticCategoryQuery += sc
    db.run(query)
  }

  /**
    * Lookup a SemanticCategory reference in the db by its id.
    *
    * @param id The id of the SemanticCategory to lookup.
    * @return A future that resolves to a SemanticCategory if found.
    */
  def lookup(id: Int): Future[Option[SemanticCategory]] = {
    val query = SemanticCategories.filter(_.id === id).take(1).result.headOption
    db.run(query)
  }
}

/**
  * The schema for the SemanticCategory table.
  *
  * @param tag The needed tag for the table.
  */
class SemanticCategorySchema(tag: Tag) extends Table[SemanticCategory](tag, "semanticcategory") {
  def text = column[String]("text", O.Unique, O.Length(100))
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def * = (text, id) <> (SemanticCategory.tupled, SemanticCategory.unapply)
}

/**
  * A query that reference the general SemanticCategories table.
  */
object SemanticCategories extends TableQuery(new SemanticCategorySchema(_))
