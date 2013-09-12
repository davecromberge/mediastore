package core

import app._
import models._
import repositories._

import akka.actor.Actor
import akka.actor.Props
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent._

object Messages {
  case class Match(customer: Customer, packages: Iterable[Package])
}

class RecordMatcherActor extends Actor 
   with ComponentRegistry {
   
   lazy val cm = getCategoryMatcher
   lazy val sm = getSimilarityMatcher
   
   def receive = {
     case Messages.Match(customer: Customer, packages: Iterable[Package]) => 
        sender ! findBestMatch(customer, packages)
   }

   def findBestMatch(customer: Customer, packages: Iterable[Package]): Future[Package] = {
     Utils.collect[(BigDecimal, Package)](
       packages.map { p => 
          score(customer.attributes.get, p.attributes.get, p)
       }.toSeq
     )
     .map { packageScores => 
        packageScores.maxBy { 
           case (score, pack) => score
        }._2
     }
   }
 
   def score(xs: Iterable[Attribute], ys: Iterable[Attribute], pack: Package): Future[(BigDecimal, Package)] = {
      
      val result =
        xs.filter(_.value.isDefined)
          .toList
          .sortBy(_.code)
          .zip(ys.filter(_.value.isDefined)
                 .toList
                 .sortBy(_.code))
          .map { case (attrX, attrY) =>
                   val areSimilar = sm.calculate(attrX.value, attrY.value) >= minStringSimilarity
                   val criteria = Criteria(attrX, areSimilar, attrX.value, attrY.value, xs, ys)
                   cm.weight(criteria)
                     .zip(cm.calculate(criteria))
                     .map { case (w, s) => w * s }
          }
      Utils.collect(result.toSeq)
           .map(_.sum)
           .map(bestScore => 
              bestScore -> pack)
   }
}
 
