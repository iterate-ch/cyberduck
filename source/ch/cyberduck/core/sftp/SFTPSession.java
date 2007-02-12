package ch.cyberduck.core.sftp;

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

import ch.ethz.ssh2.*;

import ch.cyberduck.core.*;
import ch.cyberduck.core.Session;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSPathUtilities;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @version $Id$
 */
public class SFTPSession extends Session {
    private static Logger log = Logger.getLogger(SFTPSession.class);

    static {
        SessionFactory.addFactory(Session.SFTP, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new SFTPSession(h);
        }
    }

    protected Connection SSH;

    protected SFTPv3Client SFTP;

    private SFTPSession(Host h) {
        super(h);
    }

    public boolean isSecure() {
        if(null == SSH) {
            return false;
        }
        return SSH.isAuthenticationComplete();
    }

    public String getSecurityInformation() {
        StringBuffer info = new StringBuffer();
        info.append("Protocol version: " + SFTP.getProtocolVersion()+"\n");
        try {
            final ConnectionInfo i = SSH.getConnectionInfo();
            info.append("Key Exchange (KEX) Algorithm: "+i.keyExchangeAlgorithm+"\n");
            info.append("Number of key exchanges performed on this connection so far: "+i.keyExchangeCounter+"\n");
            info.append("Host Key Algorithm: "+i.serverHostKeyAlgorithm+"\n");
            info.append("Server to Client Crypto Algorithm: "+i.serverToClientCryptoAlgorithm+"\n");
            info.append("Client to Server Crypto Algorithm: "+i.clientToServerCryptoAlgorithm+"\n");
            info.append("Server to Client MAC Algorithm: "+i.serverToClientMACAlgorithm+"\n");
            info.append("Client to Server MAC Algorithm: "+i.clientToServerMACAlgorithm+"\n");
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        return info.toString();
    }

    private ServerHostKeyVerifier verifier = null;

    public void setHostKeyVerificationController(ServerHostKeyVerifier v) {
        this.verifier = v;
    }

    protected void connect() throws IOException, LoginCanceledException {
        synchronized(this) {
            if(this.isConnected()) {
                return;
            }
            this.fireConnectionWillOpenEvent();
            this.message(NSBundle.localizedString("Opening SSH connection to", "Status", "") + " " + host.getHostname() + "...");
            SSH = new Connection(this.host.getHostname(), this.host.getPort());
            try {
                SSH.addConnectionMonitor(new ConnectionMonitor() {
                    public void connectionLost(Throwable reason) {
                        interrupt();
                    }
                });
                final int timeout = Preferences.instance().getInteger("connection.timeout");
                SSH.connect(verifier, timeout, timeout);
                if(!this.isConnected()) {
                    return;
                }
                this.message(NSBundle.localizedString("SSH connection opened", "Status", ""));
                this.login();
                SFTP = new SFTPv3Client(SSH);
                this.message(NSBundle.localizedString("SFTP subsystem ready", "Status", ""));
                SFTP.setCharset(this.getEncoding());
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
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(!host.getCredentials().check(this.loginController)) {
            throw new LoginCanceledException();
        }
        this.message(NSBundle.localizedString("Authenticating as", "Status", "") + " '"
                + host.getCredentials().getUsername() + "'");
        if(host.getCredentials().usesPublicKeyAuthentication()) {
            if(this.loginUsingPublicKeyAuthentication(host.getCredentials())) {
                this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
                return;
            }
        }
        else {
            if(this.loginUsingPasswordAuthentication(host.getCredentials()) ||
                    this.loginUsingKBIAuthentication(host.getCredentials())) {
                this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
                host.getCredentials().addInternetPasswordToKeychain();
                return;
            }
        }
        this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
        loginController.promptUser(host.getCredentials(),
                NSBundle.localizedString("Login failed", "Credentials", ""),
                NSBundle.localizedString("Login with username and password", "Credentials", ""));
        if(!host.getCredentials().tryAgain()) {
            throw new LoginCanceledException();
        }
        this.login();
    }

    private boolean loginUsingPublicKeyAuthentication(final Login credentials) throws IOException {
        if(SSH.isAuthMethodAvailable(host.getCredentials().getUsername(), "publickey")) {
            File key = new File(NSPathUtilities.stringByExpandingTildeInPath(credentials.getPrivateKeyFile()));
            if(key.exists()) {
                // If the private key is passphrase protected then ask for the passphrase
//                if(key.isPassphraseProtected()) {
                String passphrase = Keychain.instance().getPasswordFromKeychain("SSHKeychain", credentials.getPrivateKeyFile());
                if(null == passphrase || passphrase.equals("")) {
                    loginController.promptUser(host.getCredentials(),
                            NSBundle.localizedString("Private key password protected", "Credentials", ""),
                            NSBundle.localizedString("Enter the passphrase for the private key file", "Credentials", "")
                                    + " (" + credentials.getPrivateKeyFile() + ")");
                    if(host.getCredentials().tryAgain()) {
                        passphrase = credentials.getPassword();
//                            if(key.isPassphraseProtected()) {
//                                if(credentials.usesKeychain()) {
//                                    Keychain.instance().addPasswordToKeychain("SSHKeychain", credentials.getPrivateKeyFile(),
//                                            passphrase);
//                                }
//                            }
                    }
                    else {
                        throw new LoginCanceledException();
                    }
                }
                return SSH.authenticateWithPublicKey(host.getCredentials().getUsername(), key,
                        passphrase);
            }
            log.error("Key file " + key.getAbsolutePath() + " does not exist.");
        }
        return false;
    }

    private boolean loginUsingPasswordAuthentication(final Login credentials) throws IOException {
        if(SSH.isAuthMethodAvailable(host.getCredentials().getUsername(), "password")) {
            return SSH.authenticateWithPassword(credentials.getUsername(), credentials.getPassword());
        }
        return false;
    }

    private boolean loginUsingKBIAuthentication(final Login credentials) throws IOException {
        if(SSH.isAuthMethodAvailable(credentials.getUsername(), "keyboard-interactive")) {
            InteractiveLogic il = new InteractiveLogic(credentials);
            return SSH.authenticateWithKeyboardInteractive(credentials.getUsername(), il);
        }
        return false;
    }

    /**
     * The logic that one has to implement if "keyboard-interactive" autentication shall be
     * supported.
     */
    private class InteractiveLogic implements InteractiveCallback {
        int promptCount = 0;
        Login credentials;

        public InteractiveLogic(final Login credentials) {
            this.credentials = credentials;
        }

        /**
         * The callback may be invoked several times, depending on how
         * many questions-sets the server sends
         */
        public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt,
                                         boolean[] echo) throws IOException {
            String[] result = new String[numPrompts];
            for(int i = 0; i < numPrompts; i++) {
                result[i] = credentials.getPassword();
                promptCount++;
            }

            return result;
        }

        /**
         * We maintain a prompt counter - this enables the detection of situations where the ssh
         * server is signaling "authentication failed" even though it did not send a single prompt.
         */
        public int getPromptCount() {
            return promptCount;
        }
    }

    public void close() {
        synchronized(this) {
            this.fireActivityStartedEvent();
            try {
                this.fireConnectionWillCloseEvent();
                if(SFTP != null) {
                    SFTP.close();
                }
                if(SSH != null) {
                    SSH.close();
                }
            }
            finally {
                this.fireConnectionDidCloseEvent();
                this.fireActivityStoppedEvent();
            }
        }
    }

    public void interrupt() {
        try {
            super.interrupt();
            if(null == SSH) {
                return;
            }
            this.fireConnectionWillCloseEvent();
            SSH.close();
        }
        finally {
            SFTP = null;
            SSH = null;
            this.fireActivityStoppedEvent();
            this.fireConnectionDidCloseEvent();
        }
    }

    protected Path workdir() throws ConnectionCanceledException {
        synchronized(this) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            Path workdir = null;
            try {
                // "." as referring to the current directory
                workdir = PathFactory.createPath(this, SFTP.canonicalPath("."));
                workdir.attributes.setType(Path.DIRECTORY_TYPE);
            }
            catch(IOException e) {
                this.error(null, "Connection failed", e);
                this.interrupt();
            }
            return workdir;
        }
    }

    protected void noop() throws IOException {
        synchronized(this) {
            if(this.isConnected()) {
                SSH.sendIgnorePacket();
            }
        }
    }

    public void sendCommand(String command) throws IOException {
        ;
    }

    public boolean isConnected() {
        if(null == SSH) {
            return false;
        }
        return true;
    }
}
