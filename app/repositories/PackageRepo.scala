package repositories

import app.ComponentRegistry
import models._

import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api._ 
import reactivemongo.bson._
import reactivemongo.core.commands._
import scala.concurrent.Future

trait PackageComponent {
  class PackageRepo extends MongoRepository[Package] with Repository[Package] {

    override val collection = db("packages")
    override implicit val reader = Package.PackageBSONReader
    override implicit val writer = Package.PackageBSONWriter

    def all: Future[List[Package]] = {
      val query = BSONDocument()
      collection.find(query).cursor[Package].toList
    }

    def get(id: String): Future[Option[Package]] = {
      require(!id.isEmpty)
      collection.find(BSONDocument("_id" -> BSONObjectID(id)))
                .one[Package]
    }

    def delete(id: String): Future[LastError] = {
      require(!id.isEmpty)
      collection.remove(BSONDocument("_id" -> BSONObjectID(id)))
    }

    def update(id: String, pack: Package): Future[LastError] = {
      require(!id.isEmpty)
      require(!pack.name.isEmpty)

      val modifier = BSONDocument(
        "$set" -> BSONDocument(
            "name" -> BSONString(pack.name)
        )
      )
      collection.update(BSONDocument("_id" -> new BSONObjectID(id)), modifier)
    }

    def insert(pack: Package): Future[LastError] = {
      require(!pack.name.isEmpty)
      collection.insert(pack)
    }
  }
}
