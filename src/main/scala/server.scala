package com.scalableminds

import unfiltered.netty._
import scala.io._
import java.io._
import org.clapper.avsl.Logger
import unfiltered.request.GET
import unfiltered.request.Path
import unfiltered.response.Redirect

/** embedded server */
object Server {
  val PIDFILENAME = "RUNNING_PID"
  val logger = Logger(Server.getClass)

  def writePidInfoToFile(fileName: String) {
    val pidInfo = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")
    if (pidInfo.size < 2)
      throw new RuntimeException("Couldn't write pid to file.")
    println("PID: " + pidInfo(0))
    val file = new File(fileName)
    file.delete
    val out = new PrintWriter(file)
    try { out.print(pidInfo(0)) }
    finally { out.close }
  }

  def main(args: Array[String]) {
    if (args.size < 3)
      throw new RuntimeException("Please pass the port, dir and www dir.")
    println("---")
    writePidInfoToFile(args(1) + "/" + PIDFILENAME)
    args foreach println
    println("---")
    val mainAssets = new java.net.URL("file:" + args(2))
    val imageAssets = new java.net.URL("file:" + args(1))
    println("ass: " + imageAssets)

    val indexSupplier = unfiltered.netty.cycle.Planify {
      case GET(Path("/")) => Redirect("index.html")
    }

    val http = unfiltered.netty.Http.local(args(0).toInt) // this will not be necessary in 0.4.0
    http.resources(mainAssets, 0, true).resources(imageAssets, 0, true).plan(indexSupplier).run({ svr =>
      unfiltered.util.Browser.open(http.url)
    }, { svr =>
      logger.info("shutting down server")
    })
  }
}
