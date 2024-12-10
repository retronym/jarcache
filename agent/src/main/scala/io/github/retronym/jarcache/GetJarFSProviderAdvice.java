package io.github.retronym.jarcache;

import net.bytebuddy.asm.Advice;

import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides advice for the {@link com.sun.tools.javac.file.CacheFSInfo#getJarFSProvider()} method.
 *
 * It returns a singleton instance of {@link DeferredCloseJarFSProvider} which aggressively caches
 * the created {@link java.nio.file.FileSystem} instances.
 */
public class GetJarFSProviderAdvice {
    public static final ConcurrentHashMap<Path, Optional<List<Path>>> cache = new ConcurrentHashMap<>();
    public static final DeferredCloseJarFSProvider provider = new DeferredCloseJarFSProvider();

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, inline = false)
    public static FileSystemProvider enter() {
        return provider;
    }

    @SuppressWarnings("UnusedAssignment")
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Enter FileSystemProvider provider,
                            @Advice.Return(readOnly = false) FileSystemProvider returnValue) {
        returnValue = provider;
    }
}