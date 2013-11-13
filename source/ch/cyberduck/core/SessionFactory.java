package ch.cyberduck.core;

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

import org.apache.log4j.Logger;

public final class SessionFactory {
    private static final Logger log = Logger.getLogger(SessionFactory.class);

    private SessionFactory() {
        //
    }

    public static Session create(final Host host) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create session for %s", host));
        }
        final Protocol protocol = host.getProtocol();
        return protocol.createSession(host);
    }
}
