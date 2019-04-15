package model.dao

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

/**
  * Provides access to the persistence layer at an application level.
  *
  * @param dbConfigProvider Dependency on slick play.
  */
@Singleton
class AppDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  /**
    * Creation of the app persistency layer structure that resolves once complete.
    *
    * @return A task that resolves once the persistence layer has been created.
    */
  def create(): Future[Unit] = {
    val schemaCreation = DBIO.seq(
      Taggers.schema.create
    )

    db.run(schemaCreation)
  }
}
