package models

import reactivemongo.bson._

case class Customer(
  id: Option[String],
  name: String,
  pack: Option[String],
  attributes: Option[Seq[Attribute]]
)

object Customer {

  implicit object CustomerBSONReader extends BSONDocumentReader[Customer] {
    def read(buffer: BSONDocument): Customer = {
      require(buffer != null)

      Customer(
        Option(buffer.getAs[BSONObjectID]("_id").get.stringify),
        buffer.getAs[BSONString]("name").get.value,
        buffer.getAs[BSONString]("pack").map(_.value),
        buffer.getAs[BSONArray]("attributes").map(attributes => 
          attributes.values.map(_.asInstanceOf[BSONDocument])
                           .map(attrDoc =>
                                  Attribute(attrDoc.getAs[BSONString]("code").get.value,
                                            attrDoc.getAs[BSONString]("name").get.value,
                                            Option(attrDoc.getAs[BSONString]("value").get.value))
                               )
        )
      )
    }
  }

  implicit object CustomerBSONWriter extends BSONDocumentWriter[Customer] {
    def write(customer: Customer): BSONDocument = {
      require(customer != null)

      var attributes = BSONArray(
        customer.attributes.get.map (attr =>
          BSONDocument(
            "code" -> BSONString(attr.code),
            "name" -> BSONString(attr.name),
            "value" -> BSONString(attr.value.getOrElse("")))
        )
      )

      BSONDocument(
        "_id" -> customer.id.map(new BSONObjectID(_)).getOrElse(BSONObjectID.generate),
        "name" -> BSONString(customer.name),
        "pack" -> customer.pack.map(new BSONString(_)).getOrElse(new BSONString("Unassigned")),
        "attributes" -> attributes
      )
    }
  }
}
