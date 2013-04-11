import sbt._

import Keys._

import AndroidKeys._

resolvers += "scala-sdk" at "https://raw.github.com/jinntrance/douban-scala/master/repo/releases/"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit","-target:jvm-1.6","-optimise")

//autoScalaLibrary := false

//unmanagedBase <<= baseDirectory { base => base / "libs" }

libraryDependencies ++= Seq(
			"org.scaloid" % "scaloid" % "latest.release",
			"com.douban" %% "scala-api" % "2.0" excludeAll(
				ExclusionRule(organization = "org.scala-lang"),
				ExclusionRule(organization = "org.scalatest")),
			"com.google.zxing" % "core" % "latest.release",
			"com.google.zxing" % "android-integration" % "latest.release" excludeAll(
			ExclusionRule("com.google.android","android")))

proguardOptimizations in Android ++= Seq(
"-dontobfuscate",
"-dontoptimize",
"#-dontpreverify",
"-dontwarn scala.**",
"-dontwarn org.scaloid.**",
"-dontwarn com.douban.**",
"-dontwarn net.liftweb.json.**",
"-dontwarn com.thoughtworks.**",
"-dontwarn android.**",
"-dontnote org.scaloid.**",
"-dontnote com.douban.**",
"-dontnote net.liftweb.json.**",
"-dontnote javax.xml.**",
"-dontnote org.w3c.dom.**",
"-dontnote org.xml.sax.**",
"-dontnote scala.Enumeration",
"-keep class net.liftweb.json.**{*;}",
"-keep class com.douban.ui.**",
"#-keep class com.douban.**{*;}",
"#-keepclassmembers class com.douban.**{*;}",
"-keepclassmembers class com.douban.common.Req{private *;}",
"-keepclassmembers class com.douban.models.Bean{private *;}",
"-keepclassmembers class * extends com.douban.models.API{private *;}",
"-keep public class scala.Option",
"-keep public class scala.Function0",
"-keep public class scala.Function1",
"-keep public class scala.Function2",
"-keep public class scala.Product",
"-keep public class scala.Tuple2",
"-keep public class scala.collection.Seq",
"#-keep public class scala.collection.GenSeq",
"-keep public class scala.collection.immutable.List",
"-keep public class scala.collection.immutable.Map",
"-keep public class scala.collection.immutable.Seq",
"-keep public class scala.collection.immutable.Set",
"-keep public class scala.collection.immutable.Vector",
"-keep public class scala.collection.SeqLike {public protected *;}",
"-keep public class * extends org.scaloid.common.SActivity",
"-keep public class * extends android.app.Activity",
"-keep public class * extends android.app.Application",
"-keep public class * extends android.app.Service",
"-keep public class * extends android.app.backup.BackupAgentHelper",
"-keep public class * extends android.appwidget.AppWidgetProvider",
"-keep public class * extends android.content.BroadcastReceiver",
"-keep public class * extends android.content.ContentProvider",
"-keep public class * extends android.preference.Preference",
"-keep public class * extends android.view.View",
"-keep public class com.android.vending.licensing.ILicensingService",
"-keepclasseswithmembernames class * {native <methods>;}",
"-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet);}",
"-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet, int);}",
"-keepclassmembers class * extends android.app.Activity {   public void *(android.view.View);}",
"-keepclassmembers enum * {public static **[] values();public static ** valueOf(java.lang.String);}",
"-keep class * implements android.os.Parcelable {  public static final android.os.Parcelable$Creator *;}"
)
