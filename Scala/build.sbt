import com.typesafe.sbt.packager.Keys.dockerEnvVars
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.5"

val `cats-core-version`         = "2.13.0"
val `cats-effect-version`       = "3.5.7"
val `cats-tagless-version`      = "0.16.3"
val `tapir-version`             = "1.11.13"
val `http4s-version`            = "0.23.30"
val `sttp-client3-version`      = "3.10.2"
val `logback-version`           = "1.5.16"
val `tethys-version`            = "0.29.3"
val `pureconfig-version`        = "0.17.8"
val `asyncapi-circe-version`    = "0.11.7"
val `weaver-version`            = "0.8.4"
val `testcontainers-version`    = "0.41.8"
val `mockserver-client-version` = "5.15.0"
val `doobie-version`            = "1.0.0-RC8"
val `liquibase-version`         = "4.27.0"
val `h2-version`                = "2.3.232"
val `redis4cats-version`        = "1.7.2"
val `fs2-aws-version`           = "6.2.0"
val `fs2-kafka-version`         = "3.6.0"
val `tofu-version`              = "0.13.7"
val `wiremock-version`          = "3.3.1"

val deps: List[ModuleID] = List(
  // cats
  "org.typelevel" %% "cats-core"   % `cats-core-version`,
  "org.typelevel" %% "cats-effect" % `cats-effect-version`,

  // tapir
  "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"     % `tapir-version`,
  "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % `tapir-version`,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-tethys"       % `tapir-version`,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"       % `tapir-version`,
  "com.softwaremill.sttp.tapir"   %% "tapir-asyncapi-docs"     % `tapir-version`,
  "com.softwaremill.sttp.apispec" %% "asyncapi-circe-yaml"     % `asyncapi-circe-version`,

  // http4s
  "org.http4s" %% "http4s-ember-server" % `http4s-version`,
  "org.http4s" %% "http4s-ember-client" % `http4s-version`,
  "org.http4s" %% "http4s-dsl"          % `http4s-version`,

  // sttp
  "com.softwaremill.sttp.client3" %% "core" % `sttp-client3-version`,
  "com.softwaremill.sttp.client3" %% "cats" % `sttp-client3-version`,

  // logback
  "ch.qos.logback" % "logback-classic" % `logback-version`,

  // tethys
  "com.tethys-json" %% "tethys-core"       % `tethys-version`,
  "com.tethys-json" %% "tethys-jackson213" % `tethys-version`,
  "com.tethys-json" %% "tethys-derivation" % `tethys-version`,
  "com.tethys-json" %% "tethys-enumeratum" % `tethys-version`,

  // pureconfig
  "com.github.pureconfig" %% "pureconfig-core"           % `pureconfig-version`,
  "com.github.pureconfig" %% "pureconfig-generic-scala3" % `pureconfig-version`,

  // tofu
  "tf.tofu" %% "tofu-core-ce3"           % `tofu-version`,
  "tf.tofu" %% "tofu-logging"            % `tofu-version`,
  "tf.tofu" %% "tofu-logging-derivation" % `tofu-version`,

  // logback
  "ch.qos.logback" % "logback-classic" % "1.4.8"
)

val testDeps: List[ModuleID] = List(
  // for tests
  "com.disneystreaming" %% "weaver-cats" % `weaver-version` % Test,
  // containers - https://github.com/testcontainers/testcontainers-scala/blob/master/docs/src/main/tut/setup.md
  "com.dimafeng" %% "testcontainers-scala-mockserver" % `testcontainers-version` % Test, // example for mockserver
  "org.wiremock"  % "wiremock-standalone"             % `wiremock-version`       % Test, // wiremock client
  "com.dimafeng" %% "testcontainers-scala-wiremock"   % `testcontainers-version` % Test, // scala wrapper for wiremock
  "org.mock-server" % "mockserver-client-java" % `mockserver-client-version` % Test
)

val dbDeps: List[ModuleID] = List(
  "com.h2database" % "h2"              % `h2-version`,
  "org.liquibase"  % "liquibase-core"  % `liquibase-version`,
  "org.tpolecat"  %% "doobie-core"     % `doobie-version`,
  "org.tpolecat"  %% "doobie-postgres" % `doobie-version`,
  "org.tpolecat"  %% "doobie-h2"       % `doobie-version`,
  "org.tpolecat"  %% "doobie-hikari"   % `doobie-version`
)

val nosqlDeps: List[ModuleID] = List(
  "dev.profunktor" %% "redis4cats-effects" % `redis4cats-version`,
  "io.laserdisc"   %% "fs2-aws-s3"         % `fs2-aws-version`
)

val kafkaDeps: List[ModuleID] = List(
  "com.github.fd4s" %% "fs2-kafka" % `fs2-kafka-version`
)

lazy val `seminar-1` = project
  .settings(
    libraryDependencies ++= deps
  )

lazy val `seminar-2` = project
  .settings(
    libraryDependencies ++= deps
  )

lazy val `seminar-3` = project
  .settings(
    libraryDependencies ++= deps
  )

lazy val `seminar-4` = project
  .settings(
    libraryDependencies ++= deps
  )
  // Set docker build
  .enablePlugins(
    DockerPlugin,
    JavaAppPackaging
  )
  .settings(
    Compile / mainClass  := Some("tbank.ab.Seminar4App"), // class which will be run
    dockerBaseImage      := "eclipse-temurin:21",         // base image for Docker
    dockerExposedPorts   := List(8080),                   // defines exposing ports of the Docker image
    Docker / packageName := "tbank-ab",                   // name of the Docker image
    dockerEnvVars ++= Map("UNUSED_ENV_CONST_VAR" -> "some value") // environment variables for the Docker image
  )

lazy val `seminar-4-it` = project
  .dependsOn(`seminar-4`)
  // Set it tests
  .settings(
    libraryDependencies ++= testDeps,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )

lazy val `seminar-5` = project
  .settings(
    libraryDependencies ++= deps ++ dbDeps
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "migrations")

lazy val `seminar-6` = project
  .settings(
    libraryDependencies ++= deps ++ dbDeps
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "migrations")

lazy val `seminar-7` = project
  .settings(
    libraryDependencies ++= deps ++ dbDeps ++ nosqlDeps
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "migrations")
  // Set docker build
  .enablePlugins(
    DockerPlugin,
    JavaAppPackaging
  )
  .settings(
    Compile / mainClass  := Some("tbank.ab.Seminar7App"), // class which will be run
    dockerBaseImage      := "eclipse-temurin:21",         // base image for Docker
    dockerExposedPorts   := List(8080, 8083),             // defines exposing ports of the Docker image
    Docker / packageName := "tbank-ab",                   // name of the Docker image
    dockerEnvVars ++= Map("UNUSED_ENV_CONST_VAR" -> "some value") // environment variables for the Docker image
  )

lazy val `seminar-8` = project
  .settings(
    libraryDependencies ++= deps ++ dbDeps ++ nosqlDeps ++ kafkaDeps
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "migrations")
  // Set docker build
  .enablePlugins(
    DockerPlugin,
    JavaAppPackaging
  )
  .settings(
    Compile / mainClass  := Some("tbank.ab.Seminar8App"), // class which will be run
    dockerBaseImage      := "eclipse-temurin:21",         // base image for Docker
    dockerExposedPorts   := List(8080, 8083),             // defines exposing ports of the Docker image
    Docker / packageName := "tbank-ab",                   // name of the Docker image
    dockerEnvVars ++= Map("UNUSED_ENV_CONST_VAR" -> "some value") // environment variables for the Docker image
  )

lazy val `seminar-10` = project
  .settings(
    libraryDependencies ++= deps ++ dbDeps ++ nosqlDeps ++ kafkaDeps
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "migrations")
  // Set docker build
  .enablePlugins(
    DockerPlugin,
    JavaAppPackaging
  )
  .settings(
    Compile / mainClass  := Some("tbank.ab.Seminar10App"), // class which will be run
    dockerBaseImage      := "eclipse-temurin:21",          // base image for Docker
    dockerExposedPorts   := List(8080, 8083),              // defines exposing ports of the Docker image
    Docker / packageName := "tbank-ab",                    // name of the Docker image
    dockerEnvVars ++= Map("UNUSED_ENV_CONST_VAR" -> "some value") // environment variables for the Docker image
  )

lazy val `seminar-11` = project
  .settings(
    libraryDependencies ++= deps ++ dbDeps ++ nosqlDeps ++ kafkaDeps
  ).settings(
    scalacOptions ++= Seq("-Ykind-projector:underscores")
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "migrations")

lazy val `seminar-12` = project
  .settings(
    libraryDependencies ++= deps ++ dbDeps ++ nosqlDeps ++ kafkaDeps
  ).settings(
    scalacOptions ++= Seq("-Ykind-projector:underscores")
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "migrations")

lazy val `seminar-12-it` = project
  .dependsOn(`seminar-12`)
  // Set it tests
  .settings(
    libraryDependencies ++= testDeps,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )

lazy val seminars = (project in file(".")).settings(
  name := "seminars"
).aggregate(
//  `seminar-1`,
//  `seminar-2`,
//  `seminar-3`,
//  `seminar-4`,
//  `seminar-4-it`,
//  `seminar-5`,
//  `seminar-6`,
//  `seminar-7`,
//  `seminar-8`,
//  `seminar-10`,
  `seminar-11`,
  `seminar-12`
)
