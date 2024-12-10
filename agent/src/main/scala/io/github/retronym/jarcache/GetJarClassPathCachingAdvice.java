package io.github.retronym.jarcache;

import net.bytebuddy.asm.Advice;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides advice for the {@link com.sun.tools.javac.file.CacheFSInfo#getJarClassPath(Path)} method.
 * <p>
 * It caches the result of the method to avoid repeated calls.
 */
public class GetJarClassPathCachingAdvice {
    public static final ConcurrentHashMap<Path, Optional<List<Path>>> cache = new ConcurrentHashMap<>();

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static List<Path> enter(@Advice.Argument(0) Path path) {
        return enterImpl(path);
    }

    public static List<Path> enterImpl(Path path) {
        if (Agent.isCacheable(path)) {
            Optional<List<Path>> paths = cache.get(path);
            if (paths == null || paths.isEmpty()) {
                return null;
            } else {
                return paths.get();
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("UnusedAssignment")
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Enter List<Path> cachedValue,
                            @Advice.Argument(0) Path input,
                            @Advice.Return(readOnly = false) List<Path> returnValue) {
        if (cachedValue != null) {
            returnValue = cachedValue;
        } else {
            cache.put(input, Optional.ofNullable(returnValue));
        }
    }
}