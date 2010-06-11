package ch.cyberduck.core.gd;

/*
 *  Copyright (c) 2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.util.AuthenticationException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class GDSession extends Session {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GDSession.class);

    static {
        SessionFactory.addFactory(Protocol.GDOCS, new Factory());
    }

    public static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new GDSession(h);
        }
    }

    private DocsService client;

    protected GDSession(Host h) {
        super(h);
    }

    @Override
    protected DocsService getClient() throws ConnectionCanceledException {
        if(null == client) {
            throw new ConnectionCanceledException();
        }
        return client;
    }

    private final Handler appender = new Handler() {
        @Override
        public void publish(LogRecord record) {
            GDSession.this.log(false, record.getMessage());
        }

        @Override
        public void flush() {
            ;
        }

        @Override
        public void close() throws SecurityException {
            ;
        }
    };

    private static final Logger http = Logger.getLogger("com.google.gdata.client.http.HttpGDataRequest");

    @Override
    protected void fireConnectionWillOpenEvent() throws ResolveCanceledException, UnknownHostException {
        http.setLevel(Level.FINER);
        http.addHandler(appender);
        super.fireConnectionWillOpenEvent();
    }

    @Override
    protected void fireConnectionWillCloseEvent() {
        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.google.gdata.client.http.HttpGDataRequest");
        logger.removeHandler(appender);
        super.fireConnectionWillCloseEvent();
    }

    @Override
    protected void connect() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        this.message(MessageFormat.format(Locale.localizedString("Opening {0} connection to {1}", "Status"),
                host.getProtocol().getName(), host.getHostname()));

        client = new DocsService(this.getUserAgent());
        client.setReadTimeout(this.timeout());
        client.setConnectTimeout(this.timeout());
        client.useSsl();

        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        this.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                host.getProtocol().getName()));
        this.login();
        this.fireConnectionDidOpenEvent();
    }

    @Override
    public String getUserAgent() {
        return Preferences.instance().getProperty("application") + "-"
                + Preferences.instance().getProperty("version");
    }

    @Override
    protected void login(Credentials credentials) throws IOException {
        try {
            this.getClient().setUserCredentials(credentials.getUsername(), credentials.getPassword());
            this.message(Locale.localizedString("Login successful", "Credentials"));
        }
        catch(AuthenticationException e) {
            this.message(Locale.localizedString("Login failed", "Credentials"));
            this.login.fail(host, e.getMessage());
            this.login();
        }
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
            }
        }
        finally {
            // No logout required
            client = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
    protected void noop() throws IOException {
        ;
    }

    @Override
    public void sendCommand(String command) throws IOException {
        ;
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isDownloadResumable() {
        return false;
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isUploadResumable() {
        return false;
    }
}
