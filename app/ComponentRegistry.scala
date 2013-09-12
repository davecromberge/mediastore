package app

import scala.concurrent.{ExecutionContext}
import core._
import repositories._

import java.util.UUID

trait ComponentRegistry extends 
   StatisticsComponent with
   AttributeComponent with
   CustomerComponent with
   PackageComponent
{

   lazy val statsRepo = new StatisticsRepo
   lazy val attributeRepo = new AttributeRepo
   lazy val customerRepo = new CustomerRepo
   lazy val packageRepo = new PackageRepo
 
   val minStringSimilarity = 0.8
   def getCategoryMatcher: CategoricalMetric = OF
   def getSimilarityMatcher: SimilarityMetric = JaroWinkler

   def nextId: Option[String] = Some(UUID.randomUUID.toString.take(8))
}
