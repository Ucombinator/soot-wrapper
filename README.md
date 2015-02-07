# soot-wrapper

## API

```
import org.ucombinator.SootWrapper;
import soot.SootClass;
import soot.util.Chain;
import soot.jimple.toolkits.callgraph.CallGraph;
```

Call any of:

```
SootWrapper.Source source = SootWrapper.fromApk("/path/to/apk", "/path/to/android/platforms");
SootWrapper.Source source = SootWrapper.fromClasses("/directory/to/analyze", "/additional/classpath:/items/here");
SootWrapper.Source source = SootWrapper.fromSource("/directory/to/analyze", "/additional/classpath:/items/here");
```

And then any of:

```
Chain<SootClass> jimple = source.getJimple();
Chain<SootClass> shimple = source.getShimple();
CallGraph source.getCallGraph();
```

## Getting it

The current version of this project is published in the maven repository at https://ucombinator.github.io/maven-repo as:
artifactId soot-wrapper
groupId org.ucombinator
version 0.1

Depend on it from a tool that supports maven-style dependency management, like maven, ivy, sbt, or leiningen.

Sample `sbt` project configuration depending on this tool:

```
name := "soot-test"

organization := "org.ucombinator"

version := "0-SNAPSHOT"

resolvers += "Ucombinator maven repository on github" at "https://ucombinator.github.io/maven-repo"

libraryDependencies ++= Seq(
        "org.ucombinator" % "soot-wrapper" % "0.1"
)
```

## Releasing

To make a new release, first update the version number in `build.sbt` and the readme. Then run:

```
sbt -Ducombinator-repo=/path/to/repo publish
```

where `/path/to/repo` is the path to a checkout of the `gh-site` branch of `https://github.com/Ucombinator/soot-wrapper/`. Then commit and push the changes to both repositories.
