import sbt._

import android.Keys._

import android.Dependencies._

android.Plugin.androidBuild

organization := "com.douban"

name := "douban-book"

scalaVersion := "2.10.3"

minSdkVersion in Android := 14

platformTarget in Android := "android-18"

//resolvers += "oss repo" at "https://oss.sonatype.org/content/repositories/releases/"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit")

autoScalaLibrary := false

//unmanagedBase <<= baseDirectory { base => base / "libs" }

// call install and run without having to prefix with android:
run <<= run in Android

install <<= install in Android

libraryDependencies ++= Seq(
			"org.scaloid" % "scaloid_2.10" % "3.2-8",
			"com.douban" % "scala-api_2.10" % "2.4.4",
			"com.google.android" % "support-v4" % "r7",
      "com.google.zxing" % "core" % "2.3.0"
			)

useProguard in Android := true

typedResources in Android :=false

proguardCache in Android ++=Seq(
  ProguardCache("org.scaloid.common") % "org.scaloid",
  ProguardCache("com.douban.models") % "com.douban")

//libraryProjects in Android += android.Dependencies.LibraryProject(file("libs/zxing-android"))

//libraryProjects in Android += android.Dependencies.LibraryProject(file("libs/slidingMenu"))

proguardOptions in Android <++= baseDirectory(_/"proguard-android-optimize.txt").flatMap(f=>task(scala.io.Source.fromFile(f).getLines().toSeq))

proguardOptions in Android <++= baseDirectory(_/"proguard.cfg").flatMap(f=>task(scala.io.Source.fromFile(f).getLines().toSeq))

