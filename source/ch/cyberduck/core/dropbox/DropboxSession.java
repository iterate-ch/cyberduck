package ch.cyberduck.core.dropbox;

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

import oauth.signpost.exception.OAuthException;
import ch.cyberduck.core.*;
import ch.cyberduck.core.http.HTTP4Session;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.dropbox.client.Account;
import com.dropbox.client.DropboxClient;

import java.io.IOException;

/**
 * @version $Id: AzureSession.java 7323 2010-10-14 12:26:34Z dkocher $
 */
public class DropboxSession extends HTTP4Session {
    private static Logger log = Logger.getLogger(DropboxSession.class);

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new DropboxSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    protected DropboxSession(Host h) {
        super(h);
    }

    private DropboxClient client;

    @Override
    protected DropboxClient getClient() throws ConnectionCanceledException {
        if(null == client) {
            throw new ConnectionCanceledException();
        }
        return client;
    }

    @Override
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        client = new DropboxClient(this.http(), this.getHost().getProtocol().getScheme(), this.getHost().getPort());

        // Prompt the login credentials first
        this.login();
        this.fireConnectionDidOpenEvent();
    }

    @Override
    protected void login(LoginController controller, Credentials credentials) throws IOException {
        try {
            String key = Preferences.instance().getProperty("dropbox.key");
            String secret = Preferences.instance().getProperty("dropbox.secret");
            client.authenticate(key, secret, credentials.getUsername(), credentials.getPassword());

            Account account = this.getClient().account();
            log.info("Logged in as " + account.getDisplayName() + "(" + account.getUid() + ")");
        }
        catch(HttpException e) {
            controller.fail(this.getHost().getProtocol(), credentials, e.getMessage());
            this.login();
        }
        catch(OAuthException e) {
            controller.fail(this.getHost().getProtocol(), credentials, e.getMessage());
            this.login();
        }
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
                super.close();
            }
        }
        finally {
            // No logout required
            client = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isDownloadResumable() {
        return false;
    }

    @Override
    public boolean isUploadResumable() {
        return false;
    }

    @Override
    public boolean isCDNSupported() {
        return false;
    }
}