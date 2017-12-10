package tables

import java.sql.Timestamp

import models.User
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

trait UserTable {
  protected val driver: JdbcProfile
  import driver.api._
  class Users(tag: Tag) extends Table[User](tag, "USERS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def user = column[String]("USER")
    def password = column[String]("PASSWORD")
    def nickname = column[String]("NICKNAME")
    def role = column[Option[String]]("ROLE")

    def * = (id, user, password, nickname,role) <> (User.tupled, User.unapply _)
  }
}