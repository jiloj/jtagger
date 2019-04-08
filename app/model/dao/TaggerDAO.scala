package model.dao

import java.sql.Date
import java.time.LocalDate

import javax.inject.Inject
import model.base.Tagger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

/**
  * A DAO to interface with the Tagger persistence layer.
  *
  * @param dbConfigProvider The provider for the database configuration.
  */
class TaggerDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with AllDAO[Tagger]
    with InsertableDAO[Tagger]
    with LookupableDAO[Tagger] {
  private val InsertTaggerQuery = Taggers returning Taggers.map(_.id) into
    ((tagger, id) => tagger.copy(id=id))

  /**
    * Provides all the Taggers in the persistence layer.
    *
    * @return A Future that resolves to all elements in the persistence layer.
    */
  def all: Future[Iterable[Tagger]] = {
    db.run(Taggers.result)
  }

  /**
    * Inserts a Tagger into the persistence layer.
    *
    * @param tr The Tagger to insert.
    * @return A Future that resolves to the reference of the inserted Tagger.
    */
  def insert(tr: Tagger): Future[Tagger] = {
    val query = InsertTaggerQuery += tr
    db.run(query)
  }

  /**
    * Lookup a Tagger in the persistence layer by its id.
    *
    * @param id The id to lookup the Tagger by.
    * @return A Future that resolves to the found Tagger if properly found.
    */
  def lookup(id: Int): Future[Option[Tagger]] = {
    val query = Taggers.filter(_.id === id).take(1).result.headOption
    db.run(query)
  }
}

/**
  * The schema for the Tagger in the persistence layer.
  */
class TaggerSchema(tag: Tag) extends Table[Tagger](tag, "tagger") {
  private implicit val localDateToSqlDate = MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l),
    d => d.toLocalDate
  )

  def name = column[String]("name", O.Unique, O.Length(100))
  def filepath = column[String]("filepath", O.Unique, O.Length(100))
  def created = column[LocalDate]("created")
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def * = (name, filepath, created, id) <> (Tagger.tupled, Tagger.unapply)
}

/**
  * A general query that returns the Tagger table.
  */
object Taggers extends TableQuery(new TaggerSchema(_))
