import sbt._

import android.Keys._

import android.Dependencies.{apklib,aar}

android.Plugin.androidBuild

organization := "com.douban"

name := "douban-book"

scalaVersion := "2.11.2"

minSdkVersion in Android := "14"

platformTarget in Android := "android-20"

//resolvers += "oss repo" at "https://oss.sonatype.org/content/repositories/releases/"

javacOptions ++= Seq("-source", "1.7")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit")

incOptions := incOptions.value.withNameHashing(true) 

autoScalaLibrary := false

//android.enforceUniquePackageName :=false

//unmanagedBase <<= baseDirectory { base => base / "libs" }

// call install and run without having to prefix with android:
run <<= run in Android

install <<= install in Android

libraryDependencies ++= Seq(
			"org.scaloid" %% "scaloid" % "3.5-10-SNAPSHOT",
			"org.scaloid" %% "scaloid-support-v4" % "3.5-10-SNAPSHOT",
			"com.douban" %% "scala-api" % "2.4.5",
			"com.google.zxing" % "core" % "3.1.0",
			"com.github.chrisbanes.photoview" % "library" % "1.2.3"
			)

useProguard in Android := true

typedResources in Android := false

proguardCache in Android ++=Seq(
  ProguardCache("org.scaloid*") % "org.scaloid",
  ProguardCache("com.douban.models") % "com.douban")

proguardOptions in Android <++= baseDirectory(_ / "proguard-android-optimize.txt").flatMap(f => task(scala.io.Source.fromFile(f).getLines().toSeq))

proguardOptions in Android <++= baseDirectory(_ / "proguard.cfg").flatMap(f => task(scala.io.Source.fromFile(f).getLines().toSeq))

