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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.Mimetypes;

import com.ibm.icu.text.Normalizer;

import java.util.Comparator;

/**
 * @version $Id$
 */
public abstract class AbstractPath {
    private static Logger log = Logger.getLogger(AbstractPath.class);

    /**
     * The path delimiter
     */
    public static final String DELIMITER = "/";

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
     *
     */
    private PathReference reference;

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
    public <T extends AbstractPath> AttributedList<T> childs() {
        return this.childs(null);
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
    public <T extends AbstractPath> AttributedList<T> childs(PathFilter<T> filter) {
        return this.childs(null, filter);
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
    public <T extends AbstractPath> AttributedList<T> childs(Comparator<T> comparator, PathFilter<T> filter) {
        if(!this.isCached()) {
            this.cache().put(this.<Object>getReference(), this.list());
        }
        return this.<T>cache().get(this.getReference(), comparator, filter);
    }

    /**
     * @return true if this paths points to '/'
     */
    public boolean isRoot() {
        return this.getAbsolute().equals(DELIMITER);
    }

    public static String getParent(String absolute) {
        int index = absolute.length() - 1;
        if(absolute.charAt(index) == '/') {
            if(index > 0) {
                index--;
            }
        }
        int cut = absolute.lastIndexOf('/', index);
        if(cut > 0) {
            return absolute.substring(0, cut);
        }
        //if (index == 0) //parent is root
        return DELIMITER;
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

    public static String normalize(final String path) {
        return normalize(path, true);
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.
     *
     * @param path     The path to parse
     * @param absolute If the path is absolute
     * @return the normalized path.
     * @author Adapted from org.apache.webdav
     * @license http://www.apache.org/licenses/LICENSE-2.0
     */
    public static String normalize(final String path, final boolean absolute) {
        if(null == path) {
            return DELIMITER;
        }
        String normalized = path;
        if(Preferences.instance().getBoolean("path.normalize")) {
            if(absolute) {
                while(!normalized.startsWith("\\\\") && !normalized.startsWith(DELIMITER)) {
                    normalized = DELIMITER + normalized;
                }
            }
            while(!normalized.endsWith(DELIMITER)) {
                normalized += DELIMITER;
            }
            // Resolve occurrences of "/./" in the normalized path
            while(true) {
                int index = normalized.indexOf("/./");
                if(index < 0) {
                    break;
                }
                normalized = normalized.substring(0, index) +
                        normalized.substring(index + 2);
            }
            // Resolve occurrences of "/../" in the normalized path
            while(true) {
                int index = normalized.indexOf("/../");
                if(index < 0) {
                    break;
                }
                if(index == 0) {
                    return DELIMITER;  // The only left path is the root.
                }
                normalized = normalized.substring(0, normalized.lastIndexOf('/', index - 1)) +
                        normalized.substring(index + 3);
            }
            StringBuilder n = new StringBuilder();
            if(normalized.startsWith("//")) {
                // see #972. Omit leading delimiter
                n.append(DELIMITER);
                n.append(DELIMITER);
            }
            else if(normalized.startsWith("\\\\")) {
                ;
            }
            else if(absolute) {
                // convert to absolute path
                n.append(DELIMITER);
            }
            else if(normalized.startsWith(DELIMITER)) {
                // Keep absolute path
                n.append(DELIMITER);
            }
            // Remove duplicated delimiters
            String[] segments = normalized.split(Path.DELIMITER);
            for(String segment : segments) {
                if(segment.equals("")) {
                    continue;
                }
                n.append(segment);
                n.append(DELIMITER);
            }
            normalized = n.toString();
            while(normalized.endsWith(DELIMITER) && normalized.length() > 1) {
                //Strip any redundant delimiter at the end of the path
                normalized = normalized.substring(0, normalized.length() - 1);
            }
        }
        if(Preferences.instance().getBoolean("path.normalize.unicode")) {
            if(!Normalizer.isNormalized(normalized, Normalizer.NFC, Normalizer.UNICODE_3_2)) {
                normalized = Normalizer.normalize(normalized, Normalizer.NFC, Normalizer.UNICODE_3_2);
            }
        }
        // Return the normalized path that we have completed
        return normalized;
    }

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
            parent = DELIMITER;
        }
        //Determine if the parent path already ends with a delimiter
        if(parent.endsWith(DELIMITER)) {
            this.setPath(parent + name);
        }
        else {
            this.setPath(parent + DELIMITER + name);
        }
    }

    public abstract void setPath(String name);

    /**
     * @param p
     * @return true if p is a child of me in the path hierarchy
     */
    public boolean isChild(AbstractPath p) {
        for(AbstractPath parent = this.getParent(); !parent.isRoot(); parent = parent.getParent()) {
            if(parent.equals(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param parent Absolute path to the symbolic link
     * @param name   Target of the symbolic link name. Absolute or relative pathname
     */
    public void setSymlinkTarget(String parent, String name) {
        if(name.startsWith(DELIMITER)) {
            // Symbolic link target may be an absolute path
            this.setSymlinkTarget(name);
        }
        else {
            if(parent.endsWith(DELIMITER)) {
                this.setSymlinkTarget(parent + name);
            }
            else {
                this.setSymlinkTarget(parent + DELIMITER + name);
            }
        }
    }

    /**
     * An absolute reference here the symbolic link is pointing to
     */
    private String symbolic = null;

    public void setSymlinkTarget(String p) {
        this.symbolic = AbstractPath.normalize(p);
    }

    /**
     * @return The target of the symbolic link if this path denotes a symbolic link
     * @see ch.cyberduck.core.PathAttributes#isSymbolicLink
     */
    public String getSymlinkTarget() {
        if(this.attributes().isSymbolicLink()) {
            return this.symbolic;
        }
        return null;
    }

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
    public abstract void touch(boolean recursive);

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
    public abstract void mkdir(boolean recursive);

    /**
     * @param perm      The permissions to apply
     * @param recursive Include subdirectories and files
     */
    public abstract void writePermissions(Permission perm, boolean recursive);

    /**
     * @param millis Milliseconds since 1970
     */
    public abstract void writeModificationDate(long millis);

    /**
     * Remove this file from the remote host. Does not affect any corresponding local file
     */
    public abstract void delete();

    /**
     * @return True if the directory contains no childs items
     */
    public boolean isEmpty() {
        return this.childs().size() == 0;
    }

    public boolean isRenameSupported() {
        return true;
    }

    /**
     * @param renamed Must be an absolute path
     */
    public abstract void rename(AbstractPath renamed);

    /**
     * @param copy
     */
    public abstract void copy(AbstractPath copy);
}