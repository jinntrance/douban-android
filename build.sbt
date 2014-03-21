import sbt._

import android.Keys._

import android.Dependencies.{apklib,aar}

android.Plugin.androidBuild

organization := "com.douban"

name := "douban-book"

scalaVersion := "2.11.0-RC3"

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
			"org.scaloid" %% "scaloid" % "3.3-8-SNAPSHOT",
      "org.scaloid" %% "scaloid-support-v4" % "3.3-8-SNAPSHOT",
			"com.douban" %% "scala-api" % "2.4.5",
			"com.google.android" % "support-v4" % "r7",
			"com.google.zxing" % "core" % "3.0.0",
      "com.github.chrisbanes.photoview" % "library" % "1.2.2"
			)

useProguard in Android := true

typedResources in Android := false

proguardCache in Android ++=Seq(
  ProguardCache("org.scaloid*") % "org.scaloid",
  ProguardCache("com.douban.models") % "com.douban")

//libraryProjects in Android += android.Dependencies.LibraryProject(file("libs/zxing-android"))

//libraryProjects in Android += android.Dependencies.LibraryProject(file("libs/slidingMenu"))

proguardOptions in Android <++= baseDirectory(_ / "proguard-android-optimize.txt").flatMap(f => task(scala.io.Source.fromFile(f).getLines().toSeq))

proguardOptions in Android <++= baseDirectory(_ / "proguard.cfg").flatMap(f => task(scala.io.Source.fromFile(f).getLines().toSeq))

