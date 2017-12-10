package models

import java.sql.Timestamp
import org.joda.time.DateTime
import play.api._
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.text.SimpleDateFormat

case class Tasks(id: Long, name: String, code: String)

object Tasks extends ((Long, String, String) => Tasks) {

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    val printFormat = new SimpleDateFormat("HH:mm:ss")

    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }

    def writes(ts: Timestamp) = JsString(printFormat.format(ts))
  }

  implicit val jsonReadWriteFormatTrait = Json.format[Tasks]
  
  
  def apply(name:String,code:String):Tasks = Tasks(0,name,code)
  def unapplyit(t: Tasks): Option[ (String,String)]  = Some(t.name,t.code)
  def unapplyitadmin(t: Tasks): Option[ (Long,String,String)]  = Some(t.id,t.name,t.code)
}
