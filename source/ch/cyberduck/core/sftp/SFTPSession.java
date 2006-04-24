package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SshEventAdapter;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.authentication.*;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelEventAdapter;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

import ch.cyberduck.core.*;

import com.apple.cocoa.foundation.NSAutoreleasePool;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Opens a connection to the remote server via sftp protocol
 *
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

    protected SftpSubsystemClient SFTP;
    private SshClient SSH;

    private SFTPSession(Host h) {
        super(h);
    }

    public boolean isSecure() {
        return true;
    }

    public void close() {
        synchronized (this) {
            this.activityStarted();
            this.connectionWillClose();
            try {
                if (SFTP != null) {
                    SFTP.close();
                    SFTP = null;
                }
                if (SSH != null) {
                    SSH.disconnect();
                    SSH = null;
                }
            }
            catch (SshException e) {
                log.error("SSH Error: " + e.getMessage());
            }
            catch (IOException e) {
                log.error("IO Error: " + e.getMessage());
            }
            finally {
                this.activityStopped();
                this.connectionDidClose();
            }
        }
    }

    public void interrupt() {
        try {
            if (null == this.SSH) {
                return;
            }
            this.SSH.interrupt();
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        finally {
            this.SFTP = null;
            this.connectionDidClose();
        }
    }

    public void sendCommand(String command) {
        log.error("Not implemented");
    }

    private HostKeyVerification hostKeyVerification = new IgnoreHostKeyVerification();

    public void setHostKeyVerificationController(HostKeyVerification h) {
        this.hostKeyVerification = h;
    }

    public HostKeyVerification getHostKeyVerificationController() {
        return this.hostKeyVerification;
    }

    protected void connect() throws IOException, SshException, LoginCanceledException {
        synchronized (this) {
            SessionPool.instance().add(this);
            this.connectionWillOpen();
            this.message(NSBundle.localizedString("Opening SSH connection to", "Status", "") + " " + host.getHostname() + "...");
            this.log("=====================================");
            this.log(new java.util.Date().toString());
            this.log(host.getIp());
            SSH = new SshClient();
            SSH.setSocketTimeout(Preferences.instance().getInteger("connection.timeout"));
            SSH.addEventHandler(new SshEventAdapter() {
                public void onSocketTimeout(TransportProtocol transport) {
                    log.debug("onSocketTimeout");
                }

                public void onDisconnect(TransportProtocol transport) {
                    log.debug(transport.getState().getDisconnectReason());
                }
            });
            SshConnectionProperties properties = new SshConnectionProperties();
            properties.setHost(host.getHostname());
            properties.setPort(host.getPort());
            // Sets the prefered client->server encryption cipher
            properties.setPrefCSEncryption(Preferences.instance().getProperty("ssh.CSEncryption"));
            // Sets the prefered server->client encryption cipher
            properties.setPrefSCEncryption(Preferences.instance().getProperty("ssh.SCEncryption"));
            // Sets the prefered client->server message authentication
            properties.setPrefCSMac(Preferences.instance().getProperty("ssh.CSAuthentication"));
            // Sets the prefered server->client message authentication
            properties.setPrefSCMac(Preferences.instance().getProperty("ssh.SCAuthentication"));
            // Sets the prefered server host key for server authentication
            properties.setPrefPublicKey(Preferences.instance().getProperty("ssh.publickey"));
            // Set the zlib compression
            properties.setPrefSCComp(Preferences.instance().getProperty("ssh.compression"));
            properties.setPrefCSComp(Preferences.instance().getProperty("ssh.compression"));
            if (Proxy.isSOCKSProxyEnabled()) {
                log.info("Using SOCKS Proxy");
                properties.setTransportProvider(SshConnectionProperties.USE_SOCKS4_PROXY);
                properties.setProxyHost(Proxy.getSOCKSProxyHost());
                properties.setProxyPort(Proxy.getSOCKSProxyPort());
            }
            SSH.connect(properties, this.getHostKeyVerificationController());
            if (SSH.isConnected()) {
                this.message(NSBundle.localizedString("SSH connection opened", "Status", ""));
                String id = SSH.getServerId();
                this.log(id);
                this.host.setIdentification(id);
                this.login();
                this.message(NSBundle.localizedString("Starting SFTP subsystem...", "Status", ""));
                this.SFTP = SSH.openSftpChannel(new ChannelEventAdapter() {
                    public void onDataReceived(Channel channel, byte[] data) {
                        log(new String(data));
                    }

                    public void onDataSent(Channel channel, byte[] data) {
                        log(new String(data));
                    }
                }, host.getEncoding());
                this.message(NSBundle.localizedString("SFTP subsystem ready", "Status", ""));
                this.connectionDidOpen();
            }
        }
    }

    private int loginUsingKBIAuthentication(final Login credentials) throws IOException, SshException {
        log.info("Trying Keyboard Interactive (PAM) authentication...");
        KBIAuthenticationClient kbi = new KBIAuthenticationClient();
        kbi.setUsername(credentials.getUsername());
        kbi.setKBIRequestHandler(new KBIRequestHandler() {
            public void showPrompts(String name,
                                    String instructions,
                                    KBIPrompt[] prompts) {
                log.info(name);
                log.info(instructions);
                if (prompts != null) {
                    for (int i = 0; i < prompts.length; i++) {
                        log.info(prompts[i].getPrompt());
                        prompts[i].setResponse(credentials.getPassword());
                    }
                }
            }
        });
        // Try the authentication
        return SSH.authenticate(kbi);
    }


    private int loginUsingPasswordAuthentication(final Login credentials) throws IOException, SshException {
        log.info("Trying Password authentication...");
        PasswordAuthenticationClient auth = new PasswordAuthenticationClient();
        auth.setUsername(credentials.getUsername());
        auth.setPassword(credentials.getPassword());
        // Try the authentication
        return SSH.authenticate(auth);
    }

    private int loginUsingPublicKeyAuthentication(Login credentials) throws IOException, SshException {
        log.info("Trying Public Key authentication...");
        PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();
        pk.setUsername(credentials.getUsername());
        // Get the private key file
        SshPrivateKeyFile keyFile = SshPrivateKeyFile.parse(new java.io.File(credentials.getPrivateKeyFile()));
        // If the private key is passphrase protected then ask for the passphrase
        String passphrase = null;
        if (keyFile.isPassphraseProtected()) {
            int pool = NSAutoreleasePool.push();
            passphrase = Keychain.instance().getPasswordFromKeychain("SSHKeychain", credentials.getPrivateKeyFile());
            if (null == passphrase || passphrase.equals("")) {
                loginController.promptUser(host.getCredentials(),
                        NSBundle.localizedString("Private key password protected", "Credentials", ""),
                        NSBundle.localizedString("Enter the passphrase for the private key file", "Credentials", "")
                                + " (" + credentials.getPrivateKeyFile() + ")");
                if (host.getCredentials().tryAgain()) {
                    passphrase = credentials.getPassword();
                    if (keyFile.isPassphraseProtected()) {
                        if (credentials.usesKeychain()) {
                            Keychain.instance().addPasswordToKeychain("SSHKeychain", credentials.getPrivateKeyFile(),
                                    passphrase);
                        }
                    }
                }
                else {
                    throw new SshException(
                            NSBundle.localizedString("Login canceled", "Credentials", ""));
                }
            }
            NSAutoreleasePool.pop(pool);
        }
        // Get the key
        pk.setKey(keyFile.toPrivateKey(passphrase));
        // Try the authentication
        return SSH.authenticate(pk);
    }

    protected void login() throws IOException, SshException, LoginCanceledException {
        log.debug("login");
        if (host.getCredentials().check(this.loginController)) {
            this.message(NSBundle.localizedString("Authenticating as", "Status", "") + " '"
                    + host.getCredentials().getUsername() + "'");
            if (host.getCredentials().usesPublicKeyAuthentication()) {
                if (AuthenticationProtocolState.COMPLETE == this.loginUsingPublicKeyAuthentication(host.getCredentials()))
                {
                    this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
                    return;
                }
            }
            else {
                if (AuthenticationProtocolState.COMPLETE == this.loginUsingPasswordAuthentication(host.getCredentials()) ||
                        AuthenticationProtocolState.COMPLETE == this.loginUsingKBIAuthentication(host.getCredentials()))
                {
                    this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
                    host.getCredentials().addInternetPasswordToKeychain();
                    return;
                }
            }
            this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
            loginController.promptUser(host.getCredentials(),
                    NSBundle.localizedString("Login failed", "Credentials", ""),
                    NSBundle.localizedString("Login with username and password", "Credentials", ""));
            if (!host.getCredentials().tryAgain()) {
                throw new LoginCanceledException();
            }
            this.login();
        }
        else {
            throw new LoginCanceledException();
        }
    }

    public Path workdir() {
        try {
            Path workdir = PathFactory.createPath(this, SFTP.getDefaultDirectory());
            workdir.attributes.setType(Path.DIRECTORY_TYPE);
            return workdir;
        }
        catch (IOException e) {
            this.error(e);
        }
        return null;
    }

    protected void noop() throws IOException, SshException {
        synchronized (this) {
            if (this.isConnected()) {
                this.SSH.noop();
            }
        }
    }

    public void check() throws IOException {
        this.activityStarted();
        if (null == this.SSH) {
            this.connect();
            return;
        }
        this.host.getIp();
        if (!this.SSH.isConnected()) {
            this.close();
            this.connect();
        }
    }
}