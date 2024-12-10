package io.github.retronym.jarcache;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * This class provides a {@link FileSystemProvider} that wraps the default {@link FileSystemProvider}
 * and caches the created {@link FileSystem} instances.
 */
public class DeferredCloseJarFSProvider extends FileSystemProvider {
    private static final FileSystemProvider delegate = lookupDelegate();
    private record CacheKey(Path path, Map<String, ?> env) {}
    static final ConcurrentHashMap<CacheKey, FileSystem> cache = new ConcurrentHashMap<>();

    private static FileSystemProvider lookupDelegate() {
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (provider.getScheme().equals("jar")) {
                return provider;
            }
        }
        return null;
    }

    @Override
    public String getScheme() {
        return delegate.getScheme();
    }

    public FileSystem newFileSystem(Path path, Map<String,?> env) throws IOException {
        if (Agent.isCacheable(path)) {
            CacheKey key = new CacheKey(path, env);
            FileSystem fs = cache.computeIfAbsent(key,
                    k -> new DeferredCloseZipFileSystem(this, delegateNewFileSystem(path, env)));
            return fs;
        } else {
            return delegateNewFileSystem(path, env);
        }
    }

    private static FileSystem delegateNewFileSystem(Path path, Map<String, ?> env) {
        try {
            return delegate.newFileSystem(path, env);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        return delegate.newFileSystem(uri, env);
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return delegate.getFileSystem(uri);
    }

    @Override
    public Path getPath(URI uri) {
        return delegate.getPath(uri);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return delegate.newByteChannel(path, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return delegate.newDirectoryStream(dir, filter);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        delegate.createDirectory(dir, attrs);

    }

    @Override
    public void delete(Path path) throws IOException {
        delegate.delete(path);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        delegate.copy(source, target, options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        delegate.move(source, target, options);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return delegate.isSameFile(path, path2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return delegate.isHidden(path);
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return delegate.getFileStore(path);
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        delegate.checkAccess(path, modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return delegate.getFileAttributeView(path, type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        return delegate.readAttributes(path, type, options);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return delegate.readAttributes(path, attributes, options);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        delegate.setAttribute(path, attribute, value, options);
    }
}
