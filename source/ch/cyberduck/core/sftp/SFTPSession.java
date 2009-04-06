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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
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
        if(super.isSecure()) {
            return SSH.isAuthenticationComplete();
        }
        return false;
    }

    public String getIdentification() {
        StringBuffer info = new StringBuffer(super.getIdentification() + "\n");
        if(SFTP != null) {
            info.append("SFTP Protocol version: ").append(SFTP.getProtocolVersion()).append("\n");
        }
        try {
            final ConnectionInfo i = SSH.getConnectionInfo();
            info.append("Key Exchange (KEX) Algorithm: ").append(i.keyExchangeAlgorithm).append("\n");
            info.append("Number of key exchanges performed on this connection so far: ").append(i.keyExchangeCounter).append("\n");
            info.append("Host Key Algorithm: ").append(i.serverHostKeyAlgorithm).append("\n");
            info.append("Server to Client Crypto Algorithm: ").append(i.serverToClientCryptoAlgorithm).append("\n");
            info.append("Client to Server Crypto Algorithm: ").append(i.clientToServerCryptoAlgorithm).append("\n");
            info.append("Server to Client MAC Algorithm: ").append(i.serverToClientMACAlgorithm).append("\n");
            info.append("Client to Server MAC Algorithm: ").append(i.clientToServerMACAlgorithm).append("\n");
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
            this.message(NSBundle.localizedString("Starting SFTP subsystem", "Status", ""));
            try {
                SFTP = new SFTPv3Client(SSH);
                this.message(NSBundle.localizedString("SFTP subsystem ready", "Status", ""));
                SFTP.setCharset(this.getEncoding());
            }
            catch(IOException e) {
                this.error(null, e.getMessage(), e);
            }
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
//        this.message(NSBundle.localizedString("Starting SCP subsystem", "Status", ""));
        final SCPClient client = new SCPClient(SSH);
//        this.message(NSBundle.localizedString("SCP subsystem ready", "Status", ""));
        client.setCharset(this.getEncoding());
        return client;
    }

    protected void connect() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        this.message(MessageFormat.format(NSBundle.localizedString("Opening {0} connection to {1}", "Status", ""),
                host.getProtocol().getName(), host.getHostname()));

        SSH = new Connection(this.host.getHostname(true), this.host.getPort());

        final int timeout = this.timeout();
        SSH.connect(verifier, timeout, timeout);
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        this.message(MessageFormat.format(NSBundle.localizedString("{0} connection opened", "Status", ""),
                host.getProtocol().getName()));
        this.login();
        if(!SSH.isAuthenticationComplete()) {
            throw new LoginCanceledException();
        }
        this.fireConnectionDidOpenEvent();
    }

    protected void login(final Credentials credentials) throws IOException {
        if(host.getCredentials().isPublicKeyAuthentication()) {
            if(this.loginUsingPublicKeyAuthentication(credentials)) {
                this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
                return;
            }
        }
        else if(this.loginUsingPasswordAuthentication(credentials)
                || this.loginUsingKBIAuthentication(credentials)) {
            this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
            return;
        }
        this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
        this.login.fail(host,
                NSBundle.localizedString("Login with username and password", "Credentials", ""));
        this.login();
    }

    private boolean loginUsingPublicKeyAuthentication(final Credentials credentials) throws IOException {
        log.debug("loginUsingPublicKeyAuthentication:" + credentials);
        if(SSH.isAuthMethodAvailable(host.getCredentials().getUsername(), "publickey")) {
            final Credentials.Identity identity = host.getCredentials().getIdentity();
            if(identity.exists()) {
                // If the private key is passphrase protected then ask for the passphrase
                char[] buff = new char[256];
                CharArrayWriter cw = new CharArrayWriter();
                FileReader fr = new FileReader(new File(identity.getAbsolute()));
                while(true) {
                    int len = fr.read(buff);
                    if(len < 0) {
                        break;
                    }
                    cw.write(buff, 0, len);
                }
                fr.close();
                String passphrase = null;
                if(PEMDecoder.isPEMEncrypted(cw.toCharArray())) {
                    passphrase = Keychain.instance().getPasswordFromKeychain("SSHKeychain", identity.toURL());
                    if(StringUtils.isEmpty(passphrase)) {
                        login.prompt(host,
                                NSBundle.localizedString("Private key password protected", "Credentials", ""),
                                NSBundle.localizedString("Enter the passphrase for the private key file", "Credentials", "")
                                        + " (" + identity + ")");
                        passphrase = credentials.getPassword();
                        if(credentials.usesKeychain() && PEMDecoder.isPEMEncrypted(cw.toCharArray())) {
                            Keychain.instance().addPasswordToKeychain("SSHKeychain", identity.toURL(),
                                    passphrase);
                        }
                    }
                }
                return SSH.authenticateWithPublicKey(host.getCredentials().getUsername(), new File(identity.getAbsolute()),
                        passphrase);
            }
            log.error("Key file " + identity.getAbsolute() + " does not exist.");
        }
        return false;
    }

    private boolean loginUsingPasswordAuthentication(final Credentials credentials) throws IOException {
        log.debug("loginUsingPasswordAuthentication:" + credentials);
        if(SSH.isAuthMethodAvailable(host.getCredentials().getUsername(), "password")) {
            return SSH.authenticateWithPassword(credentials.getUsername(), credentials.getPassword());
        }
        return false;
    }

    private boolean loginUsingKBIAuthentication(final Credentials credentials) throws IOException {
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
        Credentials credentials;

        public InteractiveLogic(final Credentials credentials) {
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
            SFTP = null;
            SSH = null;
            this.fireConnectionDidCloseEvent();
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
            this.fireConnectionDidCloseEvent();
        }
    }

    public void check() throws IOException {
        this.check(true);
    }

    /**
     * @param sftp
     * @throws IOException
     */
    private void check(final boolean sftp) throws IOException {
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

    public Path workdir() throws IOException {
        if(!SFTP.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(null == workdir) {
            // "." as referring to the current directory
            workdir = PathFactory.createPath(this, this.sftp().canonicalPath("."), Path.DIRECTORY_TYPE);
        }
        return workdir;
    }

    protected void noop() throws IOException {
        if(this.isConnected()) {
            SSH.sendIgnorePacket();
        }

    }

    public boolean isSendCommandSupported() {
        return true;
    }

    public boolean isArchiveSupported() {
        return true;
    }

    public boolean isUnarchiveSupported() {
        return true;
    }

    public void sendCommand(String command) throws IOException {
        final ch.ethz.ssh2.Session sess = SSH.openSession();
        try {
            this.message(command);

            sess.execCommand(command, host.getEncoding());

            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));

            // Here is the output from stdout
            while(true) {
                String line = stdoutReader.readLine();
                if(null == line) {
                    break;
                }
                this.log(false, line);
            }
            // Here is the output from stderr
            while(true) {
                String line = stderrReader.readLine();
                if(null == line) {
                    break;
                }
                this.log(false, line);
            }
        }
        finally {
            sess.close();
        }
    }

    public boolean isConnected() {
        if(null == SSH) {
            return false;
        }
//        try {
//            SSH.getConnectionInfo();
//        }
//        catch(IllegalStateException e) {
//            log.debug("isConnected:" + e.getMessage());
//            return false;
//        }
//        catch(IOException e) {
//            log.debug("isConnected:" + e.getMessage());
//            return false;
//        }
        return true;
    }
}
