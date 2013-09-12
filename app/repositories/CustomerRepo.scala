package repositories

import models._
import models.JsonFormats._
import app.ComponentRegistry
import core.RecordMatcher

import play.api._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api._ 
import reactivemongo.core.commands.LastError
import scala.concurrent.Future

/*
  Makes use of reactive mongo plugin to return future results.
  
  Futures provide a nice way to reason about performing many operations in parallel in an efficient and non-blocking way
  http://docs.scala-lang.org/overviews/core/futures.html
*/

trait CustomerComponent {
  class CustomerRepo extends MongoRepository[Customer] 
    with Repository[Customer] 
    with ComponentRegistry {

    override val collection = db.collection[JSONCollection]("customers")

    def all: Future[List[Customer]] = {
      collection.find(Json.obj())
                .sort(Json.obj("name" -> 1))
                .cursor[Customer]
                .toList
    }

    def get(id: String): Future[Option[Customer]] = {
      require(!id.isEmpty)
      collection.find(Json.obj("id" -> id))
                .one[Customer]
    }

    def delete(id: String): Future[LastError] = {
      require(!id.isEmpty)
      collection.remove(Json.obj("id" -> id))
    }

    def update(customer: Customer): Future[LastError] = {
      require(!customer.id.isEmpty)
      require(!customer.name.isEmpty)

      doMatching(customer).flatMap { bestMatch =>
        collection.update(Json.obj("id" -> customer.id), customer.copy(pack = Some(bestMatch.name)))
      }
    }

    def insert(customer: Customer): Future[LastError] = {
      require(!customer.name.isEmpty)
      collection.insert(customer.copy(id = nextId))
    }

    private def doMatching(customer: Customer): Future[Package] = {
      val packages = packageRepo.all

      packages.flatMap { packages =>
        new RecordMatcher(customer, packages).findBestMatch
      }
    }
  }
}
