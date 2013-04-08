import android.os.Build
import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "douban-android",
    version := "0.1",
    versionCode := 1,
    scalaVersion := "2.10.1",
    platformName in Android := "android-14"
  )

  val proguardSettings = Seq (
    useProguard in Android := true,
    proguardOptimizations in Android ++= Seq("-dontobfuscate","-dontshrink","-dontoptimize","#-dontpreverify",
    "-keep class com.douban.ui.**","-keep class net.liftweb.json.*","-keep class scala.collection.immutable.StringLike",
     "-dontwarn scala.**","-dontwarn com.thoughtworks.**","-dontwarn ch.epfl.**",
    "-dontnote scala.**","-dontnote com.thoughtworks.**","-dontnote javax.xml.**","-dontnote org.w3c.dom.**","-dontnote org.xml.sax.**","-dontnote  org.scaloid.**"
    )
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    proguardSettings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "alias_name",
      libraryDependencies += "org.scalatest" %% "scalatest" % "latest.release" % "test"
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "douban-android",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++
               AndroidTest.androidSettings ++
               General.proguardSettings ++ Seq (
      name := "douban-androidTests"
    )
  ) dependsOn main
}
