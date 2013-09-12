package repositories

import models._
import app.ComponentRegistry
import core.RecordMatcher

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api._ 
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import scala.concurrent.Future

/*
  Makes use of reactive mongo plugin to return future results.
  
  Futures provide a nice way to reason about performing many operations in parallel in an efficient and non-blocking way
  http://docs.scala-lang.org/overviews/core/futures.html
*/

trait CustomerComponent {
  class CustomerRepo extends 
    MongoRepository[Customer] with 
    Repository[Customer] with 
    ComponentRegistry {

    override val collection = db("customers")
    override implicit val reader = Customer.CustomerBSONReader
    override implicit val writer = Customer.CustomerBSONWriter

    def all: Future[List[Customer]] = {
      val query = BSONDocument()
      collection.find(query)
                .cursor[Customer]
                .toList
    }

    def get(id: String): Future[Option[Customer]] = {
      require(!id.isEmpty)
      collection.find(BSONDocument("_id" -> new BSONObjectID(id)))
                .one[Customer]
    }

    def delete(id: String): Future[LastError] = {
      require(!id.isEmpty)
      collection.remove(BSONDocument("_id" -> new BSONObjectID(id)))
    }

    def update(id: String, customer: Customer): Future[LastError] = {
      require(!id.isEmpty)
      require(!customer.name.isEmpty)

      doMatching(customer).flatMap { bestMatch =>
         val modifier = BSONDocument(
            "$set" -> BSONDocument(
              "name" -> BSONString(customer.name),
              "pack" -> BSONString(bestMatch.name)))
         collection.update(BSONDocument("_id" -> new BSONObjectID(id)), modifier)
      }
    }

    def insert(customer: Customer): Future[LastError] = {
      require(!customer.name.isEmpty)
      collection.insert(customer)
    }

    private def doMatching(customer: Customer): Future[Package] = {
      val packages = packageRepo.all

      packages.flatMap { packages =>
        new RecordMatcher(customer, packages).findBestMatch
      }
    }
  }
}
