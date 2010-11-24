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
import ch.cyberduck.core.http.HTTP3Session;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
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
public class DAVSession extends HTTP3Session {
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

    private DAVResource DAV;

    public DAVSession(Host h) {
        super(h);
    }

    @Override
    public void check() throws IOException {
        super.check();
        if(this.isConnected()) {
            this.getClient().clearHeaders();
        }
    }

    @Override
    protected DAVResource getClient() throws ConnectionCanceledException {
        if(null == DAV) {
            throw new ConnectionCanceledException();
        }
        return DAV;
    }

    protected void configure() throws IOException {
        final HttpClient client = this.getClient().getSessionInstance(this.getClient().getHttpURL(), false);
        HostConfiguration configuration = this.getHostConfiguration();
        final HostParams parameters = configuration.getParams();
        // Activates 'Expect: 100-Continue' handshake. The purpose of
        // the 'Expect: 100-Continue' handshake to allow a client that is
        // sending a request message with a request body to determine if
        // the origin server is willing to accept the request (based on
        // the request headers) before the client sends the request body.
        //
        // Otherwise, upload will fail when using digest authentication.
        // Fix #2268
        parameters.setParameter(HttpMethodParams.USE_EXPECT_CONTINUE, Boolean.TRUE);
        client.setHostConfiguration(configuration);
        if(Preferences.instance().getBoolean("connection.proxy.enable")) {
            final Proxy proxy = ProxyFactory.instance();
            if(host.getProtocol().isSecure()) {
                if(proxy.isHTTPSProxyEnabled() && !proxy.isHostExcluded(host.getHostname())) {
                    this.getClient().setProxy(proxy.getHTTPSProxyHost(), proxy.getHTTPSProxyPort());
                    //this.DAV.setProxyCredentials(new UsernamePasswordCredentials(null, null));
                }
                else {
                    this.getClient().setProxy(null, -1);
                    this.getClient().setProxyCredentials(null);
                }
            }
            else {
                if(proxy.isHTTPProxyEnabled() && !proxy.isHostExcluded(host.getHostname())) {
                    this.getClient().setProxy(proxy.getHTTPProxyHost(), proxy.getHTTPProxyPort());
                    //this.getClient().setProxyCredentials(new UsernamePasswordCredentials(null, null));
                }
                else {
                    this.getClient().setProxy(null, -1);
                    this.getClient().setProxyCredentials(null);
                }
            }
        }
        this.getClient().setFollowRedirects(Preferences.instance().getBoolean("webdav.followRedirects"));
        this.getClient().setOverwrite(true);
    }

    @Override
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        WebdavResource.setDefaultAction(WebdavResource.NOACTION);

        this.DAV = new DAVResource(host);
        final String workdir = host.getDefaultPath();
        if(StringUtils.isNotBlank(workdir)) {
            this.getClient().setPath(workdir.startsWith(String.valueOf(Path.DELIMITER)) ? workdir : String.valueOf(Path.DELIMITER) + workdir);
        }

        this.configure();
        this.login();

        WebdavResource.setDefaultAction(WebdavResource.BASIC);

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
    protected void login(final LoginController controller, final Credentials credentials) throws IOException {
        try {
            final HttpClient client = this.getClient().getSessionInstance(this.getClient().getHttpURL(), false);

            if(credentials.validate(host.getProtocol())) {
                // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
                this.getClient().setCredentials(
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
                        final StringBuilder realm = new StringBuilder(hostname);
                        realm.append(":").append(port).append(".");
                        if(StringUtils.isNotBlank(authscheme.getRealm())) {
                            realm.append(" ").append(authscheme.getRealm());
                        }
                        if(0 == retry) {
                            controller.check(host,
                                    Locale.localizedString("Login with username and password", "Credentials"),
                                    realm.toString());
                        }
                        else {
                            // authstate.isAuthAttempted() && authscheme.isComplete()
                            // Already tried and failed.
                            controller.fail(host.getProtocol(), credentials, realm.toString());
                        }

                        message(MessageFormat.format(Locale.localizedString("Authenticating as {0}", "Status"),
                                credentials.getUsername()));

                        retry++;
                        if(authscheme instanceof RFC2617Scheme) {
                            if(authscheme instanceof BasicScheme) {
                                DAVSession.super.warn(controller, credentials);
                            }
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
                        throw new CredentialsNotAvailableException(e.getMessage(), e);
                    }
                    catch(IOException e) {
                        throw new CredentialsNotAvailableException(e.getMessage(), e);
                    }
                }
            });

            // Try to get basic properties fo this resource using these credentials
            this.getClient().setProperties(WebdavResource.BASIC, DepthSupport.DEPTH_0);

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
                this.getClient().close();
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
    public void setWorkdir(Path workdir) throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        this.getClient().setPath(workdir.isRoot() ? String.valueOf(Path.DELIMITER) : workdir.getAbsolute() + String.valueOf(Path.DELIMITER));
        super.setWorkdir(workdir);
    }

    @Override
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isTimestampSupported() {
        return false;
    }

    @Override
    public void error(Path path, String message, Throwable e) {
        if(e instanceof HttpException) {
            String status = HttpStatus.getStatusText(((HttpException) e).getReasonCode());
            if(StringUtils.isNotBlank(status)) {
                super.error(path, message, new HttpException(status));
            }
            else {
                super.error(path, message, e);
            }
        }
        else {
            super.error(path, message, e);
        }
    }
}