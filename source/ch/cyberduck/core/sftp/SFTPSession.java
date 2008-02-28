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

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.*;
import ch.cyberduck.core.Session;

import org.apache.log4j.Logger;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;

import ch.ethz.ssh2.*;
import ch.ethz.ssh2.channel.ChannelClosedException;
import ch.ethz.ssh2.crypto.PEMDecoder;
import ch.ethz.ssh2.sftp.SFTPv3Client;

/**
 * @version $Id$
 */
public class SFTPSession extends Session {
    private static Logger log = Logger.getLogger(SFTPSession.class);

    static {
        SessionFactory.addFactory(Protocol.SFTP, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new SFTPSession(h);
        }
    }

    protected Connection SSH;

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
        if(SFTP != null) {
            info.append("SFTP Protocol version: " + SFTP.getProtocolVersion() + "\n");
        }
        try {
            final ConnectionInfo i = SSH.getConnectionInfo();
            info.append("Key Exchange (KEX) Algorithm: " + i.keyExchangeAlgorithm + "\n");
            info.append("Number of key exchanges performed on this connection so far: " + i.keyExchangeCounter + "\n");
            info.append("Host Key Algorithm: " + i.serverHostKeyAlgorithm + "\n");
            info.append("Server to Client Crypto Algorithm: " + i.serverToClientCryptoAlgorithm + "\n");
            info.append("Client to Server Crypto Algorithm: " + i.clientToServerCryptoAlgorithm + "\n");
            info.append("Server to Client MAC Algorithm: " + i.serverToClientMACAlgorithm + "\n");
            info.append("Client to Server MAC Algorithm: " + i.clientToServerMACAlgorithm + "\n");
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

    private SFTPv3Client SFTP;

    /**
     * If never called before opens a new SFTP subsystem. If called before, the cached
     * SFTP subsystem is returned. May not be used concurrently.
     *
     * @throws IOException
     */
    protected SFTPv3Client sftp() throws IOException {
        if(null == SFTP) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            if(!SSH.isAuthenticationComplete()) {
                throw new LoginCanceledException();
            }
            this.message(NSBundle.localizedString("Starting SFTP subsystem...", "Status", ""));
            SFTP = new SFTPv3Client(SSH);
            this.message(NSBundle.localizedString("SFTP subsystem ready", "Status", ""));
            SFTP.setCharset(this.getEncoding());
        }
        return SFTP;
    }

    /**
     * Opens a new, dedicated SCP channel for this SSH session
     *
     * @throws IOException
     */
    protected SCPClient openScp() throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(!SSH.isAuthenticationComplete()) {
            throw new LoginCanceledException();
        }
//        this.message(NSBundle.localizedString("Starting SCP subsystem...", "Status", ""));
        final SCPClient client = new SCPClient(SSH);
//        this.message(NSBundle.localizedString("SCP subsystem ready", "Status", ""));
        client.setCharset(this.getEncoding());
        return client;
    }

    protected void connect() throws IOException, ConnectionCanceledException, LoginCanceledException {
        synchronized(this) {
            if(this.isConnected()) {
                return;
            }
            this.fireConnectionWillOpenEvent();

            this.message(MessageFormat.format(NSBundle.localizedString("Opening {0} connection to {1}...", "Status", ""),
                    new Object[]{host.getProtocol().getName(), host.getHostname()}));

            SSH = new Connection(this.host.getHostname(true), this.host.getPort());

            final int timeout = this.timeout();
            SSH.connect(verifier, timeout, timeout);
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            this.message(MessageFormat.format(NSBundle.localizedString("{0} connection opened", "Status", ""),
                    new Object[]{host.getProtocol().getName()}));
            this.login();
            if(!SSH.isAuthenticationComplete()) {
                throw new LoginCanceledException();
            }
            this.fireConnectionDidOpenEvent();
        }
    }

    protected void login() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(!host.getCredentials().check(this.loginController, host.getProtocol(), host.getHostname())) {
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
                host.getCredentials().addInternetPasswordToKeychain(host.getProtocol(),
                        host.getHostname(), host.getPort());
                return;
            }
        }
        this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
        loginController.promptUser(host.getProtocol(), host.getCredentials(),
                NSBundle.localizedString("Login failed", "Credentials", ""),
                NSBundle.localizedString("Login with username and password", "Credentials", ""));
        if(!host.getCredentials().tryAgain()) {
            throw new LoginCanceledException();
        }
        this.login();
    }

    private boolean loginUsingPublicKeyAuthentication(final Login credentials) throws IOException {
        log.debug("loginUsingPublicKeyAuthentication:" + credentials);
        if(SSH.isAuthMethodAvailable(host.getCredentials().getUsername(), "publickey")) {
            Local key = new Local(credentials.getPrivateKeyFile());
            if(key.exists()) {
                // If the private key is passphrase protected then ask for the passphrase
                char[] buff = new char[256];
                CharArrayWriter cw = new CharArrayWriter();
                FileReader fr = new FileReader(new File(key.getAbsolute()));
                while(true) {
                    int len = fr.read(buff);
                    if(len < 0)
                        break;
                    cw.write(buff, 0, len);
                }
                fr.close();
                String passphrase = null;
                if(PEMDecoder.isPEMEncrypted(cw.toCharArray())) {
                    passphrase = Keychain.instance().getPasswordFromKeychain("SSHKeychain", credentials.getPrivateKeyFile());
                    if(null == passphrase || passphrase.equals("")) {
                        loginController.promptUser(host.getProtocol(), host.getCredentials(),
                                NSBundle.localizedString("Private key password protected", "Credentials", ""),
                                NSBundle.localizedString("Enter the passphrase for the private key file", "Credentials", "")
                                        + " (" + credentials.getPrivateKeyFile() + ")");
                        if(host.getCredentials().tryAgain()) {
                            passphrase = credentials.getPassword();
                            if(credentials.usesKeychain() && PEMDecoder.isPEMEncrypted(cw.toCharArray())) {
                                Keychain.instance().addPasswordToKeychain("SSHKeychain", credentials.getPrivateKeyFile(),
                                        passphrase);
                            }
                        }
                        else {
                            throw new LoginCanceledException();
                        }
                    }
                }
                return SSH.authenticateWithPublicKey(host.getCredentials().getUsername(), new File(key.getAbsolute()),
                        passphrase);
            }
            log.error("Key file " + key.getAbsolute() + " does not exist.");
        }
        return false;
    }

    private boolean loginUsingPasswordAuthentication(final Login credentials) throws IOException {
        log.debug("loginUsingPasswordAuthentication:" + credentials);
        if(SSH.isAuthMethodAvailable(host.getCredentials().getUsername(), "password")) {
            return SSH.authenticateWithPassword(credentials.getUsername(), credentials.getPassword());
        }
        return false;
    }

    private boolean loginUsingKBIAuthentication(final Login credentials) throws IOException {
        log.debug("loginUsingKBIAuthentication" +
                "make:" + credentials);
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
            SSH.close(null, true);
        }
        finally {
            SFTP = null;
            SSH = null;
            this.fireActivityStoppedEvent();
            this.fireConnectionDidCloseEvent();
        }
    }

    public void check() throws IOException {
        this.check(true);
    }

    public void check(final boolean sftp) throws IOException {
        try {
            super.check();
        }
        catch(ChannelClosedException e) {
            log.debug(e.getMessage());
            this.interrupt();
            this.connect();
        }
        if(sftp) {
            if(!this.sftp().isConnected()) {
                this.interrupt();
                this.connect();
            }
        }
    }

    protected Path workdir() throws IOException {
        synchronized(this) {
            if(!SFTP.isConnected()) {
                throw new ConnectionCanceledException();
            }
            if(null == workdir) {
                // "." as referring to the current directory
                workdir = PathFactory.createPath(this, this.sftp().canonicalPath("."), Path.DIRECTORY_TYPE);
            }
            return workdir;
        }
    }

    protected void setWorkdir(Path workdir) throws IOException {
        this.workdir = workdir;
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
        try {
            SSH.getConnectionInfo();
        }
        catch(IllegalStateException e) {
            log.debug("isConnected:" + e.getMessage());
            return false;
        }
        catch(IOException e) {
            log.debug("isConnected:" + e.getMessage());
            return false;
        }
        return true;
    }
}
