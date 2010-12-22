package ch.cyberduck.core.gdocs;

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
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.commons.lang.StringUtils;

import com.google.gdata.client.ClientLoginAccountType;
import com.google.gdata.client.GoogleAuthTokenFactory;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.http.GoogleGDataRequest;
import com.google.gdata.client.http.HttpUrlConnectionSource;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.common.base.CharEscapers;
import com.google.gdata.util.common.base.StringUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 */
public class GDSession extends SSLSession {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GDSession.class);

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new GDSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    private DocsService client;

    public GDSession(Host h) {
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
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        GoogleGDataRequest.Factory requestFactory = new GoogleGDataRequest.Factory();
        requestFactory.setConnectionSource(new HttpUrlConnectionSource() {
            public HttpURLConnection openConnection(URL url) throws IOException {
                return getConnection(url);
            }
        });
        GoogleAuthTokenFactory authFactory = new GoogleAuthTokenFactory(DocsService.DOCS_SERVICE, this.getUserAgent(),
                host.getProtocol().getScheme(), "www.google.com", client) {

            @Override
            public String getAuthToken(String username,
                                       String password,
                                       String captchaToken,
                                       String captchaAnswer,
                                       String serviceName,
                                       String applicationName,
                                       ClientLoginAccountType accountType)
                    throws AuthenticationException {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Email", username);
                params.put("Passwd", password);
                params.put("source", applicationName);
                params.put("service", serviceName);
                params.put("accountType", accountType.getValue());

                if(captchaToken != null) {
                    params.put("logintoken", captchaToken);
                }
                if(captchaAnswer != null) {
                    params.put("logincaptcha", captchaAnswer);
                }
                String postOutput;
                try {
                    URL url = new URL(host.getProtocol().getScheme() + "://" + "www.google.com" + GOOGLE_LOGIN_PATH);
                    postOutput = request(url, params);
                }
                catch(IOException e) {
                    AuthenticationException ae =
                            new AuthenticationException(Locale.localizedString("Connection failed", "Error"));
                    ae.initCause(e);
                    throw ae;
                }

                // Parse the output
                Map<String, String> tokenPairs =
                        StringUtil.string2Map(postOutput.trim(), "\n", "=", true);
                String token = tokenPairs.get("Auth");
                if(token == null) {
                    throw getAuthException(tokenPairs);
                }
                return token;
            }

            /**
             * Returns the respective {@code AuthenticationException} given the return
             * values from the login URI handler.
             *
             * @param pairs name/value pairs returned as a result of a bad authentication
             * @return the respective {@code AuthenticationException} for the given error
             */
            private AuthenticationException getAuthException(Map<String, String> pairs) {

                String errorName = pairs.get("Error");

                if("BadAuthentication".equals(errorName)) {
                    return new GoogleService.InvalidCredentialsException("Invalid credentials");

                }
                else if("AccountDeleted".equals(errorName)) {
                    return new GoogleService.AccountDeletedException("Account deleted");

                }
                else if("AccountDisabled".equals(errorName)) {
                    return new GoogleService.AccountDisabledException("Account disabled");

                }
                else if("NotVerified".equals(errorName)) {
                    return new GoogleService.NotVerifiedException("Not verified");

                }
                else if("TermsNotAgreed".equals(errorName)) {
                    return new GoogleService.TermsNotAgreedException("Terms not agreed");

                }
                else if("ServiceUnavailable".equals(errorName)) {
                    return new GoogleService.ServiceUnavailableException("Service unavailable");

                }
                else if("CaptchaRequired".equals(errorName)) {

                    String captchaPath = pairs.get("CaptchaUrl");
                    StringBuilder captchaUrl = new StringBuilder();
                    captchaUrl.append(host.getProtocol().getScheme()).append("://").append("www.google.com")
                            .append(GOOGLE_ACCOUNTS_PATH).append('/').append(captchaPath);
                    return new GoogleService.CaptchaRequiredException("Captcha required",
                            captchaUrl.toString(),
                            pairs.get("CaptchaToken"));

                }
                else {
                    return new AuthenticationException("Error authenticating " +
                            "(check service name)");
                }
            }

            /**
             * Makes a HTTP POST request to the provided {@code url} given the
             * provided {@code parameters}.  It returns the output from the POST
             * handler as a String object.
             *
             * @param url the URL to post the request
             * @param parameters the parameters to post to the handler
             * @return the output from the handler
             * @throws IOException if an I/O exception occurs while creating, writing,
             *                     or reading the request
             */
            private String request(URL url, Map<String, String> parameters)
                    throws IOException {

                // Open connection
                HttpURLConnection urlConnection = getConnection(url);

                // Set properties of the connection
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                // Form the POST parameters
                StringBuilder content = new StringBuilder();
                boolean first = true;
                for(Map.Entry<String, String> parameter : parameters.entrySet()) {
                    if(!first) {
                        content.append("&");
                    }
                    content.append(
                            CharEscapers.uriEscaper().escape(parameter.getKey())).append("=");
                    content.append(CharEscapers.uriEscaper().escape(parameter.getValue()));
                    first = false;
                }

                OutputStream outputStream = null;
                try {
                    outputStream = urlConnection.getOutputStream();
                    outputStream.write(content.toString().getBytes("utf-8"));
                    outputStream.flush();
                }
                finally {
                    if(outputStream != null) {
                        outputStream.close();
                    }
                }

                // Retrieve the output
                InputStream inputStream = null;
                StringBuilder outputBuilder = new StringBuilder();
                try {
                    int responseCode = urlConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = urlConnection.getInputStream();
                    }
                    else {
                        inputStream = urlConnection.getErrorStream();
                    }

                    String string;
                    if(inputStream != null) {
                        BufferedReader reader =
                                new BufferedReader(new InputStreamReader(inputStream));
                        while(null != (string = reader.readLine())) {
                            outputBuilder.append(string).append('\n');
                        }
                    }
                }
                finally {
                    if(inputStream != null) {
                        inputStream.close();
                    }
                }
                return outputBuilder.toString();
            }
        };
        client = new DocsService(this.getUserAgent(), requestFactory, authFactory);
        client.setReadTimeout(this.timeout());
        client.setConnectTimeout(this.timeout());
        if(this.getHost().getProtocol().isSecure()) {
            client.useSsl();
        }

        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        this.login();
        this.fireConnectionDidOpenEvent();
    }

    protected HttpURLConnection getConnection(URL url) throws IOException {
        URLConnection c = null;
        if(Preferences.instance().getBoolean("connection.proxy.enable")) {
            final Proxy proxy = ProxyFactory.instance();
            if(proxy.isHTTPSProxyEnabled(new Host(Protocol.GDOCS_SSL, url.getHost(), url.getPort()))) {
                c = url.openConnection(new java.net.Proxy(java.net.Proxy.Type.HTTP,
                        new InetSocketAddress(proxy.getHTTPSProxyHost(), proxy.getHTTPSProxyPort())));
            }
        }
        if(null == c) {
            log.debug("Direct connection");
            c = url.openConnection();
        }
        c.setConnectTimeout(timeout());
        c.setReadTimeout(timeout());
        if(c instanceof HttpsURLConnection) {
            ((HttpsURLConnection) c).setSSLSocketFactory(new CustomTrustSSLProtocolSocketFactory(
                    this.getTrustManager(url.getHost())));
            ((HttpsURLConnection) c).setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String s, javax.net.ssl.SSLSession sslSession) {
                    return true;
                }
            });
        }
        if(c instanceof HttpURLConnection) {
            return (HttpURLConnection) c;
        }
        throw new ConnectionCanceledException("Invalid URL connection:" + c);
    }

    @Override
    public String getUserAgent() {
        return Preferences.instance().getProperty("application.name") + "-"
                + Preferences.instance().getProperty("application.version");
    }

    @Override
    protected void login(LoginController controller, Credentials credentials) throws IOException {
        try {
            this.getClient().setUserCredentials(credentials.getUsername(), credentials.getPassword());
            this.message(Locale.localizedString("Login successful", "Credentials"));
        }
        catch(AuthenticationException e) {
            this.message(Locale.localizedString("Login failed", "Credentials"));
            controller.fail(host.getProtocol(), credentials, e.getMessage());
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

    @Override
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isAclSupported() {
        return true;
    }

    @Override
    public boolean isTimestampSupported() {
        return false;
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(List<Path> files) {
        return Arrays.asList(new Acl.Role(AclRole.OWNER.getValue(), false), new Acl.Role(AclRole.READER.getValue()),
                new Acl.Role(AclRole.WRITER.getValue())
        );
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return Arrays.asList(
                new Acl.EmailUser(StringUtils.EMPTY) {
                    @Override
                    public String getPlaceholder() {
                        return Locale.localizedString("Google Email Address", "Google");
                    }
                },
                new Acl.DomainUser(StringUtils.EMPTY) {
                    @Override
                    public String getPlaceholder() {
                        return Locale.localizedString("Google Apps Domain", "Google");
                    }
                },
                new Acl.GroupUser(StringUtils.EMPTY, true) {
                    @Override
                    public String getPlaceholder() {
                        return Locale.localizedString("Google Group Email Address", "Google");
                    }
                },
                new Acl.CanonicalUser(Locale.localizedString("Public", "Google"), false) {
                    @Override
                    public String getPlaceholder() {
                        return Locale.localizedString("Public", "Google");
                    }
                }
        );
    }

    @Override
    public boolean isCDNSupported() {
        return false;
    }
}
