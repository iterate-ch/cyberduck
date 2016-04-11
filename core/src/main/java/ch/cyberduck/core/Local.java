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
import ch.cyberduck.core.io.LocalRepeatableFileInputStream;
import ch.cyberduck.core.local.TildeExpander;
import ch.cyberduck.core.local.WorkdirPrefixer;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.unicode.NFCNormalizer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Objects;

/**
 * @version $Id$
 */
public class Local extends AbstractPath implements Referenceable, Serializable {
    private static final Logger log = Logger.getLogger(Local.class);

    private static final boolean NORMALIZE_UNICODE = PreferencesFactory.get().getBoolean("local.normalize.unicode");
    private static final boolean NORMALIZE_TILDE = PreferencesFactory.get().getBoolean("local.normalize.tilde");
    private static final boolean NORMALIZE_PREFIX = PreferencesFactory.get().getBoolean("local.normalize.prefix");
    private static final String DELIMITER = PreferencesFactory.get().getProperty("local.delimiter");

    /**
     * Absolute path in local file system
     */
    protected final String path;

    private final LocalAttributes attributes;

    public Local(final String parent, final String name) throws LocalAccessDeniedException {
        this(parent.endsWith(DELIMITER) ?
                String.format("%s%s", parent, name) :
                String.format("%s%c%s", parent, CharUtils.toChar(DELIMITER), name));
    }

    public Local(final Local parent, final String name) throws LocalAccessDeniedException {
        this(parent.isRoot() ?
                String.format("%s%s", parent.getAbsolute(), name) :
                String.format("%s%c%s", parent.getAbsolute(), CharUtils.toChar(DELIMITER), name));
    }

    /**
     * @param name Absolute path
     */
    public Local(final String name) throws LocalAccessDeniedException {
        String path = name;
        if(NORMALIZE_UNICODE) {
            path = new NFCNormalizer().normalize(path);
        }
        if(NORMALIZE_TILDE) {
            path = new TildeExpander().expand(path);
        }
        if(NORMALIZE_PREFIX) {
            path = new WorkdirPrefixer().normalize(path);
        }
        try {
            Paths.get(path);
        }
        catch(InvalidPathException e) {
            throw new LocalAccessDeniedException(String.format("The name %s is not a valid path for the filesystem", path), e);
        }
        this.path = path;
        this.attributes = new LocalAttributes(path);
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(path, "Path");
        return dict.getSerialized();
    }

    @Override
    public EnumSet<Type> getType() {
        final EnumSet<Type> set = EnumSet.noneOf(Type.class);
        if(this.isFile()) {
            set.add(Type.file);
        }
        if(this.isDirectory()) {
            set.add(Type.directory);
        }
        if(this.isVolume()) {
            set.add(Type.volume);
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
        return Files.isDirectory(Paths.get(path));
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see Local#exists()
     */
    public boolean isFile() {
        return Files.isRegularFile(Paths.get(path));
    }

    /**
     * Checks whether a given file is a symbolic link.
     *
     * @return true if the file is a symbolic link.
     */
    public boolean isSymbolicLink() {
        return Files.isSymbolicLink(Paths.get(path));
    }

    public Local getSymlinkTarget() throws NotfoundException, LocalAccessDeniedException {
        try {
            // For a link that actually points to something (either a file or a directory),
            // the absolute path is the path through the link, whereas the canonical path
            // is the path the link references.
            return LocalFactory.get(Files.readSymbolicLink(Paths.get(path)).toAbsolutePath().toString());
        }
        catch(InvalidPathException | IOException e) {
            throw new LocalNotfoundException(String.format("Resolving symlink target for %s failed", path), e);
        }
    }

    public LocalAttributes attributes() {
        return attributes;
    }

    @Override
    public char getDelimiter() {
        return CharUtils.toChar(DELIMITER);
    }

    public void mkdir() throws AccessDeniedException {
        try {
            Files.createDirectories(Paths.get(path));
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(MessageFormat.format(LocaleFactory.localizedString(
                    "Cannot create folder {0}", "Error"), path), e);
        }
    }

    /**
     * Delete the file
     */
    public void delete() throws AccessDeniedException {
        try {
            Files.deleteIfExists(Paths.get(path));
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Delete %s failed", path), e);
        }
    }

    /**
     * Delete file
     *
     * @param deferred On application quit
     */
    public void delete(boolean deferred) throws AccessDeniedException {
        this.delete();
    }

    public AttributedList<Local> list(final Filter<String> filter) throws AccessDeniedException {
        final AttributedList<Local> children = new AttributedList<Local>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path), new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(final Path entry) throws IOException {
                return filter.accept(entry.getFileName().toString());
            }
        })) {
            for(Path entry : stream) {
                children.add(LocalFactory.get(entry.toString()));
            }
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Error listing files in directory %s", path), e);
        }
        return children;
    }

    public AttributedList<Local> list() throws AccessDeniedException {
        return this.list(new Filter<String>() {
            @Override
            public boolean accept(final String file) {
                return true;
            }
        });
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
        return FilenameUtils.getName(path);
    }

    public Local getVolume() {
        return LocalFactory.get(String.valueOf(this.getDelimiter()));
    }

    public Local getParent() {
        return LocalFactory.get(Paths.get(path).getParent().toString());
    }

    /**
     * @return True if the path exists on the file system.
     */
    public boolean exists() {
        return Files.exists(Paths.get(path));
    }

    public void rename(final Local renamed) throws AccessDeniedException {
        try {
            Files.move(Paths.get(path), Paths.get(renamed.getAbsolute()));
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Rename failed for %s", renamed), e);
        }
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
            InputStream in = null;
            OutputStream out = null;
            try {
                in = this.getInputStream();
                out = copy.getOutputStream(options.append);
                IOUtils.copy(in, out);
            }
            catch(IOException e) {
                throw new LocalAccessDeniedException(e.getMessage(), e);
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

    /**
     * Compares the two files using their path with a string comparision ignoring case.
     * Implementations should override this depending on the case sensitivity of the file system.
     */
    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Local)) {
            return false;
        }
        final Local local = (Local) o;
        if(path != null ? !path.equalsIgnoreCase(local.path) : local.path != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? StringUtils.lowerCase(path).hashCode() : 0;
    }

    public String toURL() {
        return String.format("file:%s", path);
    }

    public InputStream getInputStream() throws AccessDeniedException {
        try {
            return new LocalRepeatableFileInputStream(new File(path));
        }
        catch(FileNotFoundException e) {
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }

    public OutputStream getOutputStream(final boolean append) throws AccessDeniedException {
        try {
            return new FileOutputStream(new File(path), append);
        }
        catch(FileNotFoundException e) {
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }

    public Object lock() throws AccessDeniedException {
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
        if(directory.isFile()) {
            // If a file we don't have any children at all
            return false;
        }
        if(this.isRoot()) {
            // Root cannot be a child of any other path
            return false;
        }
        if(directory.isRoot()) {
            // Any other path is a child
            return true;
        }
        if(Objects.equals(PathNormalizer.parent(this.getAbsolute(), this.getDelimiter()), PathNormalizer.parent(directory.getAbsolute(), this.getDelimiter()))) {
            // Cannot be a child if the same parent
            return false;
        }
        for(String parent = PathNormalizer.parent(this.getAbsolute(), this.getDelimiter()); !parent.equals(String.valueOf(this.getDelimiter())); parent = PathNormalizer.parent(parent, this.getDelimiter())) {
            if(parent.equals(directory.getAbsolute())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Local{");
        sb.append("path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }
}