package io.github.retronym.jarcache;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static net.bytebuddy.agent.builder.AgentBuilder.Listener.StreamWriting.*;

public final class Agent {
    public static final String DEBUG_KEY = "io.github.retronym.jarcache.debug";
    public static final String CACHABLE_REGEX_KEY = "io.github.retronym.jarcache.cacheableRegex";
    private static boolean DEBUG = Boolean.getBoolean(DEBUG_KEY);

    static {
        setCacheableRegex(System.getProperty(CACHABLE_REGEX_KEY));
    }

    private static volatile Pattern cacheableRegex;

    public static void premain(String agentArgs, Instrumentation inst) {
        transform().installOn(inst);
    }

    public static void installDynamically() {
        transform().installOnByteBuddyAgent();
    }

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

    public static void setCacheableRegex(String cacheableRegexString) {
        if (cacheableRegexString == null) {
            cacheableRegex = null;
            return;
        }
        cacheableRegex = Pattern.compile(cacheableRegexString);
    }

    private static boolean isCacheableImpl(Path path) {
        return cacheableRegex != null && cacheableRegex.matcher(path.toString()).matches();
    }

    private static AgentBuilder.Identified.Extendable transform() {
        if (DEBUG)
            System.err.println("JARCACHE: Instrumenting with ByteBuddy");

        return new AgentBuilder.Default()
                .ignore(ElementMatchers.none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION) // Allow retransformation
                .with(DEBUG ? toSystemError().withTransformationsOnly() : AgentBuilder.Listener.NoOp.INSTANCE)
                .with(DEBUG ? toSystemError().withErrorsOnly() : AgentBuilder.Listener.NoOp.INSTANCE)
                .type(ElementMatchers.named("com.sun.tools.javac.file.FSInfo")).transform((builder, type, classLoader, module, protectionDomain) ->
                        builder)
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder
                                .method(ElementMatchers.named("getJarClassPath")).intercept(Advice.to(GetJarClassPathCachingAdvice.class))
                                .method(ElementMatchers.named("getJarFSProvider")).intercept(Advice.to(GetJarFSProviderAdvice.class))
                );
    }
}
