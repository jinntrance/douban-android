import sbt._

import android.Keys._

import android.Dependencies.{apklib,aar}

android.Plugin.buildAar

organization := "com.douban"

name := "douban-android"

scalaVersion := "2.11.6"

minSdkVersion in Android := "16"

platformTarget in Android := "android-19"

javacOptions ++= Seq("-source", "1.7")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit")

incOptions := incOptions.value.withNameHashing(true) 

autoScalaLibrary := false

//android.enforceUniquePackageName :=false

transitiveAndroidLibs in Android := false

antLayoutDetector in Android := ()

libraryProject in Android := false

// call install and run without having to prefix with android:
run <<= run in Android

install <<= install in Android

// use local published scaloid in order to build 'scaloid-support-v4' lib
val scaloidVersion = "3.6.1-10"

libraryDependencies ++= Seq(
			"org.scaloid" %% "scaloid" % scaloidVersion,
			"org.scaloid" %% "scaloid-support-v4" % scaloidVersion , /* local published */
			"com.douban" %% "scala-api" % "2.4.7",
			/* "com.google.zxing" % "core" % "3.1.0", */
      /* "org.scala-lang" % "scala-library" % "2.11.6" % "provided", */
			"com.github.chrisbanes.photoview" % "library" % "1.2.3" exclude("com.android.support", "support-v4")
			)

unmanagedJars in Compile ~= { _ filterNot (_.data.getName startsWith "android-support-v4") } 

useProguard in Android := true

proguardScala in Android := true

typedResources in Android := false

shrinkResources in Android := true

proguardCache in Android ++=Seq(
	"com.douban",
	"com.android.support", 
	"com.google.zxing",
  "com.jeremyfeinstein",
	"org.scaloid"
)


//integrate all the proguard options from the config file into android-sdk-plugin
//proguardOptions in Android ++= scala.io.Source.fromFile(baseDirectory.value.getAbsolutePath+"/proguard-android-optimize.txt").getLines().toSeq.filter(a=>{! a.trim.isEmpty && ! a.contains("//")})

proguardOptions in Android ++= scala.io.Source.fromFile(baseDirectory.value.getAbsolutePath+"/proguard.cfg").getLines().toSeq.filter(a=>{! a.trim.isEmpty && ! a.contains("//")})

