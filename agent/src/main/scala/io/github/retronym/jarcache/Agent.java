package io.github.retronym.jarcache;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Agent {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        getTransform().installOn(instrumentation);
    }

    private static Pattern cacheableRegex;
    private static boolean DEBUG = Boolean.getBoolean("io.github.retronym.jarcache.debug");

    public static boolean isCacheable(Path path) {
        boolean result = isCacheableImpl(path);
        if (DEBUG) {
            if (result) {
                System.err.println("JARCACHE: Caching " + path);
            } else {
                System.err.println("JARCACHE: Not caching " + path);
            }
        }
        return result;
    }

    private static boolean isCacheableImpl(Path path) {
        String cacheableRegexString = System.getProperty("io.github.retronym.jarcache.cacheableRegex");
        Pattern cacheableRegex = Agent.cacheableRegex;
        if (cacheableRegexString == null || cacheableRegexString.isEmpty()) {
            Agent.cacheableRegex = null;
            return false;
        } else {
            if (cacheableRegex == null || Objects.equals(cacheableRegex.pattern(), cacheableRegexString)) {
                cacheableRegex = Pattern.compile(cacheableRegexString);
            }
            Agent.cacheableRegex = cacheableRegex;
            return cacheableRegex.matcher(path.toString()).matches();
        }
    }

    public static AgentBuilder.Identified.Extendable getTransform() {
        if (DEBUG)
            System.err.println("JARCACHE: Instrumenting with ByteBuddy");

        return new AgentBuilder.Default()
                .ignore(ElementMatchers.none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION) // Allow retransformation
                .with(DEBUG ? AgentBuilder.Listener.StreamWriting.toSystemError().withTransformationsOnly() : AgentBuilder.Listener.NoOp.INSTANCE)
                .with(DEBUG ? AgentBuilder.Listener.StreamWriting.toSystemError().withErrorsOnly() : AgentBuilder.Listener.NoOp.INSTANCE)
                .with(AgentBuilder.Listener.StreamWriting.toSystemOut().withErrorsOnly())
                .type(ElementMatchers.named("com.sun.tools.javac.file.FSInfo")).transform((builder, type, classLoader, module, protectionDomain) ->
                        builder)
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder
                                .method(ElementMatchers.named("getJarClassPath")).intercept(Advice.to(GetJarClassPathCachingAdvice.class))
                                .method(ElementMatchers.named("getJarFSProvider")).intercept(Advice.to(GetJarFSProviderAdvice.class))
                );
    }
}
