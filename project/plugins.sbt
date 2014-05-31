
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

fullResolvers := Seq(
    Resolver.url("Barcelona-Public-Repository-Ivy", url("https://project-barcelona.ch/nexus/content/groups/Barcelona-Public-Repositories-Ivy/"))(Resolver.ivyStylePatterns),
                 "Barcelona-Public-Repository-Maven" at "https://project-barcelona.ch/nexus/content/groups/Barcelona-Public-Repositories-Maven/"
)

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.7.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3")

//addSbtPlugin("reaktor" % "sbt-scct" % "0.2-SNAPSHOT")

//addSbtPlugin("com.sksamuel.scoverage" % "sbt-scoverage_2.10" % "0.95.7")

addSbtPlugin("com.github.mpeltonen" %% "sbt-idea" % "1.7.0-carsten")

