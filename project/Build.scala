import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

   val appName         = "media-monkey"
   val appVersion      = "1.0"

   val appDependencies = Seq(
      "org.reactivemongo" %% "play2-reactivemongo" % "0.9"
   )

   val main = play.Project(appName, appVersion, appDependencies)
}
