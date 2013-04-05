import sbt._
import Keys._
import AndroidKeys._

name := "douban-android"

scalaVersion := "2.10.1"

autoScalaLibrary := false

resolvers += "scala-sdk" at "https://raw.github.com/jinntrance/douban-scala/master/repo/releases/"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit")

libraryDependencies ++= Seq("com.google.zxing" % "core" % "latest.release",
			"com.google.zxing" % "android-integration" % "latest.release",
			"org.scaloid" % "scaloid" % "latest.release",
			"com.douban" %% "scala-api" % "2.0")

seq(androidBuildSettings: _*)

useProguard in Android := true

//proguardOptions in Android += Seq("-dontobfuscate", "-dontoptimize")
