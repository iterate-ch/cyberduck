package ch.cyberduck.core.threading;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.i18n.Locale;

/**
 * @version $Id$
 */
public class BackgroundException extends Exception {

    private String message;

    private Path path;

    private Session session;

    public BackgroundException(Session session, Path path, String message, Throwable cause) {
        super(cause);
        this.session = session;
        this.path = path;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return Locale.localizedString(this.message, "Error");
    }

    /**
     * @return The real cause of the exception thrown
     */
    @Override
    public Throwable getCause() {
        return super.getCause();
    }

    /**
     * @return The path accessed when the exception was thrown or null if
     *         the exception is not related to any path
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * @return The session this exception occured
     */
    public Session getSession() {
        return this.session;
    }
}
