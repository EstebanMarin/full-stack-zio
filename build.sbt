scalaVersion := "2.13.6"

val Ver = new {
  val http4s  = "0.21.24"
  val slinky  = "0.6.7"
  val logback = "1.2.3"
  val zio     = "1.0.9"
  val circe   = "0.14.1"
  val tapir   = "0.16.16"
  val doobie  = "0.13.4"
  val caliban = "0.10.1"
}

lazy val sharedSettings = Seq(
  scalaVersion := "2.13.6",
  version := "0.1.0-SNAPSHOT",
  organization := "com.estebanmarin",
  organizationName := "EstebanMarin",
  libraryDependencies ++= Seq(
    "org.typelevel"              %% "cats-core"             % "2.6.1",
    "io.circe"                   %%% "circe-parser"         % Ver.circe,
    "io.circe"                   %%% "circe-generic-extras" % Ver.circe,
    "io.circe"                   %%% "circe-generic"        % Ver.circe,
    "io.circe"                   %%% "circe-literal"        % Ver.circe,
    "com.softwaremill.quicklens" %%% "quicklens"            % "1.7.4",
    "io.scalaland"               %%% "chimney"              % "0.6.1"
  ),
  scalacOptions ++= Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:higherKinds",
    "-Ymacro-annotations",
    "-Ywarn-unused:imports",
    "-Xlint:-byname-implicit" // github.com/scala/bug/issues/12072 TODO
  )
)

lazy val jsSettings = Seq(
  libraryDependencies ++= Seq(
    "me.shadaj"             %%% "slinky-web"                % Ver.slinky,
    "me.shadaj"             %%% "slinky-react-router"       % Ver.slinky,
    "io.suzaku"             %%% "diode"                     % "1.1.14",
    "com.github.oen9"       %%% "slinky-bridge-react-konva" % "0.1.1",
    "com.github.ghostdogpr" %%% "caliban-client"            % Ver.caliban
  ),
  Compile / npmDependencies ++= Seq(
    "react"            -> "16.13.1",
    "react-dom"        -> "16.13.1",
    "react-popper"     -> "1.3.7",
    "react-router-dom" -> "5.2.0",
    "path-to-regexp"   -> "6.1.0",
    "bootstrap"        -> "4.5.2",
    "jquery"           -> "3.5.1",
    "konva"            -> "4.2.2",
    "react-konva"      -> "16.13.0-3",
    "use-image"        -> "1.0.6"
  ),
  scalaJSUseMainModuleInitializer := true,
  webpack / version := "4.44.2",
  webpackBundlingMode := BundlingMode.Application,
  fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly()
)

lazy val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio"                     %% "zio"                      % Ver.zio,
    "dev.zio"                     %% "zio-interop-cats"         % "2.5.1.0",
    "dev.zio"                     %% "zio-logging-slf4j"        % "0.5.10",
    "org.http4s"                  %% "http4s-blaze-server"      % Ver.http4s,
    "org.http4s"                  %% "http4s-circe"             % Ver.http4s,
    "org.http4s"                  %% "http4s-dsl"               % Ver.http4s,
    "org.http4s"                  %% "http4s-blaze-client"      % Ver.http4s,
    "com.github.ghostdogpr"       %% "caliban"                  % Ver.caliban,
    "com.github.ghostdogpr"       %% "caliban-http4s"           % Ver.caliban,
    "com.softwaremill.sttp.tapir" %% "tapir-core"               % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % Ver.tapir,
    "ch.qos.logback"              % "logback-classic"           % Ver.logback,
    "com.github.pureconfig"       %% "pureconfig"               % "0.15.0",
    "org.reactivemongo"           %% "reactivemongo"            % "1.0.4",
    "org.flywaydb"                % "flyway-core"               % "7.9.2",
    "org.postgresql"              % "postgresql"                % "42.2.20",
    "org.tpolecat"                %% "doobie-core"              % Ver.doobie,
    "org.tpolecat"                %% "doobie-h2"                % Ver.doobie,
    "org.tpolecat"                %% "doobie-hikari"            % Ver.doobie,
    "org.reactormonk"             %% "cryptobits"               % "1.3",
    "org.mindrot"                 % "jbcrypt"                   % "0.4",
    "dev.zio"                     %% "zio-test"                 % Ver.zio % Test,
    "dev.zio"                     %% "zio-test-sbt"             % Ver.zio % Test,
    "com.h2database"              % "h2"                        % "1.4.200" % Test
  ),
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  target := baseDirectory.value / ".." / "target",
  addCompilerPlugin(("org.typelevel" %% "kind-projector" % "0.13.0").cross(CrossVersion.full))
)

lazy val app =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("."))
    .settings(sharedSettings)
    .jsSettings(jsSettings)
    .jvmSettings(jvmSettings)

lazy val appJS = app.js
  .enablePlugins(ScalaJSBundlerPlugin)
  .disablePlugins(RevolverPlugin)

lazy val appJVM = app.jvm
  .enablePlugins(JavaAppPackaging)
  .settings(
    dockerExposedPorts := Seq(8080),
    dockerBaseImage := "ghcr.io/graalvm/graalvm-ce:java11-21.3.0",
    Compile / unmanagedResourceDirectories += (appJS / Compile / resourceDirectory).value,
    Universal / mappings ++= (appJS / Compile / fullOptJS / webpack).value.map { f =>
      f.data -> s"assets/${f.data.getName()}"
    },
    Universal / mappings ++= Seq(
      (appJS / Compile / target).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "bootstrap" / "dist" / "css" / "bootstrap.min.css" -> "assets/bootstrap.min.css"
    ),
    bashScriptExtraDefines += """addJava "-Dassets=${app_home}/../assets""""
  )

disablePlugins(RevolverPlugin)
enablePlugins(CodegenPlugin)
