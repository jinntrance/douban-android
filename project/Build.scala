import android.os.Build
import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "douban-client",
    version := "0.1",
    versionCode := 0,
    scalaVersion := "2.10.1",
    platformName in Android := "android-14"
  )

  val proguardSettings = Seq (
    useProguard in Android := true
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    proguardSettings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies ++= 
      Seq(
      "org.scalatest" %% "scalatest" % "2.0" % "test")
      )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "douban-client",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++
               AndroidTest.androidSettings ++
               General.proguardSettings ++ Seq (
      name := "douban-clientTests"
    )
  ) dependsOn main
}
