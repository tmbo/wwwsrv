package com.scalableminds

import unfiltered.netty._
import scala.io._
import java.io._
import org.clapper.avsl.Logger
import unfiltered.request.GET
import unfiltered.request.Path
import unfiltered.response.Redirect
import com.scalableminds.image.ImageCompressor
import com.scalableminds.image.ImageWriter
import java.awt.image.BufferedImage

/** embedded server */
object FileMapper {
  def serializeMapping(mapping: Map[String, Array[String]]) = {
    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._
    import net.liftweb.json.Serialization.{ read, write }
    implicit val formats = Serialization.formats(NoTypeHints)
    write(mapping)
  }

  def writeToFile(mapping: Map[String, Array[String]], fileName: String) {
    println("Writing file mapping to '%s'".format(fileName))
    val file = new File(fileName)
    val out = new PrintWriter(file)
    try { out.print(serializeMapping(mapping)) }
    finally { out.close }
  }
}

object Server {
  val PIDFILENAME = "RUNNING_PID"
  val logger = Logger(Server.getClass)
  val fileMappingPath = "filemap.json"
  val compressedFilePathTemplate = "compressed%d.jpg"

  def anonymifyFileName(fileName: String)(implicit rootDir: File) =
    fileName.replace(rootDir.getAbsolutePath, "")

  def writeCompressedImageToFile(parentDir: String)(compressed: BufferedImage, idx: Int) = {
    val path = parentDir + "/" + compressedFilePathTemplate.format(idx)
    println("saving to " + path)
    (new ImageWriter).asJPGToFile(compressed, path)
    path
  }

  def processImageStack(parentDir: String)(is: Tuple2[Array[File], Int])(implicit rootDir: File) = {
    println("Rootdir: " + rootDir + " ParentDir: " + parentDir)
    (new ImageCompressor).compress(is._1).map { compressed =>
      val path = writeCompressedImageToFile(parentDir)(compressed, is._2)
      Map(anonymifyFileName(path) -> is._1.map(f => anonymifyFileName(f.getAbsolutePath)))
    } getOrElse (Map[String, Array[String]]())
  }

  def compressAllImages(dir: File): Map[String, Array[String]] = {
    implicit val rootDir = dir
    
    def compress(dir: File): Map[String, Array[String]] = {
      if (dir.isDirectory()) {
        dir.listFiles.groupBy(_.isDirectory).map {
          case (false, images) =>
            images
              .filter(i => i.getName().endsWith(".jpg") && !i.getName().contains("compressed"))
              .sortBy(_.getName)
              .sliding(100, 100)
              .toList
              .zipWithIndex
              .map(processImageStack(dir.getAbsolutePath))
          case (true, directories) =>
            directories
              .par
              .map(compress)
              .toList
        }.flatten.foldLeft(Map[String, Array[String]]())((m, e) => m ++ e)
      } else
        Map[String, Array[String]]()
    }
    compress(dir)
  }

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

  def verifyArguments(args: Array[String]) {
    if (args.size < 3)
      throw new RuntimeException("Please pass the port, imageAssetsDir and wwwDir.")
  }

  def main(args: Array[String]) {
    verifyArguments(args)

    println("---")
    writePidInfoToFile(args(1) + "/" + PIDFILENAME)
    println("Assets folder: " + args(1))
    println("Shellgame folder: " + args(2))
    println("---")

    val imageFolderPath = if (args(1).endsWith("/")) args(1) else args(1) + "/"
    val mainFolderPath = if (args(2).endsWith("/")) args(2) else args(2) + "/"

    println("Compressing images...")
    val fileMapping = compressAllImages(new File(imageFolderPath))
    FileMapper.writeToFile(fileMapping, imageFolderPath + fileMappingPath)
    println("Done compressing.")

    val imageAssets = new java.net.URL("file:" + imageFolderPath)
    val mainAssets = new java.net.URL("file:" + mainFolderPath)

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
