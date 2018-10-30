package model.dao

import java.sql.Date
import java.time.LocalDate

import javax.inject.Inject
import model.base.TaggerRef
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
    with AllDAO[TaggerRef]
    with InsertableDAO[TaggerRef]
    with LookupableDAO[TaggerRef] {
  private val InsertTaggerQuery = Taggers returning Taggers.map(_.id) into
    ((tagger, id) => tagger.copy(id=id))

  /**
    * Provides all the Taggers in the persistence layer.
    *
    * @return A Future that resolves to all elements in the persistence layer.
    */
  def all: Future[Iterable[TaggerRef]] = {
    db.run(Taggers.result)
  }

  /**
    * Inserts a Tagger into the persistence layer.
    *
    * @param tr The Tagger to insert.
    * @return A Future that resolves to the inserted reference of the TaggerRef.
    */
  def insert(tr: TaggerRef): Future[TaggerRef] = {
    val query = InsertTaggerQuery += tr
    db.run(query)
  }

  /**
    * Lookup a TaggerRef in the persistence layer by its id.
    *
    * @param id The id to lookup the TaggerRef by.
    * @return A Future that resolves to the found TaggerRef if properly found.
    */
  def lookup(id: Int): Future[Option[TaggerRef]] = {
    val query = Taggers.filter(_.id === id).take(1).result.headOption
    db.run(query)
  }
}

/**
  * The schema for the Tagger in the persistence layer.
  */
class TaggerSchema(tag: Tag) extends Table[TaggerRef](tag, "tagger") {
  private implicit val localDateToSqlDate = MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l),
    d => d.toLocalDate
  )

  def filepath = column[String]("filepath", O.Unique, O.Length(100))
  def created = column[LocalDate]("created")
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def * = (filepath, created, id) <> (TaggerRef.tupled, TaggerRef.unapply)
}

/**
  * A general query that returns the Tagger table.
  */
object Taggers extends TableQuery(new TaggerSchema(_))
