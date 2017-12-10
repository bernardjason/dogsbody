package models

import java.sql.Timestamp
import org.joda.time.DateTime
import play.api._
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.text.SimpleDateFormat
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import java.util.Date

case class TimeEntry(id: Long, user_id: Long=0, user: String="", when: Timestamp = new Timestamp(0L), task_id: Int, task: String, effort: Double)

object TimeEntry extends ((Long, Long, String, Timestamp, Int, String, Double) => TimeEntry) {

  implicit object timestampFormat extends Format[Timestamp] {
    val timeformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    val printFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def reads(json: JsValue) = {
      val str = json.as[String]
      if ( str.contains("T") )  {
        JsSuccess(new Timestamp(timeformat.parse(str).getTime))
      } else {
        JsSuccess(new Timestamp(str.toLong) )
      }
    }
    def writes(ts: Timestamp) = JsString(printFormat.format(ts))
  }

  implicit val jsonReadWriteFormatTrait =  Json.using[Json.WithDefaultValues].format[TimeEntry]

}