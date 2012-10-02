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
    if(args.size < 1)
      throw new RuntimeException("Please pass the www dir.")
    val http = unfiltered.jetty.Http.anylocal // this will not be necessary in 0.4.0
    http.context("/") { _.resources(new java.net.URL("file://"+args(0))) }
      .run({ svr =>
        unfiltered.util.Browser.open(http.url)
      }, { svr =>
        logger.info("shutting down server")
      })
  }
}
