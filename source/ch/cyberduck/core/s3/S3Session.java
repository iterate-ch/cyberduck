package ch.cyberduck.core.s3;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.*;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.*;
import org.apache.log4j.Logger;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * @version $Id:$
 */
public class S3Session extends Session {
    private static Logger log = Logger.getLogger(S3Session.class);

    static {
        SessionFactory.addFactory(Session.S3, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new S3Session(h);
        }
    }

    protected S3Service S3;

    protected S3Session(Host h) {
        super(h);
    }

    public boolean isSecure() {
        if(this.isConnected()) {
            return S3.isHttpsOnly();
        }
        return false;
    }

    public String getSecurityInformation() {
        try {
            return this.host.getIp();
        }
        catch(UnknownHostException e) {
            return this.host.getHostname();
        }
    }

    private final String ua = NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleName") + "/"
            + Preferences.instance().getProperty("version");

    private void configure(Jets3tProperties configuration) {
        configuration.setProperty("s3service.s3-endpoint", host.getHostname());
        configuration.setProperty("s3service.https-only",
                String.valueOf(host.getPort() == Session.HTTPS_PORT)
        );

        final String location = Preferences.instance().getProperty("s3.location");
        if(location.equals("US")) {
            // null defaults to US
            configuration.setProperty("s3service.default-bucket-location", null);
        }
        else {
            configuration.setProperty("s3service.default-bucket-location", location);
        }

        configuration.setProperty("httpclient.proxy-autodetect", "false");
        configuration.setProperty("httpclient.connection-timeout-ms",
                String.valueOf(Preferences.instance().getInteger("connection.timeout.seconds") * 1000)
        );
        configuration.setProperty("httpclient.socket-timeout-ms",
                String.valueOf(Preferences.instance().getInteger("connection.timeout.seconds") * 1000)
        );
        configuration.setProperty("httpclient.useragent", ua);

//        final String cipher = Preferences.instance().getProperty("s3.crypto.algorithm");
//        if(EncryptionUtil.isCipherAvailableForUse(cipher)) {
//            configuration.setProperty("crypto.algorithm", cipher);
//        }
//        else {
//            log.warn("Cipher " + cipher + " not available for use.");
//        }

        configuration.setProperty("downloads.restoreLastModifiedDate",
                Preferences.instance().getProperty("queue.download.preserveDate"));
    }

    protected void connect() throws IOException, ConnectionCanceledException, LoginCanceledException {
        synchronized(this) {
            if(this.isConnected()) {
                return;
            }
            this.fireConnectionWillOpenEvent();
            this.message(NSBundle.localizedString("Opening S3 connection to", "Status", "") + " " + host.getHostname() + "...");
            try {
                // Prompt the login credentials first
                this.login();
                AWSCredentials credentials = null; //Browse publicly available bucket
                if(!host.getCredentials().isAnonymousLogin()) {
                    credentials = new AWSCredentials(host.getCredentials().getUsername(),
                            host.getCredentials().getPassword());
                }

                final Jets3tProperties configuration = new Jets3tProperties();
                this.configure(configuration);

                this.S3 = new RestS3Service(credentials, ua, new CredentialsProvider() {
                    /**
                     * Implementation method for the CredentialsProvider interface
                     * @throws CredentialsNotAvailableException
                     */
                    public Credentials getCredentials(AuthScheme authscheme, String hostname, int port, boolean proxy)
                            throws CredentialsNotAvailableException {
                        if(authscheme == null) {
                            return null;
                        }
                        try {
                            // Backup server credentials
                            final String user = host.getCredentials().getUsername();
                            final String pass = host.getCredentials().getPassword();
                            // Misuse the login dialog for the credential provider for
                            // the additional HTTP auth scheme
                            login();
                            Credentials credentials = null;
                            if(authscheme instanceof NTLMScheme) {
                                //requires Windows authentication
//                                        credentials = new NTCredentials(host.getCredentials().getUsername(),
//                                                host.getCredentials().getPassword(),
//                                            host, domain);
                                throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                                        authscheme.getSchemeName());
                            }
                            else if(authscheme instanceof RFC2617Scheme) {
                                //requires authentication for the realm authschema.getRealm()
                                credentials = new UsernamePasswordCredentials(
                                        host.getCredentials().getUsername(), host.getCredentials().getPassword());
                            }
                            else {
                                throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                                        authscheme.getSchemeName());
                            }
                            // Set previous values for the authentication to the server
                            host.getCredentials().setUsername(user);
                            host.getCredentials().setPassword(pass);
                            return credentials;
                        }
                        catch(IOException e) {
                            throw new CredentialsNotAvailableException(e.getMessage(), e);
                        }
                    }
                }, configuration);
                host.getCredentials().addInternetPasswordToKeychain(host.getProtocol(),
                        host.getHostname(), host.getPort());
            }
            catch(S3ServiceException e) {
                throw new S3Exception(e.getMessage(), e);
            }
            try {
                if(!this.isConnected()) {
                    throw new ConnectionCanceledException();
                }
                this.message(NSBundle.localizedString("S3 connection opened", "Status", ""));
                this.fireConnectionDidOpenEvent();
            }
            catch(NullPointerException e) {
                // Because the connection could have been closed using #interrupt and set this.FTP to null; we
                // should find a better way to handle this asynchroneous issue than to catch a null pointer
                throw new ConnectionCanceledException();
            }
        }
    }

    protected void login() throws IOException, LoginCanceledException {
        if(!host.getCredentials().tryAgain()) {
            throw new LoginCanceledException();
        }
        if(!host.getCredentials().check(this.loginController, host.getProtocol(), host.getHostname())) {
            throw new LoginCanceledException();
        }
        this.message(NSBundle.localizedString("Authenticating as", "Status", "") + " '"
                + host.getCredentials().getUsername() + "'");
    }

    public void close() {
        synchronized(this) {
            try {
                if(this.isConnected()) {
                    this.fireConnectionWillCloseEvent();
                }
            }
            finally {
                S3 = null;
                this.fireConnectionDidCloseEvent();
                this.fireActivityStoppedEvent();
            }
        }
    }

    public void interrupt() {
        ;
    }

    protected Path workdir() throws IOException {
        synchronized(this) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            if(null == workdir) {
                workdir = PathFactory.createPath(this, Path.DELIMITER, Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
            }
            return workdir;
        }
    }

    protected void setWorkdir(Path workdir) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected void noop() throws IOException {
        ;
    }

    public void sendCommand(String command) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean isConnected() {
        return S3 != null;
    }
}
