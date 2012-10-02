package com.example

import unfiltered.request._
import unfiltered.response._

import org.clapper.avsl.Logger

/** unfiltered plan */
class App extends unfiltered.filter.Plan {
  import QParams._

  val logger = Logger(classOf[App])

  def intent = {
    case GET(Path(p)) =>
      logger.debug("GET %s" format p)
      ResponseString("Thanks.")
  }
}

/** embedded server */
object Server {
  val logger = Logger(Server.getClass)
  def main(args: Array[String]) {
    println("---")
args foreach println
println("---")
    if(args.size < 2)
      throw new RuntimeException("Please pass the port and www dir.")
    val http = unfiltered.jetty.Http.local(args(0).toInt) // this will not be necessary in 0.4.0
    http.context("/") { x =>
        x.resources(new java.net.URL("file://"+args(1)))
        x.current.setAliases(true)  
      }.run({ svr =>
        unfiltered.util.Browser.open(http.url)
      }, { svr =>
        logger.info("shutting down server")
      })
  }
}
