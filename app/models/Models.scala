package models

case class Attribute(
   code: String, 
   name: String, 
   value:Option[String]
)

case class Customer(
  id: Option[String],
  name: String,
  pack: Option[String],
  attributes: Option[Seq[Attribute]]
)

case class Package(
   id: Option[String], 
   name: String, 
   attributes: Option[Seq[Attribute]]
)

object JsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._

  implicit val attributeFormat = Json.format[Attribute]
  implicit val customerFormat = Json.format[Customer]
  implicit val packageFormat = Json.format[Package]
}
