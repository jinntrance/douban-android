import sbt._

resolvers += "scala-sdk" at "https://raw.github.com/jinntrance/douban-scala/master/repo/releases/"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-feature","-unchecked", "-deprecation", "-Xcheckinit","-target:jvm-1.6","–optimise")

autoScalaLibrary := false

libraryDependencies ++= Seq(
			"org.scaloid" % "scaloid" % "latest.release",
			"com.douban" %% "scala-api" % "2.0")
