package ch.cyberduck.core.sardine;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.http.HTTP4Session;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.log4j.Logger;

import com.googlecode.sardine.impl.SardineImpl;

import java.io.IOException;

/**
 * @version $Id$
 */
public class DAVSession extends HTTP4Session {
    private static Logger log = Logger.getLogger(DAVSession.class);

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new DAVSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    private SardineImpl DAV;

    public DAVSession(Host h) {
        super(h);
    }

    @Override
    protected SardineImpl getClient() throws ConnectionCanceledException {
        if(null == DAV) {
            throw new ConnectionCanceledException();
        }
        return DAV;
    }

    @Override
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        this.DAV = new SardineImpl(this.http());
        this.DAV.enablePreemptiveAuthentication(this.getHost().getProtocol().getScheme(),
                this.getHost().getHostname(), this.getHost().getPort());
//        this.DAV.enableCompression();

        this.login();

        this.fireConnectionDidOpenEvent();
    }

    @Override
    protected void prompt(LoginController login) throws LoginCanceledException {
        // Do not prompt for credentials yet but in the credentials provider
        // below upon request when the given authentication scheme realm is known
    }

    @Override
    protected void warn(LoginController login, Credentials credentials) throws IOException {
        // Do not warn yet but in the credentials provider depending on the choosen realm.
    }

    @Override
    protected void login(final LoginController controller, final Credentials c) throws IOException {
        AbstractHttpClient client = this.http();
        client.setCredentialsProvider(new CredentialsProvider() {
            public void setCredentials(AuthScope authscope, org.apache.http.auth.Credentials credentials) {
                ;
            }

            public org.apache.http.auth.Credentials getCredentials(AuthScope authscope) {
                final StringBuilder realm = new StringBuilder(authscope.getHost());
                if(StringUtils.isNotBlank(authscope.getRealm())) {
                    realm.append(":").append(authscope.getPort()).append(".");
                }
                if(StringUtils.isNotBlank(authscope.getRealm())) {
                    realm.append(" ").append(authscope.getRealm());
                }
                try {
                    controller.check(host,
                            Locale.localizedString("Login with username and password", "Credentials"),
                            realm.toString());
                }
                catch(LoginCanceledException e) {
                    log.warn(e.getMessage());
                    return null;
                }
                if(authscope.getScheme().equals(AuthPolicy.NTLM)) {
                    StringBuilder auth = new StringBuilder(c.getUsername());
                    if(StringUtils.isNotBlank(c.getPassword())) {
                        auth.append(":").append(c.getPassword());
                    }
                    return new NTCredentials(auth.toString());
                }
                return new org.apache.http.auth.UsernamePasswordCredentials(c.getUsername(), c.getPassword());
            }

            public void clear() {
                ;
            }
        });
    }

    @Override
    protected Path mount(String directory) throws IOException {
        final Path home = super.mount(directory);
        try {
            // Try to get basic properties fo this resource using these credentials
            home.list();
        }
        catch(RuntimeException e) {
            if(e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
        this.message(Locale.localizedString("Login successful", "Credentials"));
        return home;
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
            DAV = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isTimestampSupported() {
        return false;
    }
}