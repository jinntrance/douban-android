import sbt._

import android.Keys._

//import AndroidKeys._

android.Plugin.androidBuild

organization := "com.douban"

name := "douban-book"

version := "2.1"

scalaVersion := "2.10.3"

platformTarget in Android := "android-14"

//resolvers += "oss repo" at "https://oss.sonatype.org/content/repositories/releases/"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit","#-optimise")

autoScalaLibrary := false

//unmanagedBase <<= baseDirectory { base => base / "libs" }

// call install and run without having to prefix with android:
run <<= run in Android

install <<= install in Android

libraryDependencies ++= Seq(
            "org.scala-lang" % "scala-reflect" % "2.10.3",
			"org.scaloid" % "scaloid_2.10" % "2.4-8" withSources() withJavadoc(),
			"com.douban" % "scala-api_2.10" % "2.4.2" withSources() withJavadoc(),
			"com.google.zxing" % "core" % "2.2",
			"com.google.zxing" % "android-integration" % "2.2",
			"com.google.android" % "support-v4" % "r7"
			)

useProguard in Android := true

proguardOptions in Android :="""
-verbose
-printseeds target/keep.log
-printmapping target/obf.log
-optimizationpasses 5
-overloadaggressively
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-repackageclasses
-allowaccessmodification
-mergeinterfacesaggressively
-assumenosideeffects class scala.Console
-assumenosideeffects class org.scaloid.common.WidgetFamily**
-assumenosideeffects class android.util.Log {public static boolean isLoggable(java.lang.String, int);public static int v(...); public static int i(...); public static int w(...); public static int d(...); public static int e(...);}
-dontpreverify
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-keepclassmembers class * { ** MODULE$; }
-keepattributes *Annotation*
-keep class org.scaloid.common.SContext
-keep class org.scaloid.common.LoggerTag
-keep class android.support.v4.app.Fragment
-keep class scala.reflect.Manifest
-keep class scala.reflect.ClassTag
-keep class scala.collection.mutable.ArrayBuffer
-keep class scala.math.Ordering
-keep public class scala.Option
-keep public class scala.PartialFunction
-keep public class scala.Function0
-keep public class scala.Function1
-keep public class scala.Function2
-keep public class scala.Product
-keep public class scala.Tuple2
-keep public class scala.collection.GenSeq
-keep public class scala.collection.generic.CanBuildFrom
-keep public class scala.collection.SeqLike {public protected *;}
-keepclasseswithmembernames class * {native <methods>;}
-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet);}
-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet, int);}
-keepclassmembers class * extends android.app.Activity {   public void *(android.view.View);}
-keepclassmembers enum * {public static **[] values();public static ** valueOf(java.lang.String);}
-keep class * implements android.os.Parcelable {  public static final android.os.Parcelable$Creator *;}
-keep class * implements java.io.Serializable
-keepclassmembers class **.R$* {public static <fields>;}
""".split('\n').toSeq



