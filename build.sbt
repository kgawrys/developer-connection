import Dependencies.Library

name := "developer-connection"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    scalacOptions ++= List("-Ymacro-annotations"),
    scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings"))),
    libraryDependencies ++= Seq(
      Library.http4s("ember-server"),
      Library.http4s("blaze-client"),
      Library.http4s("circe"),
      Library.http4s("dsl"),
      Library.circe,
      Library.circeExtras,
      Library.derevoCirce,
      Library.logback,
      Library.pureConfig,
      Library.munit,
      Library.munitCatsEffect,
      Library.newtype,
      Library.log4catsNoOp,
      Library.betterMonadicFor,
      Library.kindProjector
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )

fork := true // https://github.com/typelevel/cats-effect/pull/833/files
