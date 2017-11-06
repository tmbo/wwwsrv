organization := "com.example"

name := "wwwsrv"

version := "0.2.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.6.4",
  "net.databinder" %% "unfiltered-netty" % "0.6.4",
  "net.databinder" %% "unfiltered-netty-server" % "0.6.4",
  "net.databinder" %% "unfiltered-json" % "0.6.4",
  "org.clapper" %% "avsl" % "0.4",
  "net.databinder" %% "unfiltered-spec" % "0.6.4" % "test"
)

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2"
)
