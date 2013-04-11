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
"-verbose","-dontobfuscate","-dontoptimize","-printseeds keep.log", "-printmapping obf.log",
"-keepparameternames","-dontskipnonpubliclibraryclasses","-dontskipnonpubliclibraryclassmembers",
"-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod",
"-keepclassmembers class * { ** MODULE$; }", "-keepdirectories",
"-keep public class scala.ScalaObject",
"-keep public class scala.Function*",
"-keep public class scala.Tuple2",
"-keep public class scala.Tuple4",
"#-keep class net.liftweb.json.**{*;}",
"-keep class scala.Symbol",
"-keep class scala.runtime.ObjectRef",
"-keep class scala.runtime.VolatileObjectRef",
"-keep class scala.reflect.Manifest",
"-keep class scala.reflect.ClassTag",
"-keep class scala.reflect.ClassManifestDeprecatedApis*",
"-keep class scala.collection.mutable.ArrayBuffer",
"-keep class scala.collection.mutable.StringBuilder",
"-keep class scala.collection.mutable.Builder",
"-keep class scala.collection.Traversable",
"-keep class scala.collection.generic.CanBuildFrom",
"-keep class scala.collection.GenMapLike*",
"-keep class scala.collection.GenSeqViewLike*",
"-keep class scala.collection.DefaultMap*",
"-keep class scala.collection.TraversableLike*",
"-keep class scala.collection.GenTraversableOnce",
"-keep class scala.collection.parallel.IterableSplitter*",
"-keep class scala.collection.parallel.SeqSplitter*",
"-keep class scala.math.Ordering",
"-keep class scala.math.BigInt",
"-keep class scala.math.Numeric",
"-keep class scala.math.BigDecimal",
"-keep class scala.text.Document",
"-keep class scala.text.DocText",
"-keep class scala.util.matching.Regex",
"-keep class net.liftweb.json.JsonAST**",
"-keep class net.liftweb.json.Formats**",
"-keep class net.liftweb.json.DateFormat",
"-keep class net.liftweb.json.TypeHints",
"-keep class net.liftweb.json.Serializer",
"-keep class net.liftweb.json.Extraction**",
"-keep class net.liftweb.json.JsonParser**",
"-keep class net.liftweb.json.FieldSerializer**",
"-keep class net.liftweb.json.ParameterNameReader",
"-keep class org.scaloid.common.SActivity",
"-keep class org.scaloid.common.SContext",
"-keep class org.scaloid.common.Registerable",
"-keep class org.scaloid.common.LoggerTag",
"-keep class org.scaloid.common..WidgetFamily*",
"-keep public class scala.Option",
"-keep public class scala.PartialFunction",
"-keep public class scala.Function0",
"-keep public class scala.Function1",
"-keep public class scala.Function2",
"-keep public class scala.Product",
"-keep public class scala.Tuple2",
"-keep public class scala.collection.Seq",
"-keep public class scala.collection.GenSeq",
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
"-keepclasseswithmembernames class * {native <methods>;}",
"-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet);}",
"-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet, int);}",
"-keepclassmembers class * extends android.app.Activity {   public void *(android.view.View);}",
"-keepclassmembers enum * {public static **[] values();public static ** valueOf(java.lang.String);}",
"-keep class * implements android.os.Parcelable {  public static final android.os.Parcelable$Creator *;}"
)
