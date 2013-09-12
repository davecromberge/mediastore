package controllers

import app.ComponentRegistry
import app.SeedingActor
import app.Messages
import views._
import models._

import akka.actor.Actor
import akka.actor.Props
import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.data._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

object Seeding extends Controller {

   val seedingActor = Akka.system.actorOf(Props[SeedingActor], name = "seeder")

   def index = Action { _ =>
     seedingActor ! Messages.Seed
     Home.flashing("success" -> "Data has been reseeded")
   }
   val Home = Redirect(routes.Customers.index)
}
