package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Opens a connection to the remote server via sftp protocol
 * @version $Id$
 */
public class SFTPSession extends Session {

	private static Logger log = Logger.getLogger(Session.class);

	protected SftpSubsystemClient SFTP;
	private SshClient SSH;

	/**
	 * @param client The client to use which does implement the ftp protocol
	 * @param action The <code>TransferAction</code> to execute after the connection has been opened
	 * @param transfer The <code>Bookmark</code> object
	 * @param secure If the connection is secure
	 */
	public SFTPSession(Host h) {
		super(h);
//		SSH = new SshClient();
	}

	public synchronized void close() {
		this.callObservers(new Message(Message.CLOSE, "Closing session."));
		try {
			if (this.SFTP != null) {
				this.log("Disconnecting...", Message.PROGRESS);
				this.SFTP.close();
				this.SFTP = null;
			}
			if (this.SSH != null) {
				this.log("Closing SSH Session Channel", Message.PROGRESS);
				this.SSH.disconnect();
				this.SSH = null;
			}
			this.log("Disconnected", Message.PROGRESS);
		}
		catch (SshException e) {
			this.log("SSH Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			this.setConnected(false);
		}
	}

	public synchronized void connect() throws IOException {
		this.callObservers(new Message(Message.OPEN, "Opening session."));
		this.log("Opening SSH connection to " + host.getIp() + "...", Message.PROGRESS);
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
		this.log(SSH.getServerId(), Message.TRANSCRIPT);

		log.info(SSH.getAvailableAuthMethods(host.getLogin().getUsername()));
		this.login();
		this.log("Starting SFTP subsystem...", Message.PROGRESS);
		this.SFTP = SSH.openSftpChannel();
		this.log("SFTP subsystem ready", Message.PROGRESS);
		this.setConnected(true);
	}

	public synchronized void mount() {
		new Thread() {
			public void run() {
				try {
					connect();
					SFTPPath home;
					if (host.hasReasonableDefaultPath()) {
						if (host.getDefaultPath().charAt(0) != '/')
							home = new SFTPPath(SFTPSession.this, ((SFTPPath) SFTPSession.this.workdir()).getAbsolute(), host.getDefaultPath());
						else
							home = new SFTPPath(SFTPSession.this, host.getDefaultPath());
					}
					else
						home = (SFTPPath) SFTPSession.this.workdir();
					home.list();
				}
				catch (SshException e) {
					SFTPSession.this.log("SSH Error: " + e.getMessage(), Message.ERROR);
				}
				catch (IOException e) {
					SFTPSession.this.log("IO Error: " + e.getMessage(), Message.ERROR);
				}
			}
		}.start();
	}

	private synchronized void login() throws IOException {
		log.debug("login");
//@todo		if (!host.getLogin().hasReasonableValues()) {
//			host.getLogin().getController().loginFailure("The username or password is not reasonable.");
//		}
		this.log("Authenticating as '" + host.getLogin().getUsername() + "'", Message.PROGRESS);
		if (host.getLogin().usesPasswordAuthentication()) {// password authentication
			PasswordAuthenticationClient auth = new PasswordAuthenticationClient();
			auth.setUsername(host.getLogin().getUsername());
			auth.setPassword(host.getLogin().getPassword());

			// Try the authentication
			int result = SSH.authenticate(auth);
			//	this.log(SSH.getAuthenticationBanner(100), Message.TRANSCRIPT);
// Evaluate the result
			if (AuthenticationProtocolState.COMPLETE == result) {
				this.log("Login successfull", Message.PROGRESS);
			}
			else {
				this.log("Login failed", Message.PROGRESS);
				String explanation = null;
				if (AuthenticationProtocolState.PARTIAL == result)
					explanation = "Authentication as user " + host.getLogin().getUsername() + " succeeded but another authentication method is required.";
				else //(AuthenticationProtocolState.FAILED == result)
					explanation = "Authentication as user " + host.getLogin().getUsername() + " failed.";
				if (host.getLogin().getController().loginFailure(explanation))
					this.login();
				else {
					throw new SshException("Login as user " + host.getLogin().getUsername() + " failed.");
				}
			}
		}
		else if (host.getLogin().usesPublicKeyAuthentication()) {//public key authentication
			PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();
			pk.setUsername(host.getLogin().getUsername());
			// Get the private key file
			SshPrivateKeyFile keyFile = SshPrivateKeyFile.parse(new java.io.File(host.getLogin().getPrivateKeyFile()));
			// If the private key is passphrase protected then ask for the passphrase
			String passphrase = null;
			if (keyFile.isPassphraseProtected()) {
				if (host.getLogin().getController().loginFailure("The Private Key is password protected. Enter the passphrase for the key file '" + host.getLogin().getPrivateKeyFile() + "'.")) {
					passphrase = host.getLogin().getPassword();
				}
				else {
					throw new SshException("Login as user " + host.getLogin().getUsername() + " failed.");
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
			}
			else {
				this.log("Login failed", Message.PROGRESS);
				throw new SshException("Login as user " + host.getLogin().getUsername() + " failed.");
			}
		}
		else {
			this.log("No authentication method specified", Message.ERROR);
		}
	}

	public Path workdir() {
		try {
			return new SFTPPath(this, SFTP.getDefaultDirectory());
		}
		catch (SshException e) {
			this.log("SSH Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		return null;
	}

	public void check() throws IOException {
		log.debug(this.toString() + ":check");
		this.log("Working", Message.START);
		if (null == this.SSH || !SSH.isConnected()) {
			this.setConnected(false);
			this.close();
			this.connect();
			while (true) {
				if (this.isConnected())
					return;
				this.log("Waiting for connection...", Message.PROGRESS);
				Thread.yield();
			}
		}
	}

	public Session copy() {
		return new SFTPSession(this.host);
	}
}