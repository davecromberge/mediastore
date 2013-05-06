package models

import play.api._

import reactivemongo.bson._
import reactivemongo.bson.handlers._

case class Customer(
  id: Option[String],
  name: String,
  pack: Option[String],
  attributes: Option[Seq[Attribute]]
)

object Customer {

  implicit object CustomerBSONReader extends BSONReader[Customer] {
    def fromBSON(document: BSONDocument) :Customer = {
      require(document != null)

      val doc = document.toTraversable
      Customer(
        Option(doc.getAs[BSONObjectID]("_id").get.stringify),
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[BSONString]("pack").map(_.value),
        doc.getAs[TraversableBSONArray]("attributes").map(attributes => 
          attributes.toList.map(_.asInstanceOf[BSONDocument].toTraversable)
                           .map(attrDoc =>
                                  Attribute(attrDoc.getAs[BSONString]("code").get.value,
                                            attrDoc.getAs[BSONString]("name").get.value,
                                            Option(attrDoc.getAs[BSONString]("value").get.value))
                               )
        )
      )
    }
  }

  implicit object CustomerBSONWriter extends BSONWriter[Customer] {
    def toBSON(customer: Customer) = {
      require(customer != null)

      var attributes = new AppendableBSONArray
      customer.attributes.get.map (attr =>
        attributes += BSONDocument(
          "code" -> BSONString(attr.code),
          "name" -> BSONString(attr.name),
          "value" -> BSONString(attr.value.getOrElse("")))
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
