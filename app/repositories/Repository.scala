package repositories

import play.api.Play.current
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.Future

trait MongoRepository[T] {
  val connection = ReactiveMongoPlugin.connection
  val db = ReactiveMongoPlugin.db
  def collection: JSONCollection
}

trait Repository[T] {
  def get(id: String): Future[Option[T]]
  def all: Future[List[T]]
  def delete(id: String): Future[Any]
  def deleteAll: Future[Any]
  def update(item: T): Future[Any]
  def insert(item: T): Future[Any]
}
