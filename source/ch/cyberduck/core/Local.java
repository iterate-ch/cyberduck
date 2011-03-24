package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.RepeatableFileInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;

import com.ibm.icu.text.Normalizer;

import java.io.*;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class Local extends AbstractPath {
    private static Logger log = Logger.getLogger(Local.class);

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
         * @return
         * @see ch.cyberduck.core.Local#exists()
         */
        @Override
        public int getType() {
            final int t = this.isFile() ? AbstractPath.FILE_TYPE : AbstractPath.DIRECTORY_TYPE;
            if(this.isSymbolicLink()) {
                return t | AbstractPath.SYMBOLIC_LINK_TYPE;
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
         * @return
         * @see ch.cyberduck.core.Local#exists()
         */
        @Override
        public boolean isDirectory() {
            return new File(path).isDirectory();
        }

        /**
         * This is only returning the correct result if the file already exists.
         *
         * @return
         * @see ch.cyberduck.core.Local#exists()
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
            try {
                return ServiceUtils.toHex(ServiceUtils.computeMD5Hash(Local.this.getInputStream()));
            }
            catch(NoSuchAlgorithmException e) {
                log.error("MD5 failed:" + e.getMessage());
            }
            catch(IOException e) {
                log.error("MD5 failed:" + e.getMessage());
            }
            return null;
        }
    }

    private String path;


    /**
     * @param parent
     * @param name
     */
    public Local(Local parent, String name) {
        this(parent.getAbsolute(), name);
    }

    /**
     * @param parent
     * @param name
     */
    public Local(String parent, String name) {
        this.setPath(parent, name);
    }

    /**
     * @param path
     */
    public Local(String path) {
        this.setPath(path);
    }

    /**
     * @param path
     */
    public Local(File path) {
        this.setPath(path.getAbsolutePath());
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
            if(new File(path).createNewFile()) {
                this.setIcon(0);
            }
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void symlink(String target) {
        log.warn("Touching file instead of creating symbolic link:" + this);
        this.touch();
    }

    @Override
    public void mkdir(boolean recursive) {
        if(recursive) {
            if(new File(path).mkdirs()) {
                if(log.isInfoEnabled()) {
                    log.info("Created directory " + this.getAbsolute());
                }
            }
        }
        else {
            this.mkdir();
        }
    }

    @Override
    public void mkdir() {
        if(new File(path).mkdir()) {
            if(log.isInfoEnabled()) {
                log.info("Created directory " + this.getAbsolute());
            }
        }
    }

    /**
     * @param progress An integer from -1 and 9. If -1 is passed, the icon should be removed.
     */
    public abstract void setIcon(int progress);

    /**
     * By default just move the file to the user trash
     */
    @Override
    public void delete() {
        this.delete(true);
    }

    public void delete(boolean trash) {
        if(trash) {
            this.trash();
        }
        else {
            if(!new File(path).delete()) {
                log.warn("Delete failed:" + this.getAbsolute());
            }
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

    /**
     * @return Always return false
     */
    @Override
    public boolean isCached() {
        return false;
    }

    private Cache<Local> cache;

    /**
     * Local directory listings are never cached
     *
     * @return Always empty cache.
     */
    @Override
    public Cache<Local> cache() {
        if(null == cache) {
            cache = new Cache<Local>();
        }
        return this.cache;
    }

    @Override
    public AttributedList<Local> list() {
        final AttributedList<Local> children = new AttributedList<Local>();
        File[] files = new File(path).listFiles();
        if(null == files) {
            log.error("Error listing children:" + path);
            return children;
        }
        for(File file : files) {
            children.add(LocalFactory.createLocal(file));
        }
        return children;
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

    @Override
    public AbstractPath getParent() {
        return LocalFactory.createLocal(new File(path).getParentFile());
    }

    /**
     * @return True if the path exists on the file system.
     */
    @Override
    public boolean exists() {
        return new File(path).exists();
    }

    @Override
    protected void setPath(String name) {
        String normalized = name;
        if(Preferences.instance().getBoolean("local.normalize.unicode")) {
            if(!Normalizer.isNormalized(normalized, Normalizer.NFC, Normalizer.UNICODE_3_2)) {
                // Canonical decomposition followed by canonical composition (default)
                normalized = Normalizer.normalize(normalized, Normalizer.NFC, Normalizer.UNICODE_3_2);
                if(log.isInfoEnabled()) {
                    log.info("Normalized local path '" + name + "' to '" + normalized + "'");
                }
            }
        }
        path = normalized;
    }

    @Override
    public void writeTimestamp(long created, long modified, long accessed) {
        if(!new File(path).setLastModified(modified)) {
            log.warn("Write modification date failed:" + this.getAbsolute());
        }
    }

    @Override
    public abstract void writeUnixPermission(Permission perm, boolean recursive);

    @Override
    public void rename(AbstractPath renamed) {
        if(!new File(path).renameTo(new File(renamed.getAbsolute()))) {
            log.warn("Rename failed:" + renamed.getAbsolute());
        }
    }

    @Override
    public void copy(AbstractPath copy) {
        if(copy.equals(this)) {
            log.warn(this.getName() + " and " + copy.getName() + " are identical. Not copied.");
            return;
        }
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

    @Override
    public int hashCode() {
        return new File(path).getAbsolutePath().hashCode();
    }

    /**
     * Compares the two files using their path with a string comparision ignoring case.
     * Implementations should override this depending on the case sensitivity of the file system.
     *
     * @param o
     * @return
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

    /**
     * @return True if application was found to open the file with
     */
    public abstract boolean open();


    public abstract void bounce();

    public String getDefaultApplication() {
        return null;
    }

    public List<String> getDefaultApplications() {
        return Collections.emptyList();
    }

    /**
     * @param originUrl Page that linked to the downloaded file
     * @param dataUrl   Href where the file was downloaded from
     */
    public abstract void setQuarantine(final String originUrl, final String dataUrl);

    /**
     * @param dataUrl Href where the file was downloaded from
     */
    public abstract void setWhereFrom(final String dataUrl);

    public java.io.InputStream getInputStream() throws FileNotFoundException {
        return new RepeatableFileInputStream(new File(path));
    }

    public java.io.OutputStream getOutputStream(boolean resume) throws FileNotFoundException {
        return new FileOutputStream(new File(path), resume);
    }
}