package io.github.retronym.jarcache;

import net.bytebuddy.asm.Advice;

import java.nio.file.spi.FileSystemProvider;

/**
 * This class provides advice for the {@link com.sun.tools.javac.file.CacheFSInfo#getJarFSProvider()} method.
 *
 * It returns a singleton instance of {@link CachingJarFSProvider} which aggressively caches
 * the created {@link java.nio.file.FileSystem} instances.
 */
public class GetJarFSProviderAdvice {
    public static final CachingJarFSProvider provider = new CachingJarFSProvider();

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