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

public interface Attributes {

    /**
     * @return The length of the file
     */
    long getSize();

    /**
     * @return The time the file was last modified in millis UTC or -1 if unknown
     */
    long getModificationDate();

    /**
     * @return The time the file was created in millis UTC or -1 if unknown
     */
    long getCreationDate();

    /**
     * @return The time the file was last accessed in millis UTC or -1 if unknown
     */
    long getAccessedDate();

    /**
     * @return The file permission mask or null if unknown
     */
    Permission getPermission();

    /**
     * Retrieves the name of the owner of the file.
     *
     * @return The owner's name as a string, or null if the owner is unknown.
     */
    String getOwner();

    /**
     * Retrieves the name of the group to which the file belongs.
     *
     * @return The group name as a string, or null if the group is unknown.
     */
    String getGroup();
}