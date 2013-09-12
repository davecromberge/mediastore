package repositories

import models._
import models.JsonFormats._
import app.ComponentRegistry

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api._ 
import reactivemongo.core.commands.LastError
import scala.concurrent.Future

trait AttributeComponent {
  class AttributeRepo extends MongoRepository[Attribute] 
    with Repository[Attribute] {

    override val collection = db.collection[JSONCollection]("attributes")

    def all: Future[List[Attribute]] = {
      collection.find(Json.obj()).cursor[Attribute].toList
    }

    def get(code: String): Future[Option[Attribute]] = {
      require(!code.isEmpty)
      collection.find(Json.obj("code" -> code))
                .one[Attribute]
    }

    def delete(code: String): Future[LastError] = {
      require(!code.isEmpty)
      collection.remove(Json.obj("code" -> code))
    }

    def deleteAll: Future[LastError] = 
      collection.remove(Json.obj())

    def update(attribute: Attribute): Future[LastError] = {
      require(!attribute.code.isEmpty)
      require(!attribute.name.isEmpty)
      collection.update(Json.obj("code" -> attribute), attribute)
    }

    def insert(attribute: Attribute): Future[LastError] = {
      require(!attribute.name.isEmpty)
      collection.insert(attribute)
    }
  }
}

