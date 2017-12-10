package models

import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.libs.json.Format
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Reads.StringReads
import java.time.LocalDateTime
import java.util.Date

trait J {
  val id: Long
  val user_id: Long
  val  description: String
  val required_effort: Double
  val  actual_effort: Double
  val  ideal_daily_effort: Double
  val  estimate_left_effort: Option[Double]
  val start_date: Timestamp = new Timestamp(0L)
  val  end_date: Timestamp = new Timestamp(0L)
  val priority: Int
}
/*
case class Jobs(id: Long, user_id: Long, description: String,
                required_effort: Double, actual_effort: Double, ideal_daily_effort: Double, estimate_left_effort: Option[Double],
                start_date: Timestamp = new Timestamp(0L), end_date: Timestamp = new Timestamp(0L), priority: Int)

*/
case class Jobs(id: Long, user_id: Long, description: String,
                required_effort: Double, actual_effort: Double, ideal_daily_effort: Double, estimate_left_effort: Option[Double],
                override val start_date: Timestamp = new Timestamp(0L), override val end_date: Timestamp = new Timestamp(0L), priority: Int) extends J;

case class JobsEnhance(override val id: Long, override val user_id: Long, override val description: String,
                override val required_effort: Double, override val actual_effort: Double, override val ideal_daily_effort: Double, override val estimate_left_effort: Option[Double],
                override val start_date: Timestamp = new Timestamp(0L), override val end_date: Timestamp = new Timestamp(0L), 
                override val priority: Int,var start_date_seconds:Long,var end_date_seconds:Long) extends J {

  def this(j:Jobs) {
     this(j.id ,j.user_id,j.description, j.required_effort, j.actual_effort, j.ideal_daily_effort, j.estimate_left_effort, j.start_date, j.end_date, j.priority,0,0)
  }
  start_date_seconds = start_date.getTime
  end_date_seconds = end_date.getTime


}
object JobsEnhance extends ((Long, Long, String, Double, Double, Double, Option[Double], Timestamp, Timestamp, Int,Long,Long) => JobsEnhance) {
  implicit object jobsTimestampFormat extends Format[Timestamp] {
    val timeformat = new SimpleDateFormat("yyyy-MM-dd")
    val printFormat = new SimpleDateFormat("yyyy-MM-dd")

    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(timeformat.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(printFormat.format(ts))
  }
  implicit val jobsEJsonReadWriteFormatTrait = Json.using[Json.WithDefaultValues].format[JobsEnhance]

}

object Jobs extends ((Long, Long, String, Double, Double, Double, Option[Double], Timestamp, Timestamp, Int) => Jobs) {

  implicit object jobsTimestampFormat extends Format[Timestamp] {
    val timeformat = new SimpleDateFormat("yyyy-MM-dd")
    val printFormat = new SimpleDateFormat("yyyy-MM-dd")

    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(timeformat.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(printFormat.format(ts))
  }

  implicit val jobsJsonReadWriteFormatTrait = Json.using[Json.WithDefaultValues].format[Jobs]

  def df = new SimpleDateFormat("yyyy-MM-dd");

  def toSqlTime(date: String) = {
    new Timestamp((df.parse(date)).getTime());
  }
  def toDate(sqlTimestamp: Timestamp) = df.format(sqlTimestamp)

  def applyit(description: String, required_effort: Double, actual_effort: Double, ideal_daily_effort: Double, estimate_left_effort: Option[Double],
              start_date: String, end_date: String, priority: Int) =
    Jobs(0, 0, description, required_effort, actual_effort, ideal_daily_effort, estimate_left_effort, toSqlTime(start_date), toSqlTime(end_date), priority)
  def unapplyit(j: Jobs): Option[(String, Double, Double, Double, Option[Double], String, String, Int)] =
    Some(j.description, j.required_effort, j.actual_effort, j.ideal_daily_effort, j.estimate_left_effort, toDate(j.start_date), toDate(j.end_date), j.priority)
}