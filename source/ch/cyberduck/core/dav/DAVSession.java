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

import ch.cyberduck.core.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import com.googlecode.sardine.impl.SardineImpl;

import java.io.IOException;

/**
 * @version $Id$
 */
public class DAVSession extends HttpSession {
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

    /**
     *
     */
    private static class CustomCredentialsProvider extends BasicCredentialsProvider {
        private boolean failure;
        private boolean retry;

        public boolean isFailure() {
            return failure;
        }

        public void setFailure(boolean failure) {
            this.failure = failure;
        }

        public boolean isRetry() {
            return retry;
        }

        public void setRetry(boolean retry) {
            this.retry = retry;
        }

        /**
         * Append realm message to hostname.
         *
         * @param authscope
         * @return
         */
        public StringBuilder getRealm(AuthScope authscope) {
            final StringBuilder realm = new StringBuilder(authscope.getHost());
            if(StringUtils.isNotBlank(authscope.getRealm())) {
                realm.append(":").append(authscope.getPort()).append(".");
            }
            if(StringUtils.isNotBlank(authscope.getRealm())) {
                realm.append(" ").append(authscope.getRealm());
            }
            return realm;
        }
    }

    @Override
    protected void login(final LoginController controller, final Credentials credentials) throws IOException {
        final AbstractHttpClient client = this.http();
        if(credentials.validate(host.getProtocol())) {
            // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
            this.getClient().setCredentials(credentials.getUsername(), credentials.getPassword());
        }
        final CustomCredentialsProvider provider = new CustomCredentialsProvider() {

            /**
             * @see org.apache.http.impl.client.DefaultRequestDirector#handleResponse(org.apache.http.impl.client.RoutedRequest, org.apache.http.HttpResponse, org.apache.http.protocol.HttpContext)
             * @param authscope Read the realm of the domain from this
             * @return Null if login has been canceled and no retry attempt should be made
             */
            @Override
            public org.apache.http.auth.Credentials getCredentials(AuthScope authscope) {
                final StringBuilder realm = this.getRealm(authscope);
                try {
                    if(!this.isRetry()) {
                        controller.check(host,
                                Locale.localizedString("Login with username and password", "Credentials"),
                                realm.toString());
                    }
                    else if(this.isFailure()) {
                        // Already received a unauthorized response with custom credentials set
                        message(Locale.localizedString("Login failed", "Credentials"));
                        controller.fail(host.getProtocol(), credentials, realm.toString());
                    }
                    // Reset status
                    this.setFailure(false);
                    this.setRetry(true);
                }
                catch(LoginCanceledException e) {
                    // Request will not be retried again.
                    return null;
                }
                if(!credentials.validate(DAVSession.this.getHost().getProtocol())) {
                    log.warn("No credentials available");
                    return null;
                }
                if(authscope.getScheme().equals(AuthPolicy.NTLM)) {
                    // Windows credentials. Provide empty string for NTLM domain by default.
                    return new NTCredentials(credentials.getUsername(), credentials.getPassword(),
                            Preferences.instance().getProperty("webdav.ntlm.workstation"),
                            Preferences.instance().getProperty("webdav.ntlm.domain"));
                }
                else {
                    // Basic or Digest authentication credentials
                    return new UsernamePasswordCredentials(credentials.getUsername(), credentials.getPassword());
                }
            }
        };
        client.setCredentialsProvider(provider);
        client.addResponseInterceptor(new HttpResponseInterceptor() {
            /**
             * Clear the credentials from the authentication state upon failed login to make sure a retry attempt is made.
             *
             * @see org.apache.http.impl.client.DefaultRequestDirector#updateAuthState(org.apache.http.auth.AuthState, org.apache.http.HttpHost, org.apache.http.client.CredentialsProvider)
             */
            public void process(final HttpResponse r, final HttpContext context) throws HttpException, IOException {
                final int code = r.getStatusLine().getStatusCode();
                if(code == HttpStatus.SC_UNAUTHORIZED) {
                    // Obtain authentication state
                    final AuthState authstate = (AuthState) context.getAttribute(
                            ClientContext.TARGET_AUTH_STATE);
                    if(null == authstate) {
                        log.warn("No auth state available in context:" + context);
                    }
                    else {
                        // Reset false credentials
                        authstate.setCredentials(null);
                        // Release underlying connection so we will get a new one (hopefully) when we retry.
                        HttpConnection conn = (HttpConnection) context.getAttribute(
                                ExecutionContext.HTTP_CONNECTION);
                        try {
                            conn.close();
                        }
                        catch(IOException e) {
                            log.warn("Error closing connection:" + e.getMessage());
                        }
                    }
                    // Do not handle here because authentication state is not populated yet.
                    provider.setFailure(true);
                }
            }
        });
        try {
            // Provoke authentication failure if listing is denied
            this.getClient().getResources(this.home().toURL());
            this.message(Locale.localizedString("Login successful", "Credentials"));
        }
        catch(HttpResponseException e) {
            if(e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
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