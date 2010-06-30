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
    private LocalAttributes attributes;

    @Override
    public LocalAttributes attributes() {
        if(null == attributes) {
            attributes = new LocalAttributes();
        }
        return attributes;
    }

    /**
     *
     */
    public class LocalPermission extends Permission {
        @Override
        public boolean isReadable() {
            return _impl.canRead();
        }

        @Override
        public boolean isWritable() {
            return _impl.canWrite();
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
            return _impl.lastModified();
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
            return _impl.length();
        }

        @Override
        public Permission getPermission() {
            return new LocalPermission();
        }

        @Override
        public boolean isVolume() {
            return null == _impl.getParent();
        }

        @Override
        public boolean isDirectory() {
            return _impl.isDirectory();
        }

        @Override
        public boolean isFile() {
            return _impl.isFile();
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
                return !_impl.getAbsolutePath().equals(_impl.getCanonicalPath());
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

    protected File _impl;

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
        if(!Path.DELIMITER.equals(name)) {
            name = name.replace('/', ':');
        }
        // See trac #933
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

    /**
     * Creates a new file and sets its resource fork to feature a custom progress icon
     */
    @Override
    public void touch(boolean recursive) {
        if(!this.exists()) {
            if(this.attributes().isFile()) {
                if(recursive) {
                    if(!this.getParent().exists()) {
                        this.getParent().touch(recursive);
                    }
                }
                this.touch();
            }
        }
    }

    @Override
    public void touch() {
        if(this.attributes().isFile()) {
            try {
                if(_impl.createNewFile()) {
                    this.setIcon(0);
                }
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void mkdir(boolean recursive) {
        if(this.attributes().isDirectory()) {
            if(recursive) {
                if(_impl.mkdirs()) {
                    log.info("Created directory " + this.getAbsolute());
                }
            }
            else {
                this.mkdir();
            }
        }
    }

    @Override
    public void mkdir() {
        if(this.attributes().isDirectory()) {
            if(_impl.mkdir()) {
                log.warn("Created directory " + this.getAbsolute());
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
            if(!_impl.delete()) {
                log.warn("Delete failed:" + this.getAbsolute());
            }
        }
    }

    /**
     * Move file to trash.
     */
    public abstract void trash();

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
        final AttributedList<Local> childs = new AttributedList<Local>();
        File[] files = _impl.listFiles();
        if(null == files) {
            log.error("_impl.listFiles == null");
            return childs;
        }
        for(File file : files) {
            childs.add(LocalFactory.createLocal(file));
        }
        return childs;
    }

    /**
     * @return the file type for the extension of this file provided by launch services
     */
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
        return _impl.getAbsolutePath();
    }

    public String getAbbreviatedPath() {
        return this.getAbsolute();
    }

    @Override
    public String getSymlinkTarget() {
        try {
            return _impl.getCanonicalPath();
        }
        catch(IOException e) {
            log.error(e.getMessage());
            return this.getAbsolute();
        }
    }

    @Override
    public String getName() {
        return _impl.getName();
    }

    @Override
    public AbstractPath getParent() {
        return LocalFactory.createLocal(_impl.getParentFile());
    }

    @Override
    public boolean exists() {
        return _impl.exists();
    }

    @Override
    public void setPath(String name) {
        _impl = new File(Path.normalize(name));
    }

    @Override
    public void writeTimestamp(long millis) {
        if(!_impl.setLastModified(millis)) {
            log.warn("Write modification date failed:" + this.getAbsolute());
        }
    }

    @Override
    public abstract void writeUnixPermission(Permission perm, boolean recursive);

    @Override
    public void rename(AbstractPath renamed) {
        if(_impl.renameTo(new File(this.getParent().getAbsolute(), renamed.getAbsolute()))) {
            this.setPath(this.getParent().getAbsolute(), renamed.getAbsolute());
        }
    }

    @Override
    public void copy(AbstractPath copy) {
        if(copy.equals(this)) {
            return;
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(_impl);
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
        return _impl.getAbsolutePath().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Local) {
            // Compare the resolved absolute path
            return this.getSymlinkTarget().equalsIgnoreCase(((Local) other).getSymlinkTarget());
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
            return _impl.toURI().toURL().toString();
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
        return new RepeatableFileInputStream(this._impl);
    }

    public java.io.OutputStream getOutputStream(boolean resume) throws FileNotFoundException {
        return new FileOutputStream(this._impl, resume);
    }
}