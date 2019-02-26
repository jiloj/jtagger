package model.dao

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

/**
  *
  * @param dbConfigProvider
  */
@Singleton
class AppDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  /**
    *
    * @return
    */
  def create(): Future[Unit] = {
    val schemaCreation = DBIO.seq(
      Taggers.schema.create
    )

    db.run(schemaCreation)
  }
}
