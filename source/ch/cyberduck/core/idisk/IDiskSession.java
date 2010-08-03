package ch.cyberduck.core.idisk;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.dav.DAVSession;

/**
 * @version $Id$
 */
public class IDiskSession extends DAVSession {

    public static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new IDiskSession(h);
        }
    }

    public IDiskSession(Host h) {
        super(h);
    }

    /**
     * Prefixing user agent with "WebDAVFS". Fix for #4435.
     *
     * @return
     */
    @Override
    public String getUserAgent() {
        return "WebDAVFS/" + super.getUserAgent();
    }
}
