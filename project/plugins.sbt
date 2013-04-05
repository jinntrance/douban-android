scalaVersion := "2.9.2"

name := "douban-android"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.7")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")

addSbtPlugin("org.scala-sbt" % "xsbt-proguard-plugin" % "0.1.3")

resolvers += Resolver.url("scalasbt releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

//addSbtPlugin("org.scala-sbt" % "sbt-android-plugin" % "0.6.2")

resolvers += Resolver.url("scala-sbt releases", new URL(
  "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(
  Resolver.ivyStylePatterns)

addSbtPlugin("com.hanhuy.sbt" % "android-sdk-plugin" % "0.3.16")

