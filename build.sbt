import Dependencies.Library

name := "developer-connection"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    scalacOptions ++= List("-Ymacro-annotations"),
    scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings"))),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies ++= Seq(
      Library.http4s("ember-server"),
      Library.http4s("blaze-client"),
      Library.http4s("circe"),
      Library.http4s("dsl"),
      Library.scalaCacheCaffeine,
      Library.circe,
      Library.circeExtras,
      Library.derevoCirce,
      Library.logback,
      Library.pureConfig,
      Library.weaverCats,
      Library.newtype,
      Library.log4catsNoOp,
      Library.betterMonadicFor,
      Library.kindProjector
    )
  )

fork := true // https://github.com/typelevel/cats-effect/pull/833/files
