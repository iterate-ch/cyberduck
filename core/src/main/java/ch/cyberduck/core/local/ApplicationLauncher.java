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

import ch.cyberduck.core.Local;

public interface ApplicationLauncher {

    /**
     * Open file with default application registered for given file type
     *
     * @param file File on disk
     * @return True if application has been launched
     */
    boolean open(Local file);

    /**
     * Open file with specific application installed
     *
     * @param file        File on disk
     * @param application Application reference
     * @return True if application has been launched
     */
    boolean open(Local file, Application application);

    /**
     * Open application with given arguments
     *
     * @param application Application reference
     * @param args        Launch arguments
     * @return True if application has been launched
     */
    boolean open(Application application, String args);

    /**
     * Post notification for file
     *
     * @param file File on disk
     */
    void bounce(Local file);
}
