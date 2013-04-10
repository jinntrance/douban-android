import sbt._

resolvers += "scala-sdk" at "https://raw.github.com/jinntrance/douban-scala/master/repo/releases/"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit","-target:jvm-1.6","-optimise")

autoScalaLibrary := false

//unmanagedBase <<= baseDirectory { base => base / "libs" }

libraryDependencies ++= Seq(
			"org.scaloid" % "scaloid" % "latest.release",
			"com.douban" %% "scala-api" % "2.0" excludeAll(
				ExclusionRule(organization = "org.scala.lang"),
				ExclusionRule(organization = "org.scalatest")),
		    "com.google.zxing" % "core" % "latest.release",
			"com.google.zxing" % "android-integration" % "latest.release" excludeAll(
                ExclusionRule("com.google.android","android")))
