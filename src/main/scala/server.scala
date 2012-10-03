package com.example

import unfiltered.request._
import unfiltered.response._
import scala.io._
import java.io._

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
  val PIDFILENAME = "RUNNING_PID"
  val logger = Logger(Server.getClass)

def writePidInfoToFile(fileName: String) {
  val pidInfo = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")
  if(pidInfo.size < 2)
    throw new RuntimeException("Couldn't write pid to file.")
  println("PID: " + pidInfo(0))
  val file = new File(fileName)
  file.delete
  val out = new PrintWriter( file )
  try{ out.print( pidInfo(0) ) }
  finally{ out.close }
}

  def main(args: Array[String]) {
    println("---")
    writePidInfoToFile(args(1) + "/" + PIDFILENAME)
    args foreach println
    println("---")
    if(args.size < 3)
      throw new RuntimeException("Please pass the port, dir and www dir.")
    val http = unfiltered.jetty.Http.local(args(0).toInt) // this will not be necessary in 0.4.0
    http.context("/") { x =>
        x.resources(new java.net.URL("file://"+args(1) + args(2)))
        x.current.setAliases(true)  
      }.run({ svr =>
        unfiltered.util.Browser.open(http.url)
      }, { svr =>
        logger.info("shutting down server")
      })
  }
}
