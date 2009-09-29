package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.http.HTTPSession;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;

import java.io.IOException;
import java.net.InetAddress;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class DAVSession extends HTTPSession {
    private static Logger log = Logger.getLogger(DAVSession.class);

    static {
        SessionFactory.addFactory(Protocol.WEBDAV, new Factory());
    }

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new DAVSession(h);
        }
    }

    protected DAVResource DAV;

    protected DAVSession(Host h) {
        super(h);
    }

    @Override
    public void check() throws IOException {
        super.check();
        if(this.isConnected()) {
            DAV.clearHeaders();
        }
    }

    protected void configure() throws IOException {
        final HttpClient client = this.DAV.getSessionInstance(this.DAV.getHttpURL(), false);
        client.getHostConfiguration().getParams().setParameter(
                "http.useragent", this.getUserAgent()
        );
        final Proxy proxy = ProxyFactory.instance();
        if(proxy.isHTTPProxyEnabled()) {
            this.DAV.setProxy(proxy.getHTTPProxyHost(), proxy.getHTTPProxyPort());
        }
        this.DAV.setFollowRedirects(Preferences.instance().getBoolean("webdav.followRedirects"));
    }

    @Override
    protected void connect() throws IOException, LoginCanceledException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        this.message(MessageFormat.format(Locale.localizedString("Opening {0} connection to {1}", "Status"),
                host.getProtocol().getName(), host.getHostname()));

        WebdavResource.setDefaultAction(WebdavResource.NOACTION);

        this.DAV = new DAVResource(host.toURL());
        final String workdir = host.getDefaultPath();
        if(StringUtils.isNotBlank(workdir)) {
            this.DAV.setPath(workdir.startsWith(Path.DELIMITER) ? workdir : Path.DELIMITER + workdir);
        }

        this.configure();
        this.login();

        WebdavResource.setDefaultAction(WebdavResource.BASIC);

        this.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                host.getProtocol().getName()));

        if(null == this.DAV.getResourceType() || !this.DAV.getResourceType().isCollection()) {
            throw new IOException("Listing directory failed");
        }

        this.fireConnectionDidOpenEvent();
    }

    @Override
    public void setLoginController(final LoginController c) {
        this.login = new LoginController() {

            public void check(Host host) throws LoginCanceledException {
                final Credentials credentials = host.getCredentials();
                if(!credentials.isValid()) {
                    if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                        credentials.setPassword(((AbstractLoginController) c).find(host));
                    }
                }
                // Do not prompt for credentials yet but in the credentials provider
                // below upon request with the given authentication scheme realm
            }

            public void check(Host host, String reason) throws LoginCanceledException {
                c.check(host, reason);
            }

            public void success(Host host) {
                c.success(host);
            }

            public void fail(Host host, String reason) throws LoginCanceledException {
                c.fail(host, reason);
            }

            public void prompt(Host host, String reason, String message) throws LoginCanceledException {
                c.prompt(host, reason, message);
            }
        };
    }

    @Override
    protected void login(final Credentials credentials) throws IOException, LoginCanceledException {
        try {
            final HttpClient client = this.DAV.getSessionInstance(this.DAV.getHttpURL(), false);

            if(credentials.isValid()) {
                // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
                this.DAV.setCredentials(
                        new UsernamePasswordCredentials(credentials.getUsername(), credentials.getPassword()));
            }

            client.getParams().setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
            client.getParams().setParameter(CredentialsProvider.PROVIDER, new CredentialsProvider() {

                int retry = 0;

                public org.apache.commons.httpclient.Credentials getCredentials(AuthScheme authscheme, String hostname, int port, boolean proxy) throws CredentialsNotAvailableException {
                    if(null == authscheme) {
                        return null;
                    }
                    try {
                        final StringBuffer realm = new StringBuffer(hostname);
                        realm.append(":").append(port).append(".");
                        if(StringUtils.isNotBlank(authscheme.getRealm())) {
                            realm.append(" ").append(authscheme.getRealm());
                        }
                        if(0 == retry) {
                            login.check(host, realm.toString());
                        }
                        else {
                            // authstate.isAuthAttempted() && authscheme.isComplete()
                            // Already tried and failed.
                            login.fail(DAVSession.this.getHost(), realm.toString());
                        }

                        message(MessageFormat.format(Locale.localizedString("Authenticating as {0}", "Status"),
                                credentials.getUsername()));

                        retry++;
                        if(authscheme instanceof RFC2617Scheme) {
                            return new UsernamePasswordCredentials(credentials.getUsername(), credentials.getPassword());
                        }
                        if(authscheme instanceof NTLMScheme) {
                            return new NTCredentials(credentials.getUsername(),
                                    credentials.getPassword(), InetAddress.getLocalHost().getHostName(),
                                    Preferences.instance().getProperty("webdav.ntlm.domain"));
                        }
                        throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                                authscheme.getSchemeName());
                    }
                    catch(LoginCanceledException e) {
                        throw new CredentialsNotAvailableException();
                    }
                    catch(IOException e) {
                        throw new CredentialsNotAvailableException();
                    }
                }
            });

            // Try to get basic properties fo this resource using these credentials
            this.DAV.setProperties(WebdavResource.BASIC, DepthSupport.DEPTH_0);

            this.message(Locale.localizedString("Login successful", "Credentials"));
        }
        catch(HttpException e) {
            if(e.getReasonCode() == HttpStatus.SC_UNAUTHORIZED) {
                throw new LoginCanceledException();
            }
            throw e;
        }
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
                DAV.close();
            }
        }
        catch(IOException e) {
            log.error("IO Error: " + e.getMessage());
        }
        finally {
            DAV = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
    public void interrupt() {
        try {
            super.interrupt();
            this.fireConnectionWillCloseEvent();
            if(this.isConnected()) {
                DAV.close();
            }
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        finally {
            DAV = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
    public void setWorkdir(Path workdir) throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        DAV.setPath(workdir.isRoot() ? Path.DELIMITER : workdir.getAbsolute() + Path.DELIMITER);
        super.setWorkdir(workdir);
    }

    @Override
    protected void noop() throws IOException {
        ;
    }

    @Override
    public void sendCommand(String command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConnected() {
        return DAV != null;
    }

    @Override
    public void error(Path path, String message, Throwable e) {
        if(e instanceof HttpException) {
            super.error(path, message, new HttpException(
                    HttpStatus.getStatusText(((HttpException) e).getReasonCode())));
        }
        super.error(path, message, e);
    }
}