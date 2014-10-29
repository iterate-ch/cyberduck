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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.LocalRepeatableFileInputStream;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.unicode.NFCNormalizer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * @version $Id$
 */
public class Local extends AbstractPath implements Referenceable, Serializable {
    private static final Logger log = Logger.getLogger(Local.class);

    /**
     * Absolute path in local file system
     */
    protected String path;

    private LocalAttributes attributes;

    public Local(final String parent, final String name) {
        this(parent.endsWith(Preferences.instance().getProperty("local.delimiter")) ?
                String.format("%s%s", parent, name) :
                String.format("%s%c%s", parent, CharUtils.toChar(Preferences.instance().getProperty("local.delimiter")), name));
    }

    public Local(final Local parent, final String name) {
        this(parent.isRoot() ?
                String.format("%s%s", parent.getAbsolute(), name) :
                String.format("%s%c%s", parent.getAbsolute(), CharUtils.toChar(Preferences.instance().getProperty("local.delimiter")), name));
    }

    /**
     * @param name Absolute path
     */
    public Local(final String name) {
        if(Preferences.instance().getBoolean("local.normalize.unicode")) {
            path = new NFCNormalizer().normalize(name);
        }
        else {
            path = name;
        }
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
        return null == new File(path).getParent();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see Local#exists()
     */
    public boolean isDirectory() {
        return new File(path).isDirectory();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see Local#exists()
     */
    public boolean isFile() {
        return new File(path).isFile();
    }

    /**
     * Checks whether a given file is a symbolic link.
     * <p/>
     * <p>It doesn't really test for symbolic links but whether the
     * canonical and absolute paths of the file are identical - this
     * may lead to false positives on some platforms.</p>
     *
     * @return true if the file is a symbolic link.
     */
    public boolean isSymbolicLink() {
        final File f = new File(path);
        if(!f.exists()) {
            return false;
        }
        // For a link that actually points to something (either a file or a directory),
        // the absolute path is the path through the link, whereas the canonical path
        // is the path the link references.
        try {
            return !f.getAbsolutePath().equals(f.getCanonicalPath());
        }
        catch(IOException e) {
            return false;
        }
    }

    public LocalAttributes attributes() {
        return attributes;
    }

    @Override
    public char getDelimiter() {
        return CharUtils.toChar(Preferences.instance().getProperty("local.delimiter"));
    }

    public void mkdir() throws AccessDeniedException {
        final File file = new File(path);
        if(file.exists()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Directory %s already exists", path));
            }
            return;
        }
        if(!file.mkdirs()) {
            throw new AccessDeniedException(String.format("Create directory %s failed", path));
        }
    }

    /**
     * Delete the file
     */
    public void delete() throws AccessDeniedException {
        final File file = new File(path);
        if(!file.exists()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("File %s does not exists", path));
            }
            return;
        }
        if(!file.delete()) {
            throw new AccessDeniedException(String.format("Delete %s failed", path));
        }
    }

    /**
     * Delete file
     *
     * @param deferred On application quit
     */
    public void delete(boolean deferred) throws AccessDeniedException {
        if(deferred) {
            new File(path).deleteOnExit();
        }
        else {
            this.delete();
        }
    }

    public AttributedList<Local> list(final Filter<String> filter) throws AccessDeniedException {
        final AttributedList<Local> children = new AttributedList<Local>();
        final File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return filter.accept(name);
            }
        });
        if(null == files) {
            throw new AccessDeniedException(String.format("Error listing files in directory %s", path));
        }
        for(File file : files) {
            children.add(LocalFactory.get(file.getAbsolutePath()));
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
        final String abb = StringUtils.removeStart(path, Preferences.instance().getProperty("local.user.home"));
        if(StringUtils.equals(abb, path)) {
            return path;
        }
        return String.format("%s%s", HOME, abb);
    }

    public Local getSymlinkTarget() throws NotfoundException {
        try {
            return LocalFactory.get(this, new File(path).getCanonicalPath());
        }
        catch(IOException e) {
            throw new NotfoundException(String.format("Resolving symlink target for %s failed", path), e);
        }
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
        return LocalFactory.get(new File(path).getParentFile().getAbsolutePath());
    }

    @Override
    public PathReference getReference() {
        return new PathReference<Local>() {
            @Override
            public Local unique() {
                return Local.this;
            }

            @Override
            public String attributes() {
                return StringUtils.EMPTY;
            }
        };
    }

    /**
     * @return True if the path exists on the file system.
     */
    public boolean exists() {
        return new File(path).exists();
    }

    public void rename(final Local renamed) throws AccessDeniedException {
        if(!new File(path).renameTo(new File(renamed.getAbsolute()))) {
            throw new AccessDeniedException(String.format("Rename failed for %s", renamed));
        }
    }

    public void copy(final Local copy) throws AccessDeniedException {
        if(copy.equals(this)) {
            log.warn(String.format("%s and %s are identical. Not copied.", this.getName(), copy.getName()));
        }
        else {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = this.getInputStream();
                out = copy.getOutputStream(false);
                IOUtils.copy(in, out);
            }
            catch(IOException e) {
                throw new AccessDeniedException(e.getMessage(), e);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
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
            throw new AccessDeniedException(e.getMessage(), e);
        }
    }

    public OutputStream getOutputStream(final boolean append) throws AccessDeniedException {
        try {
            return new FileOutputStream(new File(path), append);
        }
        catch(FileNotFoundException e) {
            throw new AccessDeniedException(e.getMessage(), e);
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
        if(ObjectUtils.equals(PathNormalizer.parent(this.getAbsolute(), this.getDelimiter()), PathNormalizer.parent(directory.getAbsolute(), this.getDelimiter()))) {
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