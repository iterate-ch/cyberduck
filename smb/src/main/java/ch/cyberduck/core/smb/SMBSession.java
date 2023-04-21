package ch.cyberduck.core.smb;

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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;

import com.hierynomus.smbj.SMBClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SMBSession extends Session<SMBClient> {
    
    protected SMBSession(Host h) {
        super(h);
    }

    private static final Logger log = LogManager.getLogger(SMBSession.class);

    @Override
    protected SMBClient connect(Proxy proxy, HostKeyCallback key, LoginCallback prompt, CancelCallback cancel)
            throws BackgroundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connect'");
    }

    @Override
    public void login(Proxy proxy, LoginCallback prompt, CancelCallback cancel) throws BackgroundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    protected void logout() throws BackgroundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    // TODO implement methods or remove class
}
