import sbt._
import sbt.Keys._
import android.Keys._

object DoubanBuild extends Build {
  // add 'run' alias to the root project
  lazy val root = Project(id = "douban-book", base = file(".")) settings(
    android.Plugin.androidCommands ++ Seq(
      run   <<= (run in (Android)) map { _ => Unit }): _*
    ) dependsOn(slidingmenu,zxingLib)


  //
  lazy val zxingLib = Project(
    id = "zxing-android", base = file("libs/zxing-android")) settings(
    Seq(libraryDependencies ++= Seq("com.google.zxing" % "core" % "2.2")) ++
    commonSettings:_*)

  // another android library
  lazy val slidingmenu = Project(
    id = "slidingMenu", base = file("libs/slidingMenu")) settings(
    Seq(libraryDependencies ++= Seq("com.google.android" % "support-v4" % "r7")) ++
    commonSettings: _*)

  // this project configuration doesn't require specifying arguments to
  // androidBuild due to the way it's setup, but specifying them is harmless.
  lazy val commonSettings = android.Plugin.androidBuild :+
    (watchSources ~= { _.filterNot(_.isDirectory) })
}
