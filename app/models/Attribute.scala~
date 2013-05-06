package models

import reactivemongo.bson._
import reactivemongo.bson.handlers._

case class Attribute(
  code: String, 
  name: String, 
  value:Option[String]
)

object Attribute {

  implicit object AttributeBSONReader extends BSONReader[Attribute] {
    def fromBSON(document: BSONDocument) :Attribute = {
      require(document != null)

      val doc = document.toTraversable
      Attribute(
        doc.getAs[BSONString]("code").get.value,
        doc.getAs[BSONString]("name").get.value,
        None
      )
    }
  }

  implicit object AttributeBSONWriter extends BSONWriter[Attribute] {
    def toBSON(attribute: Attribute) = {
      require(attribute != null)

      BSONDocument(
        "code" -> BSONString(attribute.code),
        "name" -> BSONString(attribute.name)
      )
    }
  }
}
