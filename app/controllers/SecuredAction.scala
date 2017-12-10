package controllers

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import javax.inject.{ Inject, Singleton }

import models.User
import play.api.Logger
import play.api.cache._
import play.api.mvc._
import play.api.mvc.Request
import play.api.mvc.WrappedRequest
import play.mvc.Http.Status

class AuthenticatedRequest[A](val user: User, val request: Request[A])
  extends WrappedRequest[A](request)

@Singleton
class SecuredAction @Inject() (cache: SyncCacheApi, playBodyParsers: PlayBodyParsers)(implicit val executionContext: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] { //ControllerComponents] {

  override def parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {

    request.session.get("user").map { u =>

      import play.api.Play.current

      val user = cache.get(u).asInstanceOf[Option[User]]

      if (user.isEmpty || user.get.id < 0) {
        Logger.info(s"not logged in, action ${request.method} ${request.uri}")

        Future.successful(Results.Status(Status.UNAUTHORIZED))
      } else {
        Logger.info(s"Calling action ${request.method} ${request.uri}")
        block(new AuthenticatedRequest(user.get, request))
      }
    }.getOrElse {
      Logger.info(s"not logged on, action ${request.method} ${request.uri}")

      Future.successful(Results.Status(Status.UNAUTHORIZED))
    }
  }
}