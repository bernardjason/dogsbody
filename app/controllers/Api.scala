package controllers

import java.sql.Timestamp

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success

import javax.inject.Inject
import models.TimeEntry
import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import tables.TimeEntryTable
import tables.JobsTable
import models.Jobs
import models.JobsEnhance

class Api @Inject() (implicit ec: ExecutionContext, components: ControllerComponents, cache: SyncCacheApi,
                     protected val dbConfigProvider: DatabaseConfigProvider, securedAction: SecuredAction)
  extends AbstractController(components)
  with TimeEntryTable with JobsTable with HasDatabaseConfig[JdbcProfile] {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import dbConfig.profile.api._

  val timeentries = TableQuery[TimeEntries]
  val jobs = TableQuery[AJobs]

  
  
  def getJobs() = securedAction.async { implicit request =>

    val userid = request.user.id
    Logger.info(s"get jobs for user id = ${userid}")

    val myjobs = for {
        job <- jobs.sortBy { x => x.start_date.desc } if job.user_id === (request.user.id)
      } yield (job)

    db.run(myjobs.result).map { res =>
      {
        val enhance = for( j <- res ) yield new JobsEnhance(j)
        Ok(Json.toJson(enhance))
      }
    }
  }
  def postJob = securedAction.async(parse.json) { implicit request =>
  
    println("POST",request)
    val job = request.body.as[Jobs]

    val j = Jobs(0, request.user.id, job.description,
      job.required_effort,job.actual_effort,job.ideal_daily_effort,job.estimate_left_effort,
      job.start_date,job.end_date,job.priority)
    
    db.run((jobs += j).asTry).map(res =>
      res match {
        case Success(res) => Ok(Json.toJson(j))
        case Failure(e) => {
          Logger.error(s"Problem on insert, ${e.getMessage}")
          InternalServerError(s"Problem on insert, ${e.getMessage}")
        }
      })
  }
  def putJob = securedAction.async(parse.json) { implicit request =>
  
    println("PUT",request)
    val job = request.body.as[Jobs]
    val j = Jobs(job.id, request.user.id, job.description,
      job.required_effort,job.actual_effort,job.ideal_daily_effort,job.estimate_left_effort,
      job.start_date,job.end_date,job.priority)

     val toUpdate = for {
      jobRow <- jobs.filter { r => r.id === job.id }
    } yield (jobRow)
    db.run(toUpdate.update(j).asTry).map(res =>
      res match {
        case Success(res) => Ok(Json.toJson(j))
        case Failure(e) => {
          Logger.error(s"Problem on insert, ${e.getMessage}")
          InternalServerError(s"Problem on insert, ${e.getMessage}")
        }
      })
  }

  def deleteJobs(id: Long) = securedAction.async { implicit request =>

    val toDelete = for {
      jobs <- jobs.filter { j => j.id === id }
    } yield (jobs)
    db.run(toDelete.delete).map(res =>
      {
        Ok(Json.toJson(res))
      })

  } 
  
  
  
  
  
  
  
  
  
  
  // ****************************************************************************************
  
  def getTimeEntries(week: Option[String]) = securedAction.async { implicit request =>

    val userid = request.user.id
    Logger.info(s"get time entries for user id = ${userid}")

    val mytasks = if (week.isEmpty) {
      for {
        timeentry <- timeentries.sortBy { x => x.when.desc } if timeentry.user_id === (request.user.id)
      } yield (timeentry)
    } else {
      val start = new Timestamp(week.get.toLong)
      val end = new Timestamp(week.get.toLong + (60 * 60 * 24 * 7 * 1000))

      Logger.info(s"entries for ${userid} Start ${start} end ${end}")

      for {
        timeentry <- timeentries.withFilter(w => w.when >= start && w.when < end).
          sortBy { x => x.when.desc } if timeentry.user_id === (request.user.id)
      } yield (timeentry)
    }

    db.run(mytasks.result).map { res =>
      {
        Ok(Json.toJson(res))
      }
    }
  }
  def postTimeEntry = securedAction.async(parse.json) { implicit request =>

    val timeEntry = request.body.as[TimeEntry]

    val t = TimeEntry(0, request.user.id, request.user.nickname,
      timeEntry.when,
      timeEntry.task_id, timeEntry.task, timeEntry.effort)

    db.run((timeentries += t).asTry).map(res =>
      res match {
        case Success(res) => Ok(Json.toJson(t))
        case Failure(e) => {
          Logger.error(s"Problem on insert, ${e.getMessage}")
          InternalServerError(s"Problem on insert, ${e.getMessage}")
        }
      })
  }

  def deleteTimeEntry(id: Long) = securedAction.async { implicit request =>

    val toDelete = for {
      timeentry <- timeentries.filter { t => t.id === id }
    } yield (timeentry)
    db.run(toDelete.delete).map(res =>
      {
        Ok(Json.toJson(res))
      })

  }

}