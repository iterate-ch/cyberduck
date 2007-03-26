package ch.cyberduck.core;

import com.apple.cocoa.foundation.NSObject;

import java.util.Iterator;
import java.util.Comparator;
import java.io.IOException;
import java.net.URL;

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

/**
 * @version $Id$
 */
public abstract class AbstractPath extends NSObject {

    /**
     * The path delimiter
     */
    public static final String DELIMITER = "/";

    /**
     * Attributes denoting this path
     */
    public Attributes attributes;
    public static final int FILE_TYPE = 1;
    public static final int DIRECTORY_TYPE = 2;
    public static final int SYMBOLIC_LINK_TYPE = 4;

    /**
     * @return True if this path denotes a directory and its file listing is cached for this session
     * @see ch.cyberduck.core.Cache
     */
    public boolean isCached() {
        return this.cache().containsKey(this);
    }

    public abstract Cache cache();

    /**
     * Calculates recursively the size of this path
     *
     * @return The size of the file or the sum of all containing files if a directory
     * @warn Potentially lengthy operation
     */
    public double size() {
        if(this.attributes.isDirectory()) {
            double size = 0;
            for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                size += ((AbstractPath) iter.next()).size();
            }
            this.attributes.setSize(size);
        }
        return this.attributes.getSize();
    }

    /**
     * Clear cached listing
     * @throws NullPointerException if session is not initialized
     */
    public void invalidate() {
        if(this.isCached()) {
            this.cache().get(this).attributes().setDirty(true);
        }
    }

    public abstract URL toURL();

    /**
     * Fetch the directory listing
     * @return
     */
    public abstract AttributedList list();

    /**
     * Get the cached directory listing
     * @return
     */
    public AttributedList childs() {
        return this.childs(new NullComparator(), new NullPathFilter());
    }

    /**
     * Request a sorted and filtered file listing from the server. Has to be a directory.
     * A cached listing is returned if possible
     * @param comparator The comparator to sort the listing with
     * @param filter     The filter to exlude certain files
     * @return The children of this path or an empty list if it is not accessible for some reason
     */
    public AttributedList childs(Comparator comparator, PathFilter filter) {
        if(!this.isCached() || this.cache().get(this).attributes().isDirty()) {
            this.cache().put(this, this.list());
        }
        return this.cache().get(this, comparator, filter);
    }

    /**
     * @return true if this paths points to '/'
     */
    public boolean isRoot() {
        return this.getAbsolute().equals(DELIMITER) || this.getAbsolute().indexOf('/') == -1;
    }

    public abstract String getAbsolute();

    public abstract String getName();

    public abstract AbstractPath getParent();

    public abstract boolean exists();

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.
     * *
     *
     * @return the normalized path.
     * @author Adapted from org.apache.webdav
     * @license http://www.apache.org/licenses/LICENSE-2.0
     */
    public static String normalize(final String path) {
        String normalized = path;
        if(Preferences.instance().getBoolean("path.normalize")) {
            while(!normalized.startsWith(DELIMITER)) {
                normalized = DELIMITER + normalized;
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
//            // Resolve occurrences of "//" in the normalized path
//            while(true) {
//                int index = normalized.indexOf("//");
//                if(index < 0) {
//                    break;
//                }
//                normalized = normalized.substring(0, index) +
//                        normalized.substring(index + 1);
//            }
            while(normalized.endsWith(DELIMITER) && normalized.length() > 1) {
                //Strip any redundant delimiter at the end of the path
                normalized = normalized.substring(0, normalized.length() - 1);
            }
        }
        // Return the normalized path that we have completed
        return normalized;
    }

    /**
     * @return the extension if any or null otherwise
     */
    public String getExtension() {
        String name = this.getName();
        int index = name.lastIndexOf(".");
        if(index != -1 && index != 0) {
            return name.substring(index + 1, name.length());
        }
        return null;
    }

    /**
     * @param parent The parent directory
     * @param name   The relative filename
     */
    public void setPath(String parent, String name) {
        //Determine if the parent path already ends with a delimiter
        if(parent.endsWith(DELIMITER)) {
            this.setPath(parent + name);
        }
        else {
            this.setPath(parent + DELIMITER + name);
        }
    }

    public abstract void setPath(String name);

    public void setSymbolicLinkPath(String parent, String name) {
        if(parent.endsWith(DELIMITER)) {
            this.setSymbolicLinkPath(parent + name);
        }
        else {
            this.setSymbolicLinkPath(parent + DELIMITER + name);
        }
    }

    /**
     * Where the symbolic link is pointing to
     */
    private String symbolic = null;

    public void setSymbolicLinkPath(String p) {
        this.symbolic = p;
    }

    /**
     * @return The target of the symbolic link if this path denotes a symbolic link
     * @see ch.cyberduck.core.PathAttributes#isSymbolicLink
     */
    public String getSymbolicLinkPath() {
        if(this.attributes.isSymbolicLink()) {
            return this.symbolic;
        }
        return null;
    }

    public void mkdir() {
        this.mkdir(false);
    }

    /**
     * @param recursive Create intermediate directories as required.  If this option is
     *                  not specified, the full path prefix of each operand must already exist
     */
    public abstract void mkdir(boolean recursive);

    /**
     * @param recursive Include subdirectories and files
     */
    public abstract void changePermissions(Permission perm, boolean recursive);

    public abstract void changeModificationDate(long millis);

    /**
     * Remove this file from the remote host. Does not affect any corresponding local file
     */
    public abstract void delete();

    /**
     * @param name Must be an absolute path
     */
    public abstract void rename(String name);

    /**
     * Changes the session's working directory to this path
     */
    public abstract void cwdir() throws IOException;
}
