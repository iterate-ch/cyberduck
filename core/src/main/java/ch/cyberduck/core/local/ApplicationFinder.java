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

import java.util.List;

public interface ApplicationFinder {

    /**
     * @param filename File type to search default editor
     * @return All of the application bundle identifiers that are capable of handling
     *         the specified content type in the specified roles.
     */
    List<Application> findAll(String filename);

    /**
     * Find application for file type.
     *
     * @param filename File type to search default editor
     * @return Absolute path to installed application Application#notfound if not installed
     * @see ch.cyberduck.core.local.Application#notfound
     */
    Application find(String filename);

    /**
     * @param application Application description
     * @return True if path to the applicaiton is found. False if the application is not installed
     */
    boolean isInstalled(Application application);

    /**
     * @param identifier Application identifier of search path
     * @return Application description or Application#notfound if not installed
     * @see ch.cyberduck.core.local.Application#notfound
     */
    Application getDescription(String identifier);
}
