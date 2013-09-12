package repositories

import app.ComponentRegistry
import models._
import models.JsonFormats._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api._ 
import reactivemongo.core.commands._
import scala.concurrent.Future

trait PackageComponent {
  class PackageRepo extends MongoRepository[Package] 
    with Repository[Package] 
    with ComponentRegistry {

    override val collection = db.collection[JSONCollection]("packages")

    def all: Future[List[Package]] = {
      collection.find(Json.obj())
                .sort(Json.obj("name" -> 1))
                .cursor[Package]
                .toList
    }

    def get(id: String): Future[Option[Package]] = {
      require(!id.isEmpty)
      collection.find(Json.obj("id" -> id))
                .one[Package]
    }

    def delete(id: String): Future[LastError] = {
      require(!id.isEmpty)
      collection.remove(Json.obj("id" -> id))
    }

    def update(pack: Package): Future[LastError] = {
      require(!pack.id.isEmpty)
      require(!pack.name.isEmpty)
      collection.update(Json.obj("id" -> pack.id), pack)
    }

    def insert(pack: Package): Future[LastError] = {
      require(!pack.name.isEmpty)
      collection.insert(pack.copy(id = nextId))
    }
  }
}
