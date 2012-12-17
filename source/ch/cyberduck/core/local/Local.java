package ch.cyberduck.core.local;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.PathFilter;
import ch.cyberduck.core.PathReference;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.io.RepeatableFileInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Comparator;

import com.ibm.icu.text.Normalizer;

/**
 * @version $Id$
 */
public abstract class Local extends AbstractPath {
    private static final Logger log = Logger.getLogger(Local.class);

    /**
     * Absolute path in local file system
     */
    private String path;

    /**
     * @param parent Parent directory
     * @param name   Filename
     */
    public Local(final Local parent, final String name) {
        this(parent.getAbsolute(), name);
    }

    /**
     * @param parent Parent directory
     * @param name   Filename
     */
    public Local(final String parent, final String name) {
        this.setPath(parent, name);
    }

    /**
     * @param path Absolute path
     */
    public Local(final String path) {
        this.setPath(path);
    }

    /**
     * @param path File reference
     */
    public Local(final File path) {
        try {
            this.setPath(path.getCanonicalPath());
        }
        catch(IOException e) {
            log.error(String.format("Error getting canonical path:%s", e.getMessage()));
            this.setPath(path.getAbsolutePath());
        }
    }

    @Override
    public Attributes attributes() {
        return new LocalAttributes(this.getAbsolute());
    }

    @Override
    public char getPathDelimiter() {
        return '/';
    }

    /**
     * Creates a new file and sets its resource fork to feature a custom progress icon
     */
    @Override
    public boolean touch() {
        try {
            final File file = new File(path);
            if(!file.getParentFile().mkdirs()) {
                log.debug(String.format("Create directory %s failed", path));
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

    @Override
    public void symlink(String target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mkdir() {
        if(!new File(path).mkdirs()) {
            log.debug(String.format("Create directory %s failed", path));
        }
    }

    /**
     * Delete the file
     */
    @Override
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
    public abstract void trash();

    @Override
    public AttributedList<Local> list() {
        final AttributedList<Local> children = new AttributedList<Local>();
        final File[] files = new File(path).listFiles();
        if(null == files) {
            log.error(String.format("Error listing children at %s", path));
            return children;
        }
        for(File file : files) {
            children.add(LocalFactory.createLocal(file));
        }
        return children;
    }

    @Override
    public AttributedList<Local> children() {
        return this.children(null);
    }

    @Override
    public AttributedList<Local> children(final PathFilter<? extends AbstractPath> filter) {
        return this.children(null, filter);
    }

    @Override
    public AttributedList<Local> children(final Comparator<? extends AbstractPath> comparator,
                                          final PathFilter<? extends AbstractPath> filter) {
        return this.list().filter(comparator, filter);
    }

    @Override
    public String getAbsolute() {
        return new File(path).getAbsolutePath();
    }

    /**
     * @return A shortened path representation.
     */
    public String getAbbreviatedPath() {
        return this.getAbsolute();
    }

    @Override
    public AbstractPath getSymlinkTarget() {
        try {
            return LocalFactory.createLocal(this, new File(path).getCanonicalPath());
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * @return The last path component.
     */
    @Override
    public String getName() {
        return new File(path).getName();
    }

    public Local getVolume() {
        return LocalFactory.createLocal(new File(String.valueOf(this.getPathDelimiter())));
    }

    @Override
    public AbstractPath getParent() {
        return LocalFactory.createLocal(new File(path).getParentFile());
    }

    @Override
    public PathReference getReference() {
        return new PathReference<Local>() {
            @Override
            public Local unique() {
                return Local.this;
            }
        };
    }

    /**
     * @return True if the path exists on the file system.
     */
    @Override
    public boolean exists() {
        return new File(path).exists();
    }

    @Override
    public void setPath(final String name) {
        String normalized = name;
        if(Preferences.instance().getBoolean("local.normalize.unicode")) {
            if(!Normalizer.isNormalized(normalized, Normalizer.NFC, Normalizer.UNICODE_3_2)) {
                // Canonical decomposition followed by canonical composition (default)
                normalized = Normalizer.normalize(name, Normalizer.NFC, Normalizer.UNICODE_3_2);
                if(log.isDebugEnabled()) {
                    log.debug("Normalized local path '" + name + "' to '" + normalized + "'");
                }
            }
        }
        path = normalized;
    }

    @Override
    public void writeTimestamp(final long created, final long modified, final long accessed) {
        if(modified < 0) {
            return;
        }
        if(!new File(path).setLastModified(modified)) {
            log.error(String.format("Write modification date failed for %s", path));
        }
    }

    @Override
    public void rename(final AbstractPath renamed) {
        if(!new File(path).renameTo(new File(renamed.getAbsolute()))) {
            log.error(String.format("Rename failed for %s", renamed.getAbsolute()));
        }
    }

    public void copy(final Local copy) {
        if(copy.equals(this)) {
            log.warn(String.format("%s and %s are identical. Not copied.", this.getName(), copy.getName()));
        }
        else {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(new File(path));
                out = new FileOutputStream(copy.getAbsolute());
                IOUtils.copy(in, out);
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    @Override
    public int hashCode() {
        return this.getAbsolute().hashCode();
    }

    /**
     * Compares the two files using their path with a string comparision ignoring case.
     * Implementations should override this depending on the case sensitivity of the file system.
     */
    @Override
    public boolean equals(Object o) {
        if(null == o) {
            return false;
        }
        if(o instanceof Local) {
            Local other = (Local) o;
            return this.getAbsolute().equalsIgnoreCase(other.getAbsolute());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getAbsolute();
    }

    @Override
    public String toURL() {
        try {
            return new File(path).toURI().toURL().toString();
        }
        catch(MalformedURLException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new RepeatableFileInputStream(new File(path));
    }

    public OutputStream getOutputStream(final boolean append) throws FileNotFoundException {
        return new FileOutputStream(new File(path), append);
    }
}