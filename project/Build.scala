import sbt._
import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "douban-book",
    version := "0.1",
    versionCode := 1,
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
      keyalias in Android := "jinntrance@gmail.com",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.9" % "test"
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "douban-book",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++
               AndroidTest.androidSettings ++
               General.proguardSettings ++ Seq (
      name := "douban-bookTests"
    )
  ) dependsOn main
}
