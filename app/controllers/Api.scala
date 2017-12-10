package controllers

import java.sql.Timestamp

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success

import javax.inject.Inject
import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import tables.JobsTable
import models.Jobs
import models.JobsEnhance

class Api @Inject() (implicit ec: ExecutionContext, components: ControllerComponents, cache: SyncCacheApi,
                     protected val dbConfigProvider: DatabaseConfigProvider, securedAction: SecuredAction)
  extends AbstractController(components)
  with JobsTable with HasDatabaseConfig[JdbcProfile] {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import dbConfig.profile.api._

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
        enhance.map( e => Logger.debug(e.toString()) )
        Ok(Json.toJson(enhance))
      }
    }
  }
  def postJob = securedAction.async(parse.json) { implicit request =>
  
    Logger.info("POST"+request)
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
  
    Logger.info("PUT"+request)
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
  
 

}