package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.Mimetypes;

import java.util.Comparator;

/**
 * @version $Id$
 */
public abstract class AbstractPath {
    private static Logger log = Logger.getLogger(AbstractPath.class);

    /**
     * Shortcut for the home directory
     */
    public static final String HOME = "~";

    public static final int FILE_TYPE = 1;
    public static final int DIRECTORY_TYPE = 2;
    public static final int SYMBOLIC_LINK_TYPE = 4;
    public static final int VOLUME_TYPE = 8;

    /**
     * @return True if this path denotes a directory and its file listing is cached for this session
     * @see ch.cyberduck.core.Cache
     */
    public boolean isCached() {
        return this.cache().containsKey(this.<Object>getReference())
                && !this.cache().get(this.<Object>getReference()).attributes().isInvalid();
    }

    public abstract <T extends AbstractPath> Cache<T> cache();

    /**
     * @return
     */
    public abstract Attributes attributes();

    /**
     * Clear cached listing
     *
     * @throws NullPointerException if session is not initialized
     */
    public void invalidate() {
        if(this.attributes().isDirectory()) {
            this.cache().get(this.<Object>getReference()).attributes().setInvalid(true);
        }
    }

    /**
     * Obtain a string representation of the path that is unique for versioned files.
     *
     * @return The absolute path with version ID and checksum if any.
     */
    public String unique() {
        final StringBuilder reference = new StringBuilder(this.getAbsolute());
        if(this.attributes().isDuplicate()) {
            if(StringUtils.isNotBlank(this.attributes().getVersionId())) {
                reference.append("-").append(this.attributes().getVersionId());
            }
            else if(StringUtils.isNotBlank(this.attributes().getChecksum())) {
                reference.append("-").append(this.attributes().getChecksum());
            }
        }
        return reference.toString();
    }

    /**
     * To lookup a copy of the path in the cache.
     */
    protected PathReference reference;

    /**
     * Default implementation returning a reference to self. You can override this
     * if you need a different strategy to compare hashcode and equality for caching
     * in a model.
     *
     * @return Reference to the path to be used in table models an file listing
     *         cache.
     * @see ch.cyberduck.core.Cache#lookup(PathReference)
     */
    public <T> PathReference<T> getReference() {
        if(null == reference) {
            reference = PathReferenceFactory.createPathReference(this);
        }
        return reference;
    }

    public void setReference(PathReference reference) {
        this.reference = reference;
    }

    public abstract String toURL();

    /**
     * Fetch the directory listing
     *
     * @return
     */
    public abstract <T extends AbstractPath> AttributedList<T> list();

    /**
     * Get the cached directory listing if any or return #list instead.
     * No sorting and filtering applied.
     *
     * @return Cached directory listing as returned by the server
     * @see #list()
     */
    public <T extends AbstractPath> AttributedList<T> children() {
        return this.children(null);
    }

    /**
     * Get the cached directory listing if any or return #list instead.
     * No sorting applied.
     *
     * @param filter Filter to apply to directory listing
     * @param <T>
     * @return Cached directory listing as returned by the server filtered
     * @see #list()
     */
    public <T extends AbstractPath> AttributedList<T> children(PathFilter<T> filter) {
        return this.children(null, filter);
    }

    /**
     * Request a sorted and filtered file listing from the server. Has to be a directory.
     * A cached listing is returned if possible
     *
     * @param comparator The comparator to sort the listing with
     * @param filter     The filter to exlude certain files
     * @return The children of this path or an empty list if it is not accessible for some reason
     * @see #list()
     */
    public <T extends AbstractPath> AttributedList<T> children(Comparator<T> comparator, PathFilter<T> filter) {
        if(!this.isCached()) {
            this.cache().put(this.<Object>getReference(), this.list());
        }
        return this.<T>cache().get(this.getReference(), comparator, filter);
    }

    public abstract char getPathDelimiter();

    /**
     * @return true if this paths points to '/'
     * @see #getPathDelimiter()
     */
    public boolean isRoot() {
        return this.getAbsolute().equals(String.valueOf(this.getPathDelimiter()));
    }

    protected String getParent(String absolute) {
        int index = absolute.length() - 1;
        if(absolute.charAt(index) == this.getPathDelimiter()) {
            if(index > 0) {
                index--;
            }
        }
        int cut = absolute.lastIndexOf(this.getPathDelimiter(), index);
        if(cut > 0) {
            return absolute.substring(0, cut);
        }
        //if (index == 0) parent is root
        return String.valueOf(this.getPathDelimiter());
    }

    public abstract String getAbsolute();

    public abstract String getName();

    /**
     * Subclasses may override to return a user friendly representation of the name denoting this path.
     */
    public String getDisplayName() {
        return this.getName();
    }

    public abstract AbstractPath getParent();

    public abstract boolean exists();


    /**
     * @return the extension if any or null otherwise
     */
    public String getExtension() {
        final String extension = FilenameUtils.getExtension(this.getName());
        if(StringUtils.isEmpty(extension)) {
            return null;
        }
        return extension;
    }

    public String getMimeType() {
        return getMimeType(this.getName().toLowerCase());
    }

    public static String getMimeType(String extension) {
        return Mimetypes.getInstance().getMimetype(extension);
    }

    /**
     * @param parent The parent directory
     * @param name   The relative filename
     */
    public void setPath(String parent, String name) {
        if(null == parent) {
            parent = String.valueOf(this.getPathDelimiter());
        }
        //Determine if the parent path already ends with a delimiter
        if(parent.endsWith(String.valueOf(this.getPathDelimiter()))) {
            this.setPath(parent + name);
        }
        else {
            this.setPath(parent + this.getPathDelimiter() + name);
        }
    }

    public abstract void setPath(String name);

    /**
     * @param directory
     * @return true if this is a child of <code>p</code> in the path hierarchy
     */
    public boolean isChild(AbstractPath directory) {
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
        if(this.getParent().equals(directory.getParent())) {
            // Cannot be a child if the same parent
            return false;
        }
        for(AbstractPath parent = this.getParent(); !parent.isRoot(); parent = parent.getParent()) {
            if(parent.equals(directory)) {
                return true;
            }
        }
        return false;
    }

    public abstract String kind();

    /**
     * @return The target of the symbolic link if this path denotes a symbolic link
     * @see ch.cyberduck.core.PathAttributes#isSymbolicLink
     */
    public abstract String getSymlinkTarget();


    /**
     * Create a new empty file.
     */
    public abstract void touch();

    /**
     * Create a new empty file.
     *
     * @param recursive Create intermediate directories as required.  If this option is
     *                  not specified, the full path prefix of each operand must already exist
     */
    public void touch(boolean recursive) {
        if(!this.exists()) {
            if(recursive) {
                if(!this.getParent().exists()) {
                    this.getParent().mkdir(recursive);
                }
            }
            this.touch();
        }
    }

    /**
     * Create a new folder.
     */
    public abstract void mkdir();

    /**
     * Create a new folder.
     *
     * @param recursive Create intermediate directories as required.  If this option is
     *                  not specified, the full path prefix of each operand must already exist
     */
    public void mkdir(boolean recursive) {
        if(recursive) {
            final AbstractPath parent = this.getParent();
            if(!parent.exists()) {
                parent.mkdir(recursive);
            }
        }
        this.mkdir();
    }

    /**
     * @param perm      The permissions to apply
     * @param recursive Include subdirectories and files
     * @see Session#isUnixPermissionsSupported()
     */
    public abstract void writeUnixPermission(Permission perm, boolean recursive);

    /**
     * @param created
     * @param modified
     * @param accessed @see ch.cyberduck.core.Session#isTimestampSupported()
     */
    public abstract void writeTimestamp(long created, long modified, long accessed);

    /**
     * Remove this file from the remote host. Does not affect any corresponding local file
     */
    public abstract void delete();

    /**
     * @return True if the directory contains no children items
     */
    public boolean isEmpty() {
        return this.children().size() == 0;
    }

    /**
     * @param renamed Must be an absolute path
     */
    public abstract void rename(AbstractPath renamed);

    /**
     * Duplicate file
     *
     * @param copy Destination
     */
    public abstract void copy(AbstractPath copy);

    public static class DescriptiveUrl {
        private String url = StringUtils.EMPTY;

        private String help = StringUtils.EMPTY;

        public DescriptiveUrl(String url) {
            this(url, Locale.localizedString("Open in Web Browser"));
        }

        public DescriptiveUrl(String url, String help) {
            this.url = url;
            this.help = help;
        }

        public String getUrl() {
            return url;
        }

        public String getHelp() {
            return help;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof DescriptiveUrl) {
                return this.getUrl().equals(((DescriptiveUrl) obj).getUrl());
            }
            return false;
        }
    }
}