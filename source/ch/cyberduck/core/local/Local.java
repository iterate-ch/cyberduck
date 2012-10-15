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
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.RepeatableFileInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
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
     *
     */
    public class LocalPermission extends Permission {
        @Override
        public boolean isReadable() {
            return new File(path).canRead();
        }

        @Override
        public boolean isWritable() {
            return new File(path).canWrite();
        }

        @Override
        public boolean isExecutable() {
            return true;
        }

        @Override
        public String toString() {
            return Locale.localizedString("Unknown");
        }
    }

    /**
     *
     */
    public class LocalAttributes extends Attributes {
        @Override
        public long getModificationDate() {
            return new File(path).lastModified();
        }

        /**
         * @return The modification date instead.
         */
        @Override
        public long getCreationDate() {
            return this.getModificationDate();
        }

        /**
         * @return The modification date instead.
         */
        @Override
        public long getAccessedDate() {
            return this.getModificationDate();
        }

        /**
         * This is only returning the correct result if the file already exists.
         *
         * @return File type
         * @see Local#exists()
         */
        @Override
        public int getType() {
            final int t = this.isFile() ? FILE_TYPE : DIRECTORY_TYPE;
            if(this.isSymbolicLink()) {
                return t | SYMBOLIC_LINK_TYPE;
            }
            return t;
        }

        @Override
        public long getSize() {
            if(this.isDirectory()) {
                return -1;
            }
            return new File(path).length();
        }

        @Override
        public Permission getPermission() {
            return new LocalPermission();
        }

        @Override
        public boolean isVolume() {
            return null == new File(path).getParent();
        }

        /**
         * This is only returning the correct result if the file already exists.
         *
         * @see Local#exists()
         */
        @Override
        public boolean isDirectory() {
            return new File(path).isDirectory();
        }

        /**
         * This is only returning the correct result if the file already exists.
         *
         * @see Local#exists()
         */
        @Override
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
        @Override
        public boolean isSymbolicLink() {
            if(!Local.this.exists()) {
                return false;
            }
            // For a link that actually points to something (either a file or a directory),
            // the absolute path is the path through the link, whereas the canonical path
            // is the path the link references.
            try {
                return !new File(path).getAbsolutePath().equals(new File(path).getCanonicalPath());
            }
            catch(IOException e) {
                return false;
            }
        }

        /**
         * Calculate the MD5 sum as Hex-encoded string
         *
         * @return Null if failure
         */
        @Override
        public String getChecksum() {
            if(this.isFile()) {
                try {
                    return ServiceUtils.toHex(ServiceUtils.computeMD5Hash(Local.this.getInputStream()));
                }
                catch(NoSuchAlgorithmException e) {
                    log.error(String.format("MD5 failed for %s:%s", path, e.getMessage()));
                }
                catch(IOException e) {
                    log.error(String.format("MD5 failed for %s:%s", path, e.getMessage()));
                }
            }
            return null;
        }
    }

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
        return new LocalAttributes();
    }

    @Override
    public char getPathDelimiter() {
        return '/';
    }

    /**
     * Creates a new file and sets its resource fork to feature a custom progress icon
     */
    @Override
    public void touch() {
        try {
            if(!new File(path).createNewFile()) {
                log.warn(String.format("Create file %s failed", path));
            }
        }
        catch(IOException e) {
            log.error(String.format("Error creating new file %s", e.getMessage()));
        }
    }

    @Override
    public void symlink(String target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mkdir(boolean recursive) {
        if(recursive) {
            if(!new File(path).mkdirs()) {
                log.warn(String.format("Create directories %s failed", path));
            }
        }
        else {
            this.mkdir();
        }
    }

    @Override
    public void mkdir() {
        if(!new File(path).mkdir()) {
            log.warn(String.format("Create directory %s failed", path));
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

    /**
     * Reveal file in file system
     *
     * @return True if file could be selected
     */
    public abstract boolean reveal();

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

    /**
     * @return Human readable localized description of file type
     */
    @Override
    public String kind() {
        if(this.attributes().isDirectory()) {
            return Locale.localizedString("Folder");
        }
        final String extension = this.getExtension();
        if(StringUtils.isEmpty(extension)) {
            return Locale.localizedString("Unknown");
        }
        return Locale.localizedString("File");
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