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
import ch.cyberduck.core.io.LocalRepeatableFileInputStream;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ibm.icu.text.Normalizer;

/**
 * @version $Id$
 */
public abstract class Local extends AbstractPath implements Serializable {
    private static final Logger log = Logger.getLogger(Local.class);

    /**
     * Absolute path in local file system
     */
    protected String path;

    private LocalAttributes attributes;

    /**
     * @param name Absolute path
     */
    public Local(final String name) {
        this.setPath(name);
    }

    public <T> Local(final T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        this.setPath(dict.stringForKey("Path"));
    }

    protected void setPath(final String name) {
        if(Preferences.instance().getBoolean("local.normalize.unicode")) {
            if(!Normalizer.isNormalized(name, Normalizer.NFC, Normalizer.UNICODE_3_2)) {
                // Canonical decomposition followed by canonical composition (default)
                path = Normalizer.normalize(name, Normalizer.NFC, Normalizer.UNICODE_3_2);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Normalized local path %s to %s", name, path));
                }
            }
            else {
                path = name;
            }
        }
        else {
            path = name;
        }
        this.attributes = new LocalAttributes(path);
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(this.getAbsolute(), "Path");
        return dict.getSerialized();
    }

    @Override
    public LocalAttributes attributes() {
        return attributes;
    }

    @Override
    public char getDelimiter() {
        return '/';
    }

    /**
     * Creates a new file and sets its resource fork to feature a custom progress icon
     */
    public boolean touch() {
        try {
            final File file = new File(path);
            final File parent = file.getParentFile();
            if(!parent.mkdirs()) {
                log.debug(String.format("Create directory %s failed", parent));
            }
            if(!file.createNewFile()) {
                log.warn(String.format("Create file %s failed", path));
                return false;
            }
            return true;
        }
        catch(IOException e) {
            log.error(String.format("Error creating new file %s", e.getMessage()));
            return false;
        }
    }

    public void symlink(String target) {
        //
    }

    public void mkdir() {
        if(!new File(path).mkdirs()) {
            log.debug(String.format("Create directory %s failed", path));
        }
    }

    /**
     * Delete the file
     */
    public void delete() {
        if(!new File(path).delete()) {
            log.warn(String.format("Delete %s failed", path));
        }
    }

    /**
     * Delete file
     *
     * @param deferred On application quit
     */
    public void delete(boolean deferred) {
        if(deferred) {
            new File(path).deleteOnExit();
        }
        else {
            this.delete();
        }
    }

    /**
     * Move file to trash.
     */
    public void trash() {
        this.delete();
    }

    public AttributedList<Local> list() throws AccessDeniedException {
        final AttributedList<Local> children = new AttributedList<Local>();
        final File[] files = new File(path).listFiles();
        if(null == files) {
            log.error(String.format("Error listing children for %s", path));
            throw new AccessDeniedException(String.format("Error listing files in directory %s", path));
        }
        for(File file : files) {
            children.add(LocalFactory.createLocal(file.getAbsolutePath()));
        }
        return children;
    }

    @Override
    public String getAbsolute() {
        return path;
    }

    /**
     * @return Security scoped bookmark outside of sandbox to store in preferences
     */
    public String getBookmark() {
        return this.getAbsolute();
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
        return this.getAbsolute();
    }

    public Local getSymlinkTarget() {
        try {
            return LocalFactory.createLocal(this, new File(path).getCanonicalPath());
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        return null;
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
        return LocalFactory.createLocal(String.valueOf(this.getDelimiter()));
    }

    public Local getParent() {
        return LocalFactory.createLocal(new File(path).getParentFile().getAbsolutePath());
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

    public void writeUnixPermission(final Permission permission) {
        //
    }

    public void writeTimestamp(final long created, final long modified, final long accessed) {
        if(modified < 0) {
            return;
        }
        if(!new File(path).setLastModified(modified)) {
            log.error(String.format("Write modification date failed for %s", path));
        }
    }

    public void rename(final Local renamed) {
        if(!new File(path).renameTo(new File(renamed.getAbsolute()))) {
            log.error(String.format("Rename failed for %s", renamed));
        }
    }

    public void copy(final Local copy) {
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
                log.error(e.getMessage());
            }
            catch(AccessDeniedException e) {
                log.error(e.getMessage());
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
        return String.format("file:%s", this.getAbsolute());
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
        if(directory.attributes().isFile()) {
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