package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.i18n.Locale;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;

import java.io.IOException;

import com.googlecode.sardine.impl.SardineException;
import com.googlecode.sardine.impl.handler.VoidResponseHandler;
import com.googlecode.sardine.impl.methods.HttpPropFind;

/**
 * @version $Id$
 */
public class DAVSession extends HttpSession {

    private DAVClient client;

    public DAVSession(Host h) {
        super(h);
    }

    @Override
    public DAVClient getClient() throws ConnectionCanceledException {
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

        this.client = new DAVClient(this.http());
        this.login();

        this.fireConnectionDidOpenEvent();
    }

    @Override
    protected void warn(LoginController login, Credentials credentials) throws IOException {
        // Do not warn yet but in the credentials provider depending on the choosen realm.
    }

    @Override
    protected void login(final LoginController controller, final Credentials credentials) throws IOException {
        this.client.setCredentials(credentials.getUsername(), credentials.getPassword(),
                // Windows credentials. Provide empty string for NTLM domain by default.
                Preferences.instance().getProperty("webdav.ntlm.workstation"),
                Preferences.instance().getProperty("webdav.ntlm.domain"));
        if(credentials.validate(host.getProtocol())) {
            // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
            this.client.enablePreemptiveAuthentication(this.getHost().getHostname());
        }
        try {
            this.client.execute(new HttpHead(this.home().toURL()), new VoidResponseHandler());
            this.message(Locale.localizedString("Login successful", "Credentials"));
        }
        catch(SardineException e) {
            if(e.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                // Possibly only HEAD requests are not allowed
                this.client.execute(new HttpPropFind(this.home().toURL()), new VoidResponseHandler());
                this.message(Locale.localizedString("Login successful", "Credentials"));
            }
            else if(e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                this.message(Locale.localizedString("Login failed", "Credentials"));
                controller.fail(host.getProtocol(), credentials);
                this.login();
            }
            else {
                throw e;
            }
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
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isWriteTimestampSupported() {
        return false;
    }

    @Override
    public boolean isMetadataSupported() {
        return true;
    }
}
