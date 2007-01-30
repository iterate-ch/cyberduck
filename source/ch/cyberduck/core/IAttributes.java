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

/**
 * @version $Id$
 */
public interface IAttributes {

    /**
     * @return The length of the file
     */
    public abstract double getSize();

    /**
     * @return The modification date of the file using UTC
     */
    public abstract long getTimestamp();

    /**
     * @return The file permission mask
     */
    public abstract Permission getPermission();

    /**
     * @return True if this path denotes a directory or is a symbolic link pointing to a directory
     */
    public abstract boolean isDirectory();

    /**
     * @return True if this path denotes a regular file or is a symbolic link pointing to a regular file
     */
    public abstract boolean isFile();

    /**
     * @return True if this path denotes a symbolic link.
     * @warn Returns false for Mac OS Classic Alias
     */
    public abstract boolean isSymbolicLink();
}
