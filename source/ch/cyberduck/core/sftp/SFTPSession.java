package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

/**
 * Opens a connection to the remote server via sftp protocol
 *
 * @version $Id$
 */
public class SFTPSession extends Session {
    private static Logger log = Logger.getLogger(Session.class);

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

    public SFTPSession(Host h) {
        super(h);
    }

    public synchronized void close() {
//		this.callObservers(new Message(Message.CLOSE, "Closing session."));
        try {
            if (this.SFTP != null) {
                this.log("Disconnecting...", Message.PROGRESS);
                this.SFTP.close();
                this.host.getLogin().setPassword(null);
                this.SFTP = null;
            }
            if (this.SSH != null) {
                this.log("Closing SSH Session Channel", Message.PROGRESS);
                this.SSH.disconnect();
                this.SSH = null;
            }
        }
        catch (SshException e) {
            this.log("SSH Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            this.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            this.log("Disconnected", Message.PROGRESS);
            this.setConnected(false);
        }
    }

    public synchronized void connect() throws IOException {
        this.callObservers(new Message(Message.OPEN, "Opening session."));
        this.log("Opening SSH connection to " + host.getIp() + "...", Message.PROGRESS);
        this.log(new java.util.Date().toString(), Message.TRANSCRIPT);
        this.log(host.getIp(), Message.TRANSCRIPT);
        SSH = new SshClient();
        SshConnectionProperties properties = new SshConnectionProperties();
        properties.setHost(host.getHostname());
        properties.setPort(host.getPort());

        // Sets the prefered client->server encryption cipher
        properties.setPrefCSEncryption(Preferences.instance().getProperty("ssh.CSEncryption"));
        // Sets the preffered server->client encryption cipher
        properties.setPrefSCEncryption(Preferences.instance().getProperty("ssh.SCEncryption"));
        // Sets the preffered client->server message authentication
        properties.setPrefCSMac(Preferences.instance().getProperty("ssh.CSAuthentication"));
        // Sets the preffered server->client message authentication
        properties.setPrefSCMac(Preferences.instance().getProperty("ssh.SCAuthentication"));
        // Sets the preferred server host key for server authentication
        properties.setPrefPublicKey(Preferences.instance().getProperty("ssh.publickey"));
        // Set the zlib compression
        properties.setPrefSCComp(Preferences.instance().getProperty("ssh.compression"));
        properties.setPrefCSComp(Preferences.instance().getProperty("ssh.compression"));

        this.log("Opening SSH session...", Message.PROGRESS);
        SSH.connect(properties, host.getHostKeyVerificationController());
        this.log("SSH connection opened", Message.PROGRESS);
        String id = SSH.getServerId();
        this.host.setIdentification(id);
        this.log(id, Message.TRANSCRIPT);

        log.info(SSH.getAvailableAuthMethods(host.getLogin().getUsername()));
        this.login();
        this.log("Starting SFTP subsystem...", Message.PROGRESS);
        this.SFTP = SSH.openSftpChannel();
        this.log("SFTP subsystem ready", Message.PROGRESS);
        this.setConnected(true);
    }

    private synchronized void login() throws IOException {
        log.debug("login");
        Login credentials = host.getLogin();
//		List authMethods = SSH.getAvailableAuthMethods(credentials.getUsername());

        if (host.getLogin().usesPasswordAuthentication()) {// password authentication
            if (credentials.check()) {
                this.log("Authenticating as '" + credentials.getUsername() + "'", Message.PROGRESS);

                PasswordAuthenticationClient auth = new PasswordAuthenticationClient();
                auth.setUsername(credentials.getUsername());
                auth.setPassword(credentials.getPassword());

                // Try the authentication
                int result = SSH.authenticate(auth);
                if (AuthenticationProtocolState.COMPLETE == result) {
                    this.log("Login successful", Message.PROGRESS);
                    credentials.addPasswordToKeychain();
                }
                else {
                    this.log("Login failed", Message.PROGRESS);
                    if (AuthenticationProtocolState.PARTIAL == result) {
                        throw new SshException("Authentication as user " + credentials.getUsername() + " succeeded but another authentication method is required.");
                    }
                    else if (AuthenticationProtocolState.FAILED == result) {
                        if (credentials.promptUser("Authentication as user " + credentials.getUsername() + " failed.")) {
                            this.login();
                        }
                        else {
                            throw new SshException("Login as user " + credentials.getUsername() + " canceled.");
                        }
                    }
                    else {
                        throw new SshException("Login as user " + credentials.getUsername() + " failed for an unknown reason.");
                    }
                }
            }
        }
        else if (credentials.usesPublicKeyAuthentication()) {//public key authentication
            PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();
            pk.setUsername(credentials.getUsername());
            // Get the private key file
            SshPrivateKeyFile keyFile = SshPrivateKeyFile.parse(new java.io.File(credentials.getPrivateKeyFile()));
            // If the private key is passphrase protected then ask for the passphrase
            String passphrase = null;
            if (keyFile.isPassphraseProtected()) {
                passphrase = credentials.getPasswordFromKeychain("SSHKeychain", credentials.getPrivateKeyFile());
                if (null == passphrase || passphrase.equals("")) {
                    if (host.getLogin().promptUser("The Private Key is password protected. Enter the passphrase for the key file '" + credentials.getPrivateKeyFile() + "'.")) {
                        passphrase = credentials.getPassword();
                    }
                    else {
                        throw new SshException("Login as user " + credentials.getUsername() + " failed.");
                    }
                }
            }
            // Get the key
            SshPrivateKey key = keyFile.toPrivateKey(passphrase);
            pk.setKey(key);
            // Try the authentication
            int result = SSH.authenticate(pk);
            // Evaluate the result
            if (AuthenticationProtocolState.COMPLETE == result) {
                this.log("Login sucessfull", Message.PROGRESS);
                if (keyFile.isPassphraseProtected()) {
                    credentials.addPasswordToKeychain("SSHKeychain", credentials.getPrivateKeyFile(), passphrase);
                }
            }
            else {
                this.log("Login failed", Message.PROGRESS);
                throw new SshException("Login as user " + credentials.getUsername() + " failed.");
            }
        }
        else {
            this.log("No authentication method specified", Message.ERROR);
        }
    }

    public synchronized Path workdir() {
        try {
            return PathFactory.createPath(this, SFTP.getDefaultDirectory());
        }
        catch (SshException e) {
            this.log("SSH Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            this.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        return null;
    }

    public synchronized void check() throws IOException {
        log.debug(this.toString() + ":check");
        this.log("Working", Message.START);
		this.log("Checking connection...", Message.PROGRESS);
        if (null == this.SSH || !SSH.isConnected()) {
            this.setConnected(false);
            this.close();
            this.connect();
        }
    }
}