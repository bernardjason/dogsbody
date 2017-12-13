package controllers

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import javax.inject.Inject
import models.User
import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms.default
import play.api.data.Forms.longNumber
import play.api.data.Forms.of
import play.api.data.format.Formats._
import play.api.data.Forms.sqlTimestamp
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import play.api.mvc.Call
import models.Jobs

class Application @Inject() (implicit ec: ExecutionContext, components: ControllerComponents,
                             cache: SyncCacheApi, protected val dbConfigProvider: DatabaseConfigProvider)
  extends AbstractController(components) with tables.UserTable with HasDatabaseConfig[JdbcProfile]
  with play.api.i18n.I18nSupport {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import dbConfig.profile.api._

  val users = TableQuery[Users]

  val loginForm = Form(
    mapping(
      "user" -> text,
      "password" -> nonEmptyText,
      "nickname" -> nonEmptyText)(User.apply)(User.unpicknorole))

  val loginAdminForm = Form(
    mapping(
      "id" -> default(longNumber, 0L),
      "user" -> text.verifying("need username for add or update", { !_.isEmpty }),
      "password" -> text,
      "nickname" -> text.verifying("need nickname for add or update", { !_.isEmpty }),
      "role" -> optional(text))(User.apply)(User.unpick))

 

  val jobForm = Form(
    mapping(
      "description" -> text,
      "required_effort" -> of(doubleFormat),
      "actual_effort" -> of(doubleFormat),
      "ideal_daily_effort" -> of(doubleFormat),
      "estimate_left_effort" -> optional(of(doubleFormat)),
      "start_date" -> text,
      "end_date" -> text,
      "priority" -> of(intFormat) )(Jobs.applyit)(Jobs.unapplyit))
      
      

 
  def todoDebug = Action {
    println("*****************************************************************************")
    println("*    DEBUG TODO                                                            *")
    println("*****************************************************************************")
    val id = java.util.UUID.randomUUID().toString
    val u = User(1, "admin", "admin", "Dont call me Bernie", Some("admin"))
    cache.set(id, u)
    Redirect(routes.Application.todo()).withSession("user" -> id)
  }

  def todo = Action.async { implicit request =>
    getAuth.map { auth =>
      Future.successful(Ok(views.html.todo(loginForm, auth,jobForm)) )
    }.getOrElse( Future.successful(Ok(views.html.splash(loginForm, null,jobForm))) )
  }

  def getAllTheUsers = {
    val q = users.sortBy(f => f.user)
    db.run(q.result)
  }

  def notLoggedIn(implicit request: play.api.mvc.Request[play.api.mvc.AnyContent]) = {
    Logger.info(s"not logged in")
    Redirect(routes.Application.todo()).withNewSession
  }

  def getAuth(implicit request: play.api.mvc.Request[play.api.mvc.AnyContent]): Option[User] = {
    request.session.get("user").map { u =>
      Logger.info(s"session user is ${u}")
      return cache.get[User](u)
    }
    None
  }

  def useradmin = Action.async { implicit request =>
    getAuth.map { u =>
      getAllTheUsers.map { users =>
        Ok(views.html.admin(loginAdminForm, u, users.toList))
      }
    }.getOrElse { Future.successful(notLoggedIn) }
  }

  def javascript = Action { implicit request =>
    getAuth.map { u =>
      Ok(views.js.javascript(u))
    }.getOrElse {
      Ok(views.js.javascript(null))
    }
  }

  def index = Action {
    Redirect(routes.Application.todo())
  }

 
  def newuser = Action.async { implicit request =>

    loginAdminForm.bindFromRequest.fold(
      formWithErrors => {
        getAuth.map { u =>
          getAllTheUsers.map(users =>
            BadRequest(views.html.admin(formWithErrors, u, users.toList)))
        }.getOrElse {
          Future.successful(notLoggedIn)
        }
      },
      newuserData => {
        Logger.debug(request.body.asFormUrlEncoded.get("action").headOption.toString)

        val delete = request.body.asFormUrlEncoded.get("action").headOption match {
          case Some("delete") => true
          case _              => false
        }
        Logger.debug("DELETE " + delete)

        getAuth.map { u =>
          if (newuserData.id > 0) {
            if (delete == true) {
              Logger.info("Delete " + newuserData.id + " " + newuserData.user + " " + newuserData.nickname + " " + newuserData.role)
              val q = users.filter { u => u.id === newuserData.id }
              db.run((q.delete).asTry).map { handleDbResponse(_, routes.Application.useradmin()) }
            } else {
              Logger.info("Update " + newuserData.id + " " + newuserData.user + " " + newuserData.nickname + " " + newuserData.role)
              if (newuserData.password.length > 0) {
                val q = users.filter { u => u.id === newuserData.id }
                db.run((q.update(newuserData)).asTry).map { handleDbResponse(_, routes.Application.useradmin()) }
              } else {
                val q = users.filter(u => u.id === newuserData.id).map(x => (x.user, x.nickname, x.role))
                val u = (newuserData.user, newuserData.nickname, newuserData.role)
                db.run((q.update(u)).asTry).map { handleDbResponse(_, routes.Application.useradmin()) }
              }
            }
          } else {
            Logger.info("Insert " + newuserData)
            db.run((users += newuserData).asTry).map { handleDbResponse(_, routes.Application.useradmin()) }
          }
        }.getOrElse { Future.successful(Redirect(routes.Application.todo()).withNewSession) }

      })
  }

  def handleDbResponse(res: Try[Int], redirect: Call) = res match {
    case Success(res) => Redirect(redirect)
    case Failure(e) => {
      Logger.error(s"Problem on update " + res)
      val flasherr = s"Error " + res
      Redirect(redirect).flashing("error" -> flasherr)
    }
    case _ => { Redirect(redirect) }
  }

  val login = Action(parse.form(loginForm)).async { implicit request =>

    val loginData = request.body

    val q = users.filter { u => u.user === loginData.user && u.password === loginData.password }

    var id: String = null
    db.run(q.result).map { u =>
      u.foreach { user =>
        id = java.util.UUID.randomUUID().toString
        cache.set(id, user)
        Logger.info(s"login is [${id}] and db is ${user}")
      }
      if (id != null)
        Redirect(routes.Application.todo()).withSession("user" -> id)
      else
        Redirect(routes.Application.todo()).withNewSession
    }

  }

  val logout = Action {
    Redirect(routes.Application.todo()).withNewSession
  }
}