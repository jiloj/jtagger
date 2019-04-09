// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.20")

// sbt-paradox, used for documentation
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.3.3")

// Load testing tool:
// http://gatling.io/docs/2.2.2/extensions/sbt_plugin.html
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.2")

// Scala formatting: "sbt scalafmt"
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0")

// Scala linter
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.1")
