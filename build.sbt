import sbt._

resolvers += "scala-sdk" at "https://raw.github.com/jinntrance/douban-scala/master/repo/releases/"

scalacOptions ++= Seq("-feature","-unchecked", "-deprecation", "-Xcheckinit")

autoScalaLibrary := false

libraryDependencies ++= Seq(
			"org.scaloid" % "scaloid" % "latest.release",
			"com.douban" %% "scala-api" % "2.0")
