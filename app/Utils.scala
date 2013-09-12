package app

import java.util.concurrent.atomic.{ AtomicReferenceArray, AtomicInteger }
import scala.collection.mutable
import scala.concurrent._

import play.api._


object Utils {
  
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  def collect[A](futures: Seq[Future[A]]): Future[Seq[A]] = {
    if (futures.isEmpty) {
      future(Seq[A]())
    } else {
      val results = new AtomicReferenceArray[A](futures.size)
      val count = new AtomicInteger(futures.size)

      val promiseOfResult = promise[Seq[A]]
      val futureResult = promiseOfResult.future

      for (i <- 0 until futures.size) {
        val f = futures(i)
        f onSuccess { case x =>
          results.set(i, x)
          if (count.decrementAndGet() == 0) {
            val resultsArray = new mutable.ArrayBuffer[A](futures.size)
            for (j <- 0 until futures.size) resultsArray += results.get(j)
            promiseOfResult success resultsArray.toSeq
          }
        }
        f onFailure { case cause =>
            promiseOfResult failure cause
        }
      }

      futureResult
    }
  }
}
