import play.api._

object Global extends GlobalSettings {
  
  override def onStart(app: Application) {
    Bootstrap.init 
  }
  
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }  
}
