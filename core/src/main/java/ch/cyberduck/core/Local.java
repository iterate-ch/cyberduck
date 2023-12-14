package ch.cyberduck.core;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LocalNotfoundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.local.TildeExpander;
import ch.cyberduck.core.local.WorkdirPrefixer;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.unicode.NFCNormalizer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Local extends AbstractPath implements Referenceable, Serializable {
    private static final Logger log = LogManager.getLogger(Local.class);

    /**
     * Absolute path in local file system
     */
    private String path;

    public Local(final String parent, final String name) {
        this(parent, name, PreferencesFactory.get().getProperty("local.delimiter"));
    }

    public Local(final String parent, final String name, final String delimiter) {
        this(parent.endsWith(delimiter) ?
                String.format("%s%s", parent, name) :
                String.format("%s%c%s", parent, CharUtils.toChar(delimiter), name));
    }

    public Local(final Local parent, final String name) {
        this(parent, name, PreferencesFactory.get().getProperty("local.delimiter"));
    }

    public Local(final Local parent, final String name, final String delimiter) {
        this(parent.isRoot() ?
                String.format("%s%s", parent.getAbsolute(), name) :
                String.format("%s%c%s", parent.getAbsolute(), CharUtils.toChar(delimiter), name));
    }

    /**
     * @param name Absolute path
     */
    public Local(final String name) {
        String path = name;
        if(PreferencesFactory.get().getBoolean("local.normalize.unicode")) {
            path = new NFCNormalizer().normalize(path).toString();
        }
        if(PreferencesFactory.get().getBoolean("local.normalize.tilde")) {
            path = new TildeExpander().expand(path);
        }
        if(PreferencesFactory.get().getBoolean("local.normalize.prefix")) {
            path = new WorkdirPrefixer().normalize(path);
        }
        try {
            this.path = Paths.get(path).toString();
        }
        catch(InvalidPathException e) {
            log.error(String.format("The name %s is not a valid path for the filesystem", path), e);
            this.path = path;
        }
    }

    @Override
    public <T> T serialize(final Serializer<T> dict) {
        dict.setStringForKey(path, "Path");
        return dict.getSerialized();
    }

    @Override
    public EnumSet<Type> getType() {
        final EnumSet<Type> set = EnumSet.noneOf(Type.class);
        if(this.isDirectory()) {
            set.add(Type.directory);
            if(this.isVolume()) {
                set.add(Type.volume);
            }
        }
        else {
            set.add(Type.file);
        }
        if(this.isSymbolicLink()) {
            set.add(Type.symboliclink);
        }
        return set;
    }

    public boolean isVolume() {
        return null == Paths.get(path).getParent();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see Local#exists()
     */
    public boolean isDirectory() {
        return Paths.get(path).toFile().isDirectory();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see Local#exists()
     */
    public boolean isFile() {
        return Paths.get(path).toFile().isFile();
    }

    /**
     * Checks whether a given file is a symbolic link.
     *
     * @return true if the file is a symbolic link.
     */
    public boolean isSymbolicLink() {
        return Files.isSymbolicLink(Paths.get(path));
    }

    public Local getSymlinkTarget() throws NotfoundException {
        try {
            try {
                Paths.get(path).toRealPath();
            }
            catch(NoSuchFileException ignore) {
                // Proceed if target is not found
            }
            catch(FileSystemException e) {
                // Too many levels of symbolic links
                log.warn(String.format("Failure resolving symlink target for %s. %s", path, e.getMessage()));
                throw new LocalNotfoundException(MessageFormat.format("Failure to read attributes of {0}", this.getName()), e);
            }
            // For a link that actually points to something (either a file or a directory),
            // the absolute path is the path through the link, whereas the canonical path
            // is the path the link references.
            final Path target = Files.readSymbolicLink(Paths.get(path));
            if(target.isAbsolute()) {
                return LocalFactory.get(target.toString());
            }
            else {
                return LocalFactory.get(this.getParent(), target.toString());
            }
        }
        catch(InvalidPathException | IOException e) {
            throw new LocalNotfoundException(MessageFormat.format("Failure to read attributes of {0}", this.getName()), e);
        }
    }

    public LocalAttributes attributes() {
        return new LocalAttributes(path);
    }

    @Override
    public char getDelimiter() {
        return CharUtils.toChar(PreferencesFactory.get().getProperty("local.delimiter"));
    }

    public void mkdir() throws AccessDeniedException {
        new DefaultLocalDirectoryFeature().mkdir(this);
    }

    /**
     * Delete the file
     */
    public void delete() throws AccessDeniedException, NotfoundException {
        try {
            Files.delete(Paths.get(path));
        }
        catch(NoSuchFileException e) {
            throw new LocalNotfoundException(String.format("Delete %s failed", path), e);
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(MessageFormat.format(
                    LocaleFactory.localizedString("Cannot delete {0}", "Error"), this.getName()), e);
        }
    }

    public AttributedList<Local> list(final Filter<String> filter) throws AccessDeniedException {
        return this.list(path, filter);
    }

    public AttributedList<Local> list(final String path, final Filter<String> filter) throws AccessDeniedException {
        final AttributedList<Local> children = new AttributedList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path), new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(final Path entry) {
                if(null == entry.getFileName()) {
                    return false;
                }
                return filter.accept(entry.getFileName().toString());
            }
        })) {
            for(Path entry : stream) {
                children.add(LocalFactory.get(entry.toString()));
            }
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(MessageFormat.format(
                    LocaleFactory.localizedString("Listing directory {0} failed", "Error"), this.getName()), e);
        }
        return children;
    }

    public AttributedList<Local> list() throws AccessDeniedException {
        return this.list(new NullFilter<>());
    }

    @Override
    public String getAbsolute() {
        return path;
    }

    /**
     * @return Security scoped bookmark outside of sandbox to store in preferences
     */
    public String getBookmark() {
        return path;
    }

    public void setBookmark(final String data) {
        //
    }

    public Local withBookmark(final String data) {
        this.setBookmark(data);
        return this;
    }

    /**
     * @return A shortened path representation.
     */
    public String getAbbreviatedPath() {
        return new TildeExpander().abbreviate(path);
    }

    /**
     * Subclasses may override to return a user friendly representation of the name denoting this path.
     *
     * @return Name of the file
     * @see #getName()
     */
    public String getDisplayName() {
        return this.getName();
    }

    /**
     * @return The last path component.
     */
    @Override
    public String getName() {
        final char delimiter = this.getDelimiter();
        if(String.valueOf(delimiter).equals(path)) {
            return path;
        }
        if(!StringUtils.contains(path, delimiter)) {
            return path;
        }
        return StringUtils.substringAfterLast(path, String.valueOf(delimiter));
    }

    public Local getVolume() {
        return LocalFactory.get(String.valueOf(this.getDelimiter()));
    }

    public Local getParent() {
        if(this.isVolume()) {
            return this;
        }
        return LocalFactory.get(Paths.get(path).getParent().toString());
    }

    /**
     * Does not follow symlinks. Can be expensive if called many times due to symlink check.
     *
     * @return True if the path exists on the file system.
     * @see <a href="https://rules.sonarsource.com/java/tag/performance/RSPEC-3725"/>
     */
    public boolean exists() {
        return this.exists(LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * @return True if the path exists on the file system.
     */
    public boolean exists(LinkOption... options) {
        if(options.length == 0) {
            return Paths.get(path).toFile().exists();
        }
        return Files.exists(Paths.get(path), options);
    }

    public void rename(final Local renamed) throws AccessDeniedException {
        try {
            try {
                Files.move(Paths.get(path), Paths.get(renamed.getAbsolute()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            // Catch generic exception due to a bug in IKVM moving files containing special characters
            // in conjunction with the atomic move option
            catch(FileSystemException e) {
                // Copying file to different disk is not possible with atomic move.
                // Moving directory to an already existing target will throw exists exception with atomic move flag.
                Files.move(Paths.get(path), Paths.get(renamed.getAbsolute()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(MessageFormat.format(
                    LocaleFactory.localizedString("Cannot rename {0}", "Error"), this.getName()), e);
        }
        path = renamed.getAbsolute();
    }

    public void copy(final Local copy) throws AccessDeniedException {
        this.copy(copy, new CopyOptions());
    }

    public void copy(final Local copy, final CopyOptions options) throws AccessDeniedException {
        if(copy.equals(this)) {
            log.warn(String.format("%s and %s are identical. Not copied.", this.getName(), copy.getName()));
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Copy to %s with options %s", copy, options));
            }
            FileChannel in = null;
            FileChannel out = null;
            try {
                in = getReadChannel(this.path);
                out = getWriteChannel(copy.path, options.append, !copy.exists());
                out.transferFrom(in, out.size(), in.size());
            }
            catch(IOException e) {
                throw new LocalAccessDeniedException(MessageFormat.format(
                        LocaleFactory.localizedString("Cannot copy {0}", "Error"), this.getName()), e);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    public static final class CopyOptions {
        public boolean append;

        public CopyOptions append(final boolean append) {
            this.append = append;
            return this;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CopyOptions{");
            sb.append("append=").append(append);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Local)) {
            return false;
        }
        final Local local = (Local) o;
        return Objects.equals(path, local.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public String toURL() {
        return String.format("file:%s", path);
    }


    public InputStream getInputStream() throws AccessDeniedException {
        return getInputStream(path);
    }

    protected InputStream getInputStream(final String path) throws AccessDeniedException {
        try {
            return new SeekableByteChannelInputStream(getReadChannel(path));
        }
        catch(RuntimeException e) {
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }

    private static FileChannel getReadChannel(final String path) throws LocalAccessDeniedException {
        try {
            return FileChannel.open(Paths.get(path), StandardOpenOption.READ);
        }
        catch(RuntimeException | IOException e) {
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }

    protected OutputStream getOutputStream(final String path, final boolean append) throws AccessDeniedException {
        return Channels.newOutputStream(getWriteChannel(path, append, !this.exists()));
    }

    public OutputStream getOutputStream(final boolean append) throws AccessDeniedException {
        return Channels.newOutputStream(getWriteChannel(path, append, !this.exists()));
    }

    private static FileChannel getWriteChannel(final String path, final boolean append, final boolean create) throws LocalAccessDeniedException {
        try {
            final Set<OpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.WRITE);
            if(create) {
                options.add(StandardOpenOption.CREATE);
            }
            if(append) {
                options.add(StandardOpenOption.APPEND);
            }
            else {
                options.add(StandardOpenOption.TRUNCATE_EXISTING);
            }
            return FileChannel.open(Paths.get(path), options);
        }
        catch(RuntimeException | IOException e) {
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }

    public Object lock(final boolean interactive) throws AccessDeniedException {
        if(log.isWarnEnabled()) {
            log.warn(String.format("No lock support in %s", this));
        }
        return null;
    }

    public void release(Object lock) {
        //
    }

    /**
     * @param directory Parent directory
     * @return True if this is a child in the path hierarchy of the argument passed
     */
    public boolean isChild(final Local directory) {
        if(this.isRoot()) {
            // Root cannot be a child of any other path
            return false;
        }
        if(Objects.equals(this.parent(this.getAbsolute()), this.parent(directory.getAbsolute()))) {
            // Cannot be a child if the same parent
            return false;
        }
        final String prefix = FilenameUtils.getPrefix(this.getAbsolute());
        String parent = this.getAbsolute();
        while(!parent.equals(prefix)) {
            parent = this.parent(parent);
            if(directory.getAbsolute().equals(parent)) {
                return true;
            }
        }
        return false;
    }

    private String parent(final String absolute) {
        final String prefix = FilenameUtils.getPrefix(absolute);
        if(absolute.equals(prefix)) {
            return null;
        }
        int index = absolute.length() - 1;
        if(absolute.charAt(index) == this.getDelimiter()) {
            if(index > 0) {
                index--;
            }
        }
        final int cut = absolute.lastIndexOf(this.getDelimiter(), index);
        if(cut > FilenameUtils.getPrefixLength(absolute)) {
            return absolute.substring(0, cut);
        }
        return String.valueOf(prefix);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Local{");
        sb.append("path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private static final class SeekableByteChannelInputStream extends InputStream {
        private final SeekableByteChannel channel;
        private long markPosition = 0L;

        public SeekableByteChannelInputStream(final SeekableByteChannel channel) {
            this.channel = channel;
        }

        @Override
        public int read() throws IOException {
            final ByteBuffer buffer = ByteBuffer.wrap(new byte[1]);
            final int bytesRead = channel.read(buffer);
            if(bytesRead > 0) {
                buffer.position(0);
                return Byte.toUnsignedInt(buffer.get());
            }
            else {
                return -1;
            }
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            final ByteBuffer buffer = ByteBuffer.wrap(b);
            buffer.position(off);
            buffer.limit(off + len);
            final int bytesRead = channel.read(buffer);
            if(bytesRead > 0) {
                return bytesRead;
            }
            else {
                return -1;
            }
        }

        @Override
        public long skip(final long n) throws IOException {
            channel.position(channel.position() + n);
            return channel.position();
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(final int readlimit) {
            try {
                markPosition = channel.position();
            }
            catch(final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void reset() throws IOException {
            channel.position(markPosition);
            markPosition = 0;
        }

        @Override
        public void close() throws IOException {
            channel.close();
        }
    }
}
