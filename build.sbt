name := """dogsbody"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  guice,
  cacheApi,
  ehcache,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "joda-time" % "joda-time" % "2.3",
  "org.xerial" % "sqlite-jdbc" % "3.8.6"
  ,"com.typesafe.play" %% "play-slick" % "3.0.0"
  ,"com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"
  ,"org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
)


//libraryDependencies += evolutions



resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


