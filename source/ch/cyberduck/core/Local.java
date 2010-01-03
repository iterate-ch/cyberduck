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

import java.io.*;
import java.net.MalformedURLException;

/**
 * @version $Id$
 */
public abstract class Local extends AbstractPath implements Attributes {
    private static Logger log = Logger.getLogger(Local.class);

    {
        attributes = this;
    }

    public Permission getPermission() {
        return null;
    }

    public void setPermission(Permission p) {
        ;
    }

    public boolean isVolume() {
        return null == _impl.getParent();
    }

    public boolean isDirectory() {
        return _impl.isDirectory();
    }

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

    public void setType(int i) {
        ;
    }

    public void setSize(long size) {
        ;
    }

    public void setOwner(String owner) {
        ;
    }

    public void setGroup(String group) {
        ;
    }

    public String getOwner() {
        return null;
    }

    public String getGroup() {
        return null;
    }

    public long getModificationDate() {
        return _impl.lastModified();
    }

    public void setModificationDate(long millis) {
        ;
    }

    public long getCreationDate() {
        return this.getModificationDate();
    }

    public void setCreationDate(long millis) {
        ;
    }

    public long getAccessedDate() {
        return this.getModificationDate();
    }

    public void setAccessedDate(long millis) {
        ;
    }

    public int getType() {
        final int t = this.isFile() ? AbstractPath.FILE_TYPE : AbstractPath.DIRECTORY_TYPE;
        if(this.isSymbolicLink()) {
            return t | AbstractPath.SYMBOLIC_LINK_TYPE;
        }
        return t;
    }

    public long getSize() {
        if(this.isDirectory()) {
            return -1;
        }
        return _impl.length();
    }

    protected File _impl;

    public Local(Local parent, String name) {
        this(parent.getAbsolute(), name);
    }

    public Local(String parent, String name) {
        if(!Path.DELIMITER.equals(name)) {
            name = name.replace('/', ':');
        }
        // See trac #933
        this.setPath(parent, name);
    }

    public Local(String path) {
        this.setPath(path);
    }

    public Local(File path) {
        this.setPath(path.getAbsolutePath());
    }

    @Override
    public boolean isReadable() {
        return _impl.canRead();
    }

    @Override
    public boolean isWritable() {
        return _impl.canWrite();
    }

    /**
     * Creates a new file and sets its resource fork to feature a custom progress icon
     *
     * @return
     */
    public boolean touch() {
        if(!this.exists()) {
            try {
                if(_impl.createNewFile()) {
                    this.setIcon(0);
                }
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
        return false;
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
        if(this.attributes.isDirectory()) {
            return Locale.localizedString("Folder");
        }
        final String extension = this.getExtension();
        if(StringUtils.isEmpty(extension)) {
            return Locale.localizedString("Unknown");
        }
        // Native file type mapping
        final String kind = this.kind(this.getExtension());
        if(StringUtils.isEmpty(kind)) {
            return Locale.localizedString("Unknown");
        }
        return kind;
    }

    /**
     * @param extension Filename extension
     * @return Human readable description of file type
     */
    protected String kind(String extension) {
        return null;
    }

    @Override
    public String getAbsolute() {
        return _impl.getAbsolutePath();
    }

    public String getAbbreviatedPath() {
        return this.getAbsolute();
    }

    /**
     * @param <T>
     * @return Always null
     */
    @Override
    public <T> PathReference<T> getReference() {
        return null;
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
    public void mkdir(boolean recursive) {
        if(recursive) {
            if(_impl.mkdirs()) {
                log.info("Created directory " + this.getAbsolute());
            }
        }
        else {
            if(_impl.mkdir()) {
                log.warn("Created directory " + this.getAbsolute());
            }
        }
    }

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
     *
     */
    public abstract void open();


    public abstract void bounce();

    public String getDefaultApplication() {
        return null;
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

    public static class OutputStream extends FileOutputStream {
        public OutputStream(Local local, boolean resume) throws FileNotFoundException {
            super(local._impl, resume);
        }
    }

    public static class InputStream extends RepeatableFileInputStream {
        public InputStream(Local local) throws FileNotFoundException {
            super(local._impl);
        }
    }
}