package ch.cyberduck.core;

import java.io.IOException;

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
     * @return The modification date of the file in UTC
     */
    public abstract long getModificationDate();

    public abstract void setModificationDate(long millis) throws IOException;

    /**
     * @return The file permission mask
     */
    public abstract Permission getPermission() throws IOException;

    public abstract void setPermission(Permission permission) throws IOException;

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
