package models

import reactivemongo.bson._

case class Package(id: Option[String], name: String, attributes: Option[Seq[Attribute]])

object Package {

  implicit object PackageBSONReader extends BSONDocumentReader[Package] {
    def read(buffer: BSONDocument): Package = {
      require(buffer != null)
      
      Package(
        Option(buffer.getAs[BSONObjectID]("_id").get.stringify),
        buffer.getAs[BSONString]("name").get.value,
        buffer.getAs[BSONArray]("attributes").map(attributes => 
          attributes.toList.map(_.asInstanceOf[BSONDocument])
                           .map(attrDoc =>
                                  Attribute(attrDoc.getAs[BSONString]("code").get.value,
                                            attrDoc.getAs[BSONString]("name").get.value,
                                            Option(attrDoc.getAs[BSONString]("value").get.value))
                               )
        )
      )
    }
  }

  implicit object PackageBSONWriter extends BSONDocumentWriter[Package] {
    def write(pack: Package): BSONDocument = {
      require(pack != null)

      var attributes = BSONArray(
        pack.attributes.get.map (attr =>
          BSONDocument(
            "code" -> BSONString(attr.code),
            "name" -> BSONString(attr.name),
            "value" -> BSONString(attr.value.getOrElse("")))
        )
      )

      BSONDocument(
        "_id" -> pack.id.map(new BSONObjectID(_)).getOrElse(BSONObjectID.generate),
        "name" -> BSONString(pack.name),
        "attributes" -> attributes
      )
    }
  }
}
