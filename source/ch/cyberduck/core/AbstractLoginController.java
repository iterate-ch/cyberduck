package ch.cyberduck.core;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class AbstractLoginController implements LoginController {
    private static final Logger log = Logger.getLogger(AbstractLoginController.class);

    @Override
    public void prompt(final Protocol protocol, final Credentials credentials,
                       final String title, final String reason) throws LoginCanceledException {
        final LoginOptions options = new LoginOptions();
        options.publickey = protocol.equals(Protocol.SFTP);
        options.anonymous = protocol.isAnonymousConfigurable();
        this.prompt(protocol, credentials, title, reason, options);
    }
}
