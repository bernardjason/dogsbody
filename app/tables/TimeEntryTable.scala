package tables

import java.sql.Timestamp

import models.TimeEntry
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

trait TimeEntryTable {
  protected val driver: JdbcProfile
  import driver.api._
  class TimeEntries(tag: Tag) extends Table[TimeEntry](tag, "TIME_ENTRY") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def user_id = column[Long]("USER_ID")
    def user = column[String]("USER")
    def when = column[Timestamp]("WHEN")
    def task_id = column[Int]("TASK_ID")
    def task = column[String]("TASK")
    def effort = column[Double]("EFFORT")
    def * = (id, user_id, user, when, task_id, task, effort) <> (TimeEntry.tupled, TimeEntry.unapply _)
  }
}