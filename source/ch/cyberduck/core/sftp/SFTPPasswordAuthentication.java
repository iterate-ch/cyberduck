package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public class SFTPPasswordAuthentication {
    private static final Logger log = Logger.getLogger(SFTPPasswordAuthentication.class);

    private SFTPSession session;

    public SFTPPasswordAuthentication(final SFTPSession session) {
        this.session = session;
    }

    public boolean authenticate(final Host host, final LoginCallback prompt)
            throws IOException, LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using password authentication with credentials %s", host.getCredentials()));
        }
        if(session.getClient().isAuthMethodAvailable(host.getCredentials().getUsername(), "password")) {
            return session.getClient().authenticateWithPassword(host.getCredentials().getUsername(), host.getCredentials().getPassword());
        }
        return false;
    }
}
