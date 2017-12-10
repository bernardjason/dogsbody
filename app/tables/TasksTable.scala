package tables

import java.sql.Timestamp

import models.Tasks
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

trait TasksTable {
  protected val driver: JdbcProfile
  import driver.api._
  class ATask(tag: Tag) extends Table[Tasks](tag, "TASKS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def code = column[String]("CODE")

    def * = (id, name, code) <> (Tasks.tupled, Tasks.unapply)
  }
}