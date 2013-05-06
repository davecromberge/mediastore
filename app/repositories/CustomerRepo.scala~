package repositories

import app._
import core._
import models._

import play.api._
import play.api.Play.current
import play.modules.reactivemongo._ 

import reactivemongo.api._ 
import reactivemongo.bson._
import reactivemongo.core.commands._
import reactivemongo.bson.handlers.DefaultBSONHandlers.{ DefaultBSONReaderHandler, DefaultBSONDocumentWriter }

import scala.concurrent._

/*
  Makes use of reactive mongo plugin to return future results.
  
  Futures provide a nice way to reason about performing many operations in parallel in an efficient and non-blocking way

  http://docs.scala-lang.org/overviews/core/futures.html

  The reactive mongo plugin also allows a cursor to be returned for consumers (reactive iteratees)
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
      collection.find(query).toList
    }

    def get(id: String): Future[Option[Customer]] = {
      require(!id.isEmpty)
      collection.find(BSONDocument("_id" -> new BSONObjectID(id)))
                .headOption
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
