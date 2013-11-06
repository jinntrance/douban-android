import sbt._

import android.Keys._

import android.Dependencies._

android.Plugin.androidBuild

organization := "com.douban"

name := "douban-book"

version := "2.1"

scalaVersion := "2.10.3"

minSdkVersion in Android := 14

platformTarget in Android := "android-18"

//resolvers += "oss repo" at "https://oss.sonatype.org/content/repositories/releases/"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit","#-optimise")

autoScalaLibrary := false

//unmanagedBase <<= baseDirectory { base => base / "libs" }

// call install and run without having to prefix with android:
run <<= run in Android

install <<= install in Android

libraryDependencies ++= Seq(
			"org.scaloid" % "scaloid_2.10" % "2.4-8",
			"com.douban" % "scala-api_2.10" % "2.4.3" withSources(),
			"com.google.android" % "support-v4" % "r7",
      "com.google.zxing" % "core" % "2.2"
			)

useProguard in Android := true

typedResources in Android :=false

proguardCache in Android ++=Seq(
  ProguardCache("org.scaloid.common") % "org.scaloid",
  ProguardCache("com.douban.models") % "com.douban")

//libraryProjects in Android += android.Dependencies.LibraryProject(file("libs/zxing-android"))

//libraryProjects in Android += android.Dependencies.LibraryProject(file("libs/slidingMenu"))

proguardConfig in Android <<= baseDirectory map (b => IO.readLines(b/"proguard.cfg"))



