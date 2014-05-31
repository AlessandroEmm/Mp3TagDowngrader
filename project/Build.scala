import sbt._
import Keys._
import com.typesafe.sbt.osgi.SbtOsgi._
import com.typesafe.sbt.osgi.OsgiKeys._
import com.typesafe.sbt.SbtScalariform._
import sbtunidoc.Plugin._
import sbtunidoc.Plugin.UnidocKeys._
import sbtrelease.ReleasePlugin._

object Values {


  val group = Seq("ch","ale")
  
  val groupId = group.mkString(".")
    
  def artifactId(name: String*) = (group ++ name).mkString("-")
  
  def bundleSymbolicName(name: String*) = (group ++ name).mkString(".")

  def osgiVersion(v: String) = { 
    val V = "^(.*?)(?:-SNAPSHOT)?$".r
    v match {
       case V(vers) => s"$vers.${System.currentTimeMillis}"
    }
  }
  
}


object BuildSettings { 

  def scalatestDependency(scalaVersion: String) = scalaVersion match {
    case "2.11.0-RC1" => "org.scalatest" %% "scalatest" % "2.1.0" % "test"
    case "2.11.0-RC3" => "org.scalatest" %% "scalatest" % "2.1.2" % "test"
    case "2.11.0-RC4" => "org.scalatest" %% "scalatest" % "2.1.3" % "test"
    case x => "org.scalatest" %% "scalatest" % "[2.1.3,3.0.0[" % "test"
  }
  


  val buildSettings = Defaults.defaultSettings ++ releaseSettings ++ /*unidocSettings ++*/ scalariformSettings ++ org.scalastyle.sbt.ScalastylePlugin.Settings ++ Seq(
    organization := Values.groupId,
    organizationName := "alm",
    homepage := Some(url("https://project-barcelona.ch/stash/projects/ARRAFW/repos/io.arra.framework/browse")),
    description := "core arra framework",
    licenses += "Proprietary" -> url("https://project-barcelona.ch/stash/projects/"),
    scalaVersion := "2.11.0",
    autoCompilerPlugins := true,
    scalacOptions ++= Seq("-feature","-unchecked","-deprecation","–encoding","UTF8","–explaintypes","-Xfuture"/*,"-Xlint"*/),
//mainClass in (Compile,run) := Some("ch.ale.barcode.Scan"), 
       
    //scalacOptions in (ScalaUnidoc, unidoc) += "-Ymacro-expand:none",
    autoAPIMappings := true, // for scaladoc

	libraryDependencies += "org" % "jaudiotagger" % "2.0.3", 
	libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",

    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
     publishTo := {
       val nexus = "https://project-barcelona.ch//nexus/content/repositories/"
       if (isSnapshot.value)
         Some("snapshots" at nexus + "arra_snapshot")
       else
         Some("releases" at nexus + "arra_release") },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

    // Use fullResolvers to avoid breaking out of our nexus!!!
    fullResolvers := Seq(
       // Local resolver in order to support publish-local

       // in order to resolve intra-project dependencies (between subprojects) we have to add the project resolver
       projectResolver.value, 
       // and of course we have to add our nexus repositories
       Resolver.url("Barcelona-Public-Repository-Ivy", url("https://project-barcelona.ch/nexus/content/groups/Barcelona-Public-Repositories-Ivy/"))(Resolver.ivyStylePatterns),
                    "Barcelona-Public-Repository-Maven" at "https://project-barcelona.ch/nexus/content/groups/Barcelona-Public-Repositories-Maven/"
    ),
    
    checksums := Nil // work-around the nexus SHA1 checksum problem
     
  )
}

object FrameworkCoreBuild extends Build {

  import BuildSettings._
  
  lazy val TagConverter = Project(
      "TagConverter", 
      file("TagConverter"), 
      settings = buildSettings ++ osgiSettings  ++ Seq(
         name := Values.bundleSymbolicName("TagConverter")
        ,description := "Barcode Scan"

      )
  )

  lazy val default = Project(
     "default",
     file("."),
     settings = buildSettings ++ Seq(
         publishLocal := {}
        ,publish := {}  
     )
  ).aggregate(TagConverter)
  


}
