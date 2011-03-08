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

import ch.cyberduck.core.*;
import ch.cyberduck.core.dropbox.client.Account;
import ch.cyberduck.core.dropbox.client.DropboxClient;
import ch.cyberduck.core.http.HTTP4Session;
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

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

        if(log.isInfoEnabled()) {
            Account account = this.getClient().account();
            log.info("Logged in as " + account.getDisplayName() + "(" + account.getUid() + ")");
        }

        this.fireConnectionDidOpenEvent();
    }

    @Override
    protected void login(LoginController controller, Credentials credentials) throws IOException {
        Credentials application = new Credentials(
                Preferences.instance().getProperty("dropbox.key"),
                Preferences.instance().getProperty("dropbox.secret"), false) {
            @Override
            public String getUsernamePlaceholder() {
                return Locale.localizedString("Dropbox API Key");
            }

            @Override
            public String getPasswordPlaceholder() {
                return Locale.localizedString("Dropbox API Secret");
            }
        };
        if(!application.validate(this.getHost().getProtocol())) {
            // Prompt for MFA credentials.
            controller.prompt(host.getProtocol(), application,
                    Locale.localizedString("Dropbox App Keys"),
                    Locale.localizedString("Provide additional login credentials", "Credentials") + ".",
                    false, false, false);
            // Save updated keys
            Preferences.instance().setProperty("dropbox.key", application.getUsername());
            Preferences.instance().setProperty("dropbox.secret", application.getPassword());
        }
        try {
            client.authenticate(
                    Preferences.instance().getProperty("dropbox.key"),
                    Preferences.instance().getProperty("dropbox.secret"),
                    credentials.getUsername(), credentials.getPassword());
        }
        catch(IOException e) {
            controller.fail(this.getHost().getProtocol(), credentials, e.getMessage());
            Preferences.instance().deleteProperty("dropbox.secret");
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

    @Override
    public boolean isChecksumSupported() {
        return true;
    }
}