scalaVersion := "2.10.1"

resolvers += "scala-sdk" at "https://raw.github.com/jinntrance/douban-scala/master/repo/releases/"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit")

libraryDependencies ++= Seq("com.douban" %% "scala-api" % "2.0")

seq(androidBuildSettings: _*)

