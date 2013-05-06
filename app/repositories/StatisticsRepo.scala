package repositories

import app._ 
import models._

import play.api.Play.current
import play.api._
import scala.util.{ Random }
import scala.collection.immutable.{ Map, HashMap }
import scala.concurrent.{ExecutionContext, Future}

import reactivemongo.api._ 
import reactivemongo.bson._
import reactivemongo.core.commands._
import reactivemongo.bson.handlers.DefaultBSONHandlers._

/* Similarity Measures for Categorical Data: A Comparative Evaluation

   http://www.siam.org/proceedings/datamining/2008/dm08_22_Boriah.pdf

   This repository attempts to use the data as an indicator when scoring matches 
   and mismatches across categorical attributes.
*/

trait DataStatistics {
   // d
   val numberOfAttributes: Future[Long]
 
   // N
   val numberOfRecords: Future[Long]
  
   // fk(x)
   def getFrequency(attr: Attribute, value: Option[String]): Future[Long]
 
   // pk(x)
   def getProbability(attr: Attribute, value: Option[String]): Future[BigDecimal]
 
   // p2k(x)
   def getProbability2(attr: Attribute, value: Option[String]): Future[BigDecimal]
  
   // nk
   def getSampleSize(attr: Attribute): Future[Long]
 
   // X~
   def getDistribution(attr: Attribute): Map[String, Long]
}

trait StatisticsComponent {
   class StatisticsRepo extends DataStatistics with ComponentRegistry {
      
      implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
      lazy val numberOfRecords: Future[Long] = customerRepo.all.map(_.size)
      lazy val numberOfAttributes: Future[Long] = attributeRepo.all.map(_.size)
 
      def getFrequency(attr: Attribute, value: Option[String]): Future[Long] = {
        require(!attr.code.isEmpty)

        customerRepo.collection.find(BSONDocument("attributes.value" -> new BSONString(value.getOrElse(""))))
                    .toList
                    .map(_.size)
      }
 
      def getSampleSize(attr: Attribute): Future[Long] = {
         require(!attr.code.isEmpty)
         // todo
         numberOfRecords
      }
 
      def getProbability(attr: Attribute, value: Option[String]): Future[BigDecimal] = {
         require(!attr.code.isEmpty)
         
         for {
            fkx <- getFrequency(attr, value)
            n <- numberOfRecords
         } yield fkx / n
      }
 
      def getProbability2(attr: Attribute, value: Option[String]): Future[BigDecimal] = {
         require(!attr.code.isEmpty)
 
         for {
            fkx <- getFrequency(attr, value)
            n <- numberOfRecords
         } yield (fkx * (fkx - 1)) / (n * (n - 1))
      }
     
      def getDistribution(attr: Attribute): Map[String, Long] = {
         require(!attr.code.isEmpty)
         
         // dummy data - not implemented
         var distribution = new HashMap[String, Long]
         distribution += (Random.nextString(10) -> Random.nextInt)
         distribution += (Random.nextString(10) -> Random.nextInt)
         distribution += (Random.nextString(10) -> Random.nextInt)
         distribution
      }
   }
}
