import sbt._

object Dependencies {

  object Version {
    val http4s     = "1.0.0-M31"
    val circe      = "0.14.1"
    val log4cats   = "2.2.0"
    val logback    = "1.2.10"
    val pureConfig = "0.17.1"
    val newtype    = "0.4.4"

    // Test
    val munit           = "0.7.29"
    val munitCatsEffect = "1.0.7"

    // Compiler
    val betterMonadicFor = "0.3.1" // TODO what for?
    val kindProjector    = "0.13.2" // TODO what for?
  }

  object Library { // Libraries
    def http4s(module: String): ModuleID = "org.http4s" %% s"http4s-$module" % Version.http4s

    val circe       = "io.circe"              %% "circe-generic"        % Version.circe
    val circeExtras = "io.circe"              %% "circe-generic-extras" % Version.circe
    val log4cats    = "org.typelevel"         %% "log4cats-slf4j"       % Version.log4cats
    val logback     = "ch.qos.logback"         % "logback-classic"      % Version.logback
    val pureConfig  = "com.github.pureconfig" %% "pureconfig"           % Version.pureConfig
    val newtype     = "io.estatico"           %% "newtype"              % Version.newtype

    val munit           = "org.scalameta" %% "munit"               % Version.munit           % Test // todo scalatest (?)
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % Version.munitCatsEffect % Test // todo scalatest (?)
    val log4catsNoOp    = "org.typelevel" %% "log4cats-noop"       % Version.log4cats        % Test

    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % Version.betterMonadicFor)
    val kindProjector    = compilerPlugin("org.typelevel" %% "kind-projector" % Version.kindProjector cross CrossVersion.full)
  }
}