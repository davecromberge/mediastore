package repositories

import models._

import play.api.Play.current
import play.modules.reactivemongo._ 

import reactivemongo.api._ 
import reactivemongo.bson._
import reactivemongo.core.commands._
import reactivemongo.bson.handlers.DefaultBSONHandlers.{ DefaultBSONReaderHandler, DefaultBSONDocumentWriter }

import scala.concurrent.{ExecutionContext, Future}

trait AttributeComponent {
  class AttributeRepo extends MongoRepository[Attribute] with Repository[Attribute] {

    override val collection = db("attributes")
    override implicit val reader = Attribute.AttributeBSONReader
    override implicit val writer = Attribute.AttributeBSONWriter

    def all: Future[List[Attribute]] = {
      collection.find(BSONDocument()).toList
    }

    def get(code: String): Future[Option[Attribute]] = {
      require(!code.isEmpty)
      collection.find(BSONDocument("code" -> new BSONString(code)))
                .headOption
    }

    def delete(code: String): Future[LastError] = {
      require(!code.isEmpty)
      collection.remove(BSONDocument("code" -> new BSONString(code)))
    }

    def deleteAll: Future[LastError] = {
      collection.remove(BSONDocument())
    }

    def update(code: String, attribute: Attribute): Future[LastError] = {
      require(!code.isEmpty)
      require(!attribute.name.isEmpty)

      val modifier = BSONDocument(
        "$set" -> BSONDocument(
          "code" -> BSONString(attribute.code),
          "name" -> BSONString(attribute.name),
          "value" -> BSONString(attribute.value.getOrElse(""))))
          collection.update(BSONDocument("code" -> new BSONString(code)), modifier)
    }

    def insert(attribute: Attribute): Future[LastError] = {
      require(!attribute.name.isEmpty)
      collection.insert(attribute)
    }
  }
}

