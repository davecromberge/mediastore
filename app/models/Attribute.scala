package models

import reactivemongo.bson._

case class Attribute(
  code: String, 
  name: String, 
  value:Option[String]
)

object Attribute {

  implicit object AttributeBSONReader extends BSONDocumentReader[Attribute] {
    def read(buffer: BSONDocument): Attribute = {
      require(buffer != null)
      Attribute(
        buffer.getAs[BSONString]("code").get.value,
        buffer.getAs[BSONString]("name").get.value,
        None
      )
    }
  }

  implicit object AttributeBSONWriter extends BSONDocumentWriter[Attribute] {
    def write(attribute: Attribute): BSONDocument = {
      require(attribute != null)
      BSONDocument(
        "code" -> BSONString(attribute.code),
        "name" -> BSONString(attribute.name)
      )
    }
  }
}
