name := "soot-wrapper"

organization := "org.ucombinator"

version := "0.1"


// Depend on soot from the ucombinator repo
resolvers += "Ucombinator maven repository on github" at "https://ucombinator.github.io/maven-repo"

libraryDependencies ++= Seq(
    "org.ucombinator.soot" % "soot-all-in-one" % "nightly.20150205",
    "com.google.code.findbugs" % "jsr305" % "1.3.9"
)

javacOptions += "-Xlint:unchecked"

javacOptions in (Compile, doc) := Seq()

// Publish to the maven repo indicated on the command line by
// `-Ducombinator-repo="/path/to/repo"` else to the folder "deploy" in the current directory
publishTo := Some(Resolver.file("file",  new File(sys.props.getOrElse("ucombinator-repo", default = "deploy"))))


// Publish with maven and make this a pure java project
publishMavenStyle := true

crossPaths := false

autoScalaLibrary := false
