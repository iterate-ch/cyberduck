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
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.KBIAuthenticationClient;
import com.sshtools.j2ssh.authentication.KBIPrompt;
import com.sshtools.j2ssh.authentication.KBIRequestHandler;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelEventAdapter;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.Login;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;

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
        synchronized(this) {
            try {
                if (SFTP != null) {
                    SFTP.close();
                    host.getCredentials().setPassword(null);
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
                this.setClosed();
            }
        }
    }

    public void interrupt() {
        try {
            if (null == this.SSH) {
                return;
            }
            this.SSH.getActiveSession("sftp").close();
        }
        catch (SshException e) {
            log.error("SSH Error: " + e.getMessage());
        }
        catch (IOException e) {
            this.log(Message.ERROR, "IO " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
        }
    }

    public void sendCommand(String command) {
        //todo
    }

    private HostKeyVerification hostKeyVerification = new IgnoreHostKeyVerification();

    public void setHostKeyVerificationController(HostKeyVerification h) {
        this.hostKeyVerification = h;
    }

    public HostKeyVerification getHostKeyVerificationController() {
        return this.hostKeyVerification;
    }

    public void connect(String encoding) throws IOException {
        synchronized(this) {
            this.log(Message.PROGRESS, NSBundle.localizedString("Opening SSH connection to", "Status", "") + " " + host.getIp() + "...");
            this.setConnected();
            this.log(Message.TRANSCRIPT, "=====================================");
            this.log(Message.TRANSCRIPT, new java.util.Date().toString());
            this.log(Message.TRANSCRIPT, host.getIp());
            SSH = new SshClient();
            //SSH.setSocketTimeout(Preferences.instance().getInteger("connection.timeout"));
            SSH.addEventHandler(new SshEventAdapter() {
                public void onSocketTimeout(TransportProtocol transport) {
                    log.debug("onSocketTimeout");
                }

                public void onDisconnect(TransportProtocol transport) {
                    log.debug("onDisconnect");
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
                this.log(Message.PROGRESS, NSBundle.localizedString("SSH connection opened", "Status", ""));
                String id = SSH.getServerId();
                this.host.setIdentification(id);
                this.log(Message.TRANSCRIPT, id);
                log.info(SSH.getAvailableAuthMethods(host.getCredentials().getUsername()));
                this.login();
                this.log(Message.PROGRESS, NSBundle.localizedString("Starting SFTP subsystem...", "Status", ""));
                this.SFTP = SSH.openSftpChannel(new ChannelEventAdapter() {
                    public void onDataReceived(Channel channel, byte[] data) {
                        log(Message.TRANSCRIPT, new String(data));
                    }

                    public void onDataSent(Channel channel, byte[] data) {
                        log(Message.TRANSCRIPT, new String(data));
                    }
                }, encoding);
                this.log(Message.PROGRESS, NSBundle.localizedString("SFTP subsystem ready", "Status", ""));
            }
        }
    }

    private int loginUsingKBIAuthentication(final Login credentials) throws IOException {
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


    private int loginUsingPasswordAuthentication(final Login credentials) throws IOException {
        log.info("Trying Password authentication...");
        PasswordAuthenticationClient auth = new PasswordAuthenticationClient();
        auth.setUsername(credentials.getUsername());
        auth.setPassword(credentials.getPassword());
        // Try the authentication
        return SSH.authenticate(auth);
    }

    private int loginUsingPublicKeyAuthentication(Login credentials) throws IOException {
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
                host.setCredentials(credentials.promptUser("The Private Key is password protected. Enter the passphrase for the key file '" + credentials.getPrivateKeyFile() + "'."));
                if (host.getCredentials().tryAgain()) {
                    passphrase = credentials.getPassword();
                    if (keyFile.isPassphraseProtected()) {
                        if (credentials.usesKeychain()) {
                            Keychain.instance().addPasswordToKeychain("SSHKeychain", credentials.getPrivateKeyFile(), passphrase);
                        }
                    }
                }
                else {
                    throw new SshException("Login as user " + credentials.getUsername() + " canceled.");
                }
            }
            NSAutoreleasePool.pop(pool);
        }
        // Get the key
        pk.setKey(keyFile.toPrivateKey(passphrase));
        // Try the authentication
        return SSH.authenticate(pk);
    }

    private void login() throws IOException {
        log.debug("login");
        Login credentials = host.getCredentials();
        if (credentials.check()) {
            this.log(Message.PROGRESS, NSBundle.localizedString("Authenticating as", "Status", "") + " '" + credentials.getUsername() + "'");
            if (credentials.usesPublicKeyAuthentication()) {
                if (AuthenticationProtocolState.COMPLETE == this.loginUsingPublicKeyAuthentication(credentials)) {
                    this.log(Message.PROGRESS, NSBundle.localizedString("Login successful", "Status", ""));
                    this.setAuthenticated();
                    return;
                }
            }
            else {
                if (AuthenticationProtocolState.COMPLETE == this.loginUsingPasswordAuthentication(credentials) ||
                        AuthenticationProtocolState.COMPLETE == this.loginUsingKBIAuthentication(credentials)) {
                    this.log(Message.PROGRESS, NSBundle.localizedString("Login successful", "Status", ""));
                    credentials.addInternetPasswordToKeychain();
                    this.setAuthenticated();
                    return;
                }
            }
            this.log(Message.PROGRESS, NSBundle.localizedString("Login failed", "Status", ""));
            host.setCredentials(credentials.promptUser("Authentication for user " + credentials.getUsername() + " failed.")); //todo localize
            if (host.getCredentials().tryAgain()) {
                this.login();
            }
            else {
                throw new SshException("Login as user " + credentials.getUsername() + " canceled."); //todo localize
            }
        }
        throw new IOException("Login as user " + host.getCredentials().getUsername() + " failed."); //todo localize
    }

    public Path workdir() {
        try {
            Path workdir = PathFactory.createPath(this, SFTP.getDefaultDirectory());
            workdir.attributes.setType(Path.DIRECTORY_TYPE);
            return workdir;
        }
        catch (SshException e) {
            this.log(Message.ERROR, "SSH " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
        }
        catch (IOException e) {
            this.log(Message.ERROR, "IO " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
        }
        return null;
    }

    public void noop() throws IOException {
        synchronized(this) {
            if (this.isConnected()) {
                this.SSH.noop();
            }
        }
    }

    public void check() throws IOException {
        this.log(Message.START, "Working");
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