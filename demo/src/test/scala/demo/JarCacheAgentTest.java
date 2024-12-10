package demo;

import jdk.jfr.consumer.RecordedEvent;
import io.github.retronym.jarcache.Agent;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.*;

import org.moditect.jfrunit.events.*;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static java.util.stream.Collectors.*;

@JfrEventTest
public class JarCacheAgentTest {
    public static final String SOURCE_CODE = """
                package demo;
                import p1.*;
                public class HelloWorld {
                    public static void main(String[] args) {
                        System.out.println("Hello, Custom Classpath!");
                    }
                }
            """;
    public JfrEvents jfrEvents = new JfrEvents();

    @Test
    @EnableEvent(value = FileRead.EVENT_NAME, stackTrace = EnableEvent.StacktracePolicy.INCLUDED)
    public void compileWithoutCaching() throws Exception {
        Agent.setCacheableRegex(null);
        testImpl();
    }

    @Test
    @EnableEvent(value = FileRead.EVENT_NAME, stackTrace = EnableEvent.StacktracePolicy.INCLUDED)
    public void compileWithCaching() throws Exception {
        Agent.setCacheableRegex(".*/compileTest.*");

        testImpl();
    }

    private void testImpl() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ByteBuddyAgent.install();
        Agent.installDynamically();

        JavaFileObject javaFileObject = new InMemoryJavaFileObject("HelloWorld", SOURCE_CODE);
        Path tempDir = Files.createTempDirectory("compileTest");
        for (int i = 0; i < 1000; i++) {
            createClasspathJAR(tempDir, i);
        }
        try {
            Iterable<String> options = Arrays.asList(
                    "--processor-module-path", "",
                    "-classpath", Files.list(tempDir).map(Path::toAbsolutePath).map(Path::toString).collect(joining(File.pathSeparator)));

            Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObject);
            compile(compiler, options, compilationUnits);
            jfrEvents.reset();

            Timer timer = new Timer();
            int N = 200;
            for (int i = 0; i < N; i++) {
                compile(compiler, options, compilationUnits);
            }
            System.out.println("\n");

            List<RecordedEvent> readEvents = jfrEvents.events().filter(event -> event.getEventType().getName().equals(FileRead.EVENT_NAME)).toList();
            timer.print("single threaded compile completed. " + "#file reads: " + readEvents.size());
        } finally {
            cleanup(tempDir);
        }
    }

    private static void cleanup(Path tempDir) throws IOException {
        Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach(JarCacheAgentTest::deleteQuietly);
    }

    private static Path createClasspathJAR(Path tempDir, int i) throws IOException {
        var tempJar = Files.createTempFile(tempDir, "file" + i, ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(tempJar))) {
            jar.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            byte[] bytes = "\n".repeat(10000).getBytes();
            jar.write(bytes);
            jar.closeEntry();
            jar.putNextEntry(new JarEntry("p1/C" + i + ".class"));
            jar.write(new byte[0]);
        }
        return tempJar;
    }

    private void compile(JavaCompiler compiler, Iterable<String> options, Iterable<? extends JavaFileObject> compilationUnits) {
        JavaCompiler.CompilationTask task1 = compiler.getTask(null, null, null, options, null, compilationUnits);
        compile(task1);
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compile(JavaCompiler.CompilationTask compilationTask) {
        boolean success = compilationTask.call();

        if (!success) {
            throw new RuntimeException("Compilation failed.");
        }
    }
}
