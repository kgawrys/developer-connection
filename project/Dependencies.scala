import sbt._

object Dependencies {

  object Version {
    val circe              = "0.14.1"
    val derevo             = "0.13.0"
    val http4s             = "1.0.0-M31"
    val newtype            = "0.4.4"
    val log4cats           = "2.2.0"
    val logback            = "1.2.10"
    val pureConfig         = "0.17.1"
    val scalaCacheCaffeine = "1.0.0-M6"

    // Test
    val weaver = "0.7.9"

    // Compiler
    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
  }

  object Library { // Libraries
    def http4s(module: String): ModuleID   = "org.http4s" %% s"http4s-$module"   % Version.http4s
    def derevo(artifact: String): ModuleID = "tf.tofu"    %% s"derevo-$artifact" % Version.derevo

    val derevoCirce = derevo("circe-magnolia")

    val circe              = "io.circe"              %% "circe-generic"        % Version.circe
    val circeExtras        = "io.circe"              %% "circe-generic-extras" % Version.circe
    val newtype            = "io.estatico"           %% "newtype"              % Version.newtype
    val log4cats           = "org.typelevel"         %% "log4cats-slf4j"       % Version.log4cats
    val logback            = "ch.qos.logback"         % "logback-classic"      % Version.logback
    val pureConfig         = "com.github.pureconfig" %% "pureconfig"           % Version.pureConfig
    val scalaCacheCaffeine = "com.github.cb372"      %% "scalacache-caffeine"  % Version.scalaCacheCaffeine

    val weaverCats   = "com.disneystreaming" %% "weaver-cats"   % Version.weaver   % Test
    val log4catsNoOp = "org.typelevel"       %% "log4cats-noop" % Version.log4cats % Test

    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % Version.betterMonadicFor)
    val kindProjector    = compilerPlugin("org.typelevel" %% "kind-projector" % Version.kindProjector cross CrossVersion.full)
  }
}
