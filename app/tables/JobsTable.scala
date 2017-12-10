package tables

import java.sql.Timestamp
import models.Jobs
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

trait JobsTable {
  protected val driver: JdbcProfile
  import driver.api._
  class AJobs(tag: Tag) extends Table[Jobs](tag, "JOBS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def user_id = column[Long]("USER_ID")
    def description = column[String]("DESCRIPTION")

    def required_effort = column[Double]("REQUIRED_EFFORT")
    def actual_effort = column[Double]("ACTUAL_EFFORT")
    def ideal_daily_effort = column[Double]("IDEAL_DAILY_EFFORT")
    def estimate_left_effort = column[Option[Double]]("ESTIMATE_LEFT_EFFORT")

    def start_date = column[Timestamp]("START_DATE")
    def end_date = column[Timestamp]("END_DATE")
    def priority = column[Int]("PRIORITY")
    def * = (id, user_id, description, 
        required_effort,actual_effort,ideal_daily_effort,estimate_left_effort,
        start_date,end_date,priority) <> (Jobs.tupled, Jobs.unapply _)
  }
}