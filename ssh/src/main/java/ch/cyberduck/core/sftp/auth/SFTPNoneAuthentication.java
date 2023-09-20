package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AuthenticationProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.method.AuthNone;

public class SFTPNoneAuthentication implements AuthenticationProvider<Boolean> {
    private static final Logger log = LogManager.getLogger(SFTPNoneAuthentication.class);

    private final SSHClient client;

    public SFTPNoneAuthentication(final SSHClient client) {
        this.client = client;
    }

    @Override
    public Boolean authenticate(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel)
            throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using none authentication with credentials %s", bookmark.getCredentials()));
        }
        try {
            client.auth(bookmark.getCredentials().getUsername(), new AuthNone());
            return client.isAuthenticated();
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    @Override
    public String getMethod() {
        return "none";
    }
}
