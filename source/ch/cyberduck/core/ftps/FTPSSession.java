package ch.cyberduck.core.ftps;

import java.io.IOException;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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
 * Opens a connection to the remote server via ftp protocol
 *
 * @version $Id$
 */
public class FTPSSession extends Session {

	static {
		SessionFactory.addFactory(Session.FTP_TLS, new Factory());
	}

	private static class Factory extends SessionFactory {
		protected Session create(Host h) {
			return new FTPSSession(h);
		}
	}

    private FTPSSession(Host h) {
        super(h);
    }

    public void connect(String encoding) throws IOException {
        throw new IOException("FTP-TLS not supported in this version. " +
                "Upgrade to Cyberduck 2.5 or later.");
    }

    public void close() {
        //
    }

    public Path workdir() {
        return null;
    }

    public void noop() throws IOException {
        throw new IOException("FTP-TLS not supported in this version. " +
                "Upgrade to Cyberduck 2.5 or later.");
    }

    public void check() throws IOException {
        throw new IOException("FTP-TLS not supported in this version. " +
                "Upgrade to Cyberduck 2.5 or later.");
    }
}