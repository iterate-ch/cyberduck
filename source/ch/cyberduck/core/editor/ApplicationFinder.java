package ch.cyberduck.core.editor;

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

import ch.cyberduck.core.local.Local;

import java.util.List;

/**
 * @version $Id$
 */
public interface ApplicationFinder {

    /**
     * @return All of the application bundle identifiers that are capable of handling
     *         the specified content type in the specified roles.
     */
    List<Application> findAll(Local file);

    /**
     * Find application for file type.
     *
     * @param file File
     * @return Absolute path to installed application
     */
    Application find(Local file);

    boolean isInstalled(Application application);

    Application find(String application);
}
