package core

import play.api._

import app._
import models._
import repositories._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits._

case class Criteria(
  attr: Attribute, valuesAreSimilar: Boolean, 
  v1: Option[String], v2: Option[String], 
  xs: Iterable[Attribute], ys: Iterable[Attribute])
 
trait Metric extends ComponentRegistry {
   def calculate(criteria: Criteria): Future[BigDecimal]
}
 
trait CategoricalMetric extends Metric {
   def weight(criteria: Criteria): Future[BigDecimal] = {
     statsRepo.numberOfAttributes.map { d =>
       assume(d >= 0)
       if (d == 0) 1 else 1 / d
     }
   }
}

object Overlap extends CategoricalMetric {
  def calculate(criteria: Criteria): Future[BigDecimal] = {
    if (criteria.valuesAreSimilar) { 
      future { 1 } 
    }
    else 
    {
      future { 0 }
    }
  }
}

object Eskin extends CategoricalMetric {
   def calculate(criteria: Criteria): Future[BigDecimal] = {
      require(!criteria.attr.code.isEmpty)
 
      if (criteria.valuesAreSimilar) {
        future { 1 }
      } else {
        statsRepo.getSampleSize(criteria.attr).map(nk =>
          BigDecimal((nk * nk) / (nk * nk + 2))
        )
      }
   }
}

object IOF extends CategoricalMetric
{
   def calculate(criteria: Criteria): Future[BigDecimal] = {
      require(!criteria.attr.code.isEmpty)
 
      if (criteria.valuesAreSimilar) {
        future { 1 }
      } else {
        statsRepo.getFrequency(criteria.attr, criteria.v1)
          .zip(statsRepo.getFrequency(criteria.attr, criteria.v2))
          .map { case (fkX, fkY) => if (fkX <= 1 || fkY <= 1) 0 else 1 / (math.log(fkX) * math.log(fkY)) }
      }
   }
}

object OF extends CategoricalMetric {
  def calculate(criteria: Criteria): Future[BigDecimal] = {
    require(!criteria.attr.code.isEmpty)

    if (criteria.v1 == criteria.v2) {
      future { 1 }
    } else {
      statsRepo.numberOfRecords.flatMap { n =>
        statsRepo.getFrequency(criteria.attr, criteria.v1)
          .zip(statsRepo.getFrequency(criteria.attr, criteria.v2))
          .map { case (fkX, fkY) => 
            1 / (math.log(n.doubleValue / fkX.doubleValue) * math.log(n.doubleValue / fkY.doubleValue))
          }
        }
    }
  }
}

object Lin extends CategoricalMetric {
   def calculate(criteria: Criteria): Future[BigDecimal] = {
      require(!criteria.attr.code.isEmpty)
     
      val pkXFuture: Future[BigDecimal] = statsRepo.getProbability(criteria.attr, criteria.v2)
      val pkYFuture: Future[BigDecimal] = statsRepo.getProbability(criteria.attr, criteria.v2)
 
      if (criteria.v1 == criteria.v1) {
        pkXFuture.map(pkX =>
          2 * math.log(pkX.doubleValue))
      } else {
        pkXFuture.zip(pkYFuture).map { case (pkX, pkY) => 
          2 * math.log((pkX + pkY).doubleValue)
        }
      }
   }
}
 /*
object Lin1 extends CategoricalMetric {
   
   private def diagonal(xs: Iterable[Attribute], ys: Iterable[Attribute]) = {
     xs.zip(ys)
       .map(pair => 
          statsRepo.getProbability(pair._1, pair._1.value.get).zip(statsRepo.getProbability(pair._2, pair._2.value.get)))
       .filter(pair => pair._1 <= pair._2)
       .unzip
       ._1
   }
   def calculate(criteria: Criteria): Future[BigDecimal] = {
      require(!criteria.attr.code.isEmpty)
     
      val subsetQ = diagonal(criteria.xs, criteria.ys)
 
      if (criteria.v1 == criteria.v1) {
         subsetQ.map(q => math.log(statsRepo.getProbability(q, q.value.get).doubleValue))
                .sum
      } else {
         2 * math.log(
            subsetQ.map (q => statsRepo.getProbability(q, q.value.get).doubleValue)
                   .sum
         )
      }
   }
 
   override def weight(criteria: Criteria): BigDecimal = {
     1 / (diagonal(criteria.xs, criteria.ys).map(q => statsRepo.getProbability(q, q.value.get)).sum)
   }
}
*/
