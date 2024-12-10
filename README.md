# JAR Cache

Speed up Java compilation by caching reads of JAR indices.

## Problem

In large builds, a given JAR may be referenced on hundreds of separate sub-projects. Even when the build tool
compiles these from a single JVM, each compilation still reads the JAR index from disk.

This can be a non-trivial overhead, especially when the JAR is on a network file system.

In practice, we can assume that certain JARs are immutable and can be cached.

**WARNING**: This is a proof of concept. It is not production ready.

## Usage as a dynamically attached agent

Add `-XX:+EnableDynamicAgentLoading` to avoid warnings about dynamically attaching the agent.

```
javacOptions += "-XX:+EnableDynamicAgentLoading" // to avoid warnings about dynamically attaching the agent.

libraryDependencies += "io.github.retronym" % "jarcache-agent" % "X.Y.Z"

libraryDependencies += "net.bytebuddy" % "byte-buddy-agent" % "1.15.10"
```

In application code, prior to calling `javac`:

```java
System.setProperty("io.github.retronym.jarcache.cacheableRegex", ".*/compileTest.*");
net.bytebuddy.agent.ByteBuddyAgent.install();
com.github.retronym.jarcache.Agent.installDynamically();
```

See `JarCacheAgentTest` in the demo project for an example.

## Usage as a Java Agent

### Build the agent
```
jarcache> sbt clean "show agent/assembly"
...
[info] /Users/jz/code/jarcache/agent/target/agent-assembly-0.1.0-SNAPSHOT.jar
```

### Start SBT (or any Java build tool) with the agent enabled.

```
myproject> sbt -J-javaagent:/Users/jz/code/jarcache/agent/target/agent-assembly-0.1.0-SNAPSHOT.jar \
   -J-Dio.github.retronym.jarcache.cacheableRegex='.*/Caches/Coursier/v1/https/repo1.maven.org/(?!.*-SNAPSHOT).*' \
   -J-Dio.github.retronym.jarcache.debug=true \
   compile
 
JARCACHE: Instrumenting with ByteBuddy
JARCACHE: Instrumenting with ByteBuddy
[info] welcome to sbt 1.10.6 (Azul Systems, Inc. Java 23.0.1)
[info] loading global plugins from /Users/jz/.sbt/1.0/plugins
[info] loading settings for project jarcache-build from plugins.sbt...
[info] loading project definition from /Users/jz/code/jarcache/project
[info] loading settings for project jarcache from build.sbt...
[info] set current project to jarcache (in build file:/Users/jz/code/jarcache/)
[success] Total time: 0 s, completed 10 Dec 2024, 5:15:11 pm
[info] compiling 3 Java sources to /Users/jz/code/jarcache/demo/target/test-classes ...
[Byte Buddy] TRANSFORM com.sun.tools.javac.file.FSInfo [jdk.internal.loader.ClassLoaders$AppClassLoader@5a07e868, module jdk.compiler, Thread[#136,pool-8-thread-10,5,main], loaded=false]
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/moditect/jfrunit/jfrunit-core/1.0.0.Alpha2/jfrunit-core-1.0.0.Alpha2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/5.11.3/junit-jupiter-api-5.11.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/github/sbt/junit/jupiter-interface/0.13.3/jupiter-interface-0.13.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/net/bytebuddy/byte-buddy/1.15.10/byte-buddy-1.15.10.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter/5.8.2/junit-jupiter-5.8.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/vintage/junit-vintage-engine/5.8.2/junit-vintage-engine-5.8.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/assertj/assertj-core/3.21.0/assertj-core-3.21.0.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/platform/junit-platform-commons/1.11.3/junit-platform-commons-1.11.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/platform/junit-platform-launcher/1.11.3/junit-platform-launcher-1.11.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.11.3/junit-jupiter-engine-5.11.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/test-interface/1.0/test-interface-1.0.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-params/5.8.2/junit-jupiter-params-5.8.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/platform/junit-platform-engine/1.11.3/junit-platform-engine-1.11.3.jar
JARCACHE: Not caching /Users/jz/.sbt/boot/scala-2.12.20/lib/scala-library.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/moditect/jfrunit/jfrunit-core/1.0.0.Alpha2/jfrunit-core-1.0.0.Alpha2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/5.11.3/junit-jupiter-api-5.11.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/github/sbt/junit/jupiter-interface/0.13.3/jupiter-interface-0.13.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/net/bytebuddy/byte-buddy/1.15.10/byte-buddy-1.15.10.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter/5.8.2/junit-jupiter-5.8.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/vintage/junit-vintage-engine/5.8.2/junit-vintage-engine-5.8.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/assertj/assertj-core/3.21.0/assertj-core-3.21.0.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/platform/junit-platform-commons/1.11.3/junit-platform-commons-1.11.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/platform/junit-platform-launcher/1.11.3/junit-platform-launcher-1.11.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.11.3/junit-jupiter-engine-5.11.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/test-interface/1.0/test-interface-1.0.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-params/5.8.2/junit-jupiter-params-5.8.2.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
JARCACHE: Caching /Users/jz/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/junit/platform/junit-platform-engine/1.11.3/junit-platform-engine-1.11.3.jar
JARCACHE: Not caching /Users/jz/.sbt/boot/scala-2.12.20/lib/scala-library.jar
[success] Total time: 1 s, completed 10 Dec 2024, 5:15:12 pm
```

## Test

The demo project includes a test that demonstrates the caching behavior and counts the reduced number of file reads.

```
sbt:jarcache> demo/test
Instrumenting with ByteBuddy
...
[info] Test demo.JarCacheAgentTest#compileWithCaching() started
...
[Byte Buddy] TRANSFORM com.sun.tools.javac.file.FSInfo [jdk.internal.loader.ClassLoaders$AppClassLoader@5a07e868, module jdk.compiler, Thread[#29,pool-1-thread-1,5,main], loaded=false]
[Byte Buddy] TRANSFORM jdk.nio.zipfs.ZipFileSystem [jdk.internal.loader.ClassLoaders$PlatformClassLoader@370cfad1, module jdk.zipfs, Thread[#29,pool-1-thread-1,5,main], loaded=false]
[info] single threaded compile completed. #file reads: 2 took 7063 ms
...
[info] Test demo.JarCacheAgentTest#compileWithoutCaching() started
...
[info] single threaded compile completed. #file reads: 1000000 took 43373 ms
...
```

## Implementation

The agent uses ByteBuddy to instrument the `FSInfo` class in the Java compiler implementation.

An alternative would be to use the non-public API for Javac which allows an instance of `FSInfo` to be provided.
Such an approach would need to be woven into each build tool (e.g. Zinc).

## Annotation Processors

To further reduce JAR reads, add this to `javacOptions` to disable a classpath scan for annotation processors.
As this can be configured through the normal option API of `javac`, this agent does not attempt to make it more
efficient.

```
--processor-module-path ""
``` 