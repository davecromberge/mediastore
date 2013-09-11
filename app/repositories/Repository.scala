package repositories

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import scala.concurrent.Future

trait MongoRepository[T] {
  implicit val reader: BSONReader[T, T]
  implicit val writer: BSONWriter[T, T]

  val connection = ReactiveMongoPlugin.connection
  val db = ReactiveMongoPlugin.db
  def collection: reactivemongo.api.Collection
}

trait Repository[T] {
  def get(id: String): Future[Option[T]]
  def all: Future[List[T]]
  def delete(id: String): Future[Any]
  def update(id: String, item: T): Future[Any]
  def insert(item: T): Future[Any]
}
