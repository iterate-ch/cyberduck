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
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SshEventAdapter;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.authentication.*;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

import ch.cyberduck.core.*;

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

	public synchronized void close() {
		try {
			if(this.SFTP != null) {
				this.log("Disconnecting...", Message.PROGRESS);
				this.SFTP.close();
				this.host.getCredentials().setPassword(null);
				this.SFTP = null;
			}
			if(this.SSH != null) {
				this.log("Closing SSH Session Channel", Message.PROGRESS);
				this.SSH.disconnect();
				this.SSH = null;
			}
		}
		catch(SshException e) {
			log.error("SSH Error: "+e.getMessage());
		}
		catch(IOException e) {
			log.error("IO Error: "+e.getMessage());
		}
		finally {
			this.log("Disconnected", Message.PROGRESS);
			this.setClosed();
		}
	}

	public synchronized void connect(String encoding) throws IOException {
		this.log("Opening SSH connection to "+host.getIp()+"...", Message.PROGRESS);
		this.setConnected();
		this.log("=====================================", Message.TRANSCRIPT);
		this.log(new java.util.Date().toString(), Message.TRANSCRIPT);
		this.log(host.getIp(), Message.TRANSCRIPT);
		SSH = new SshClient();
		//SSH.setSocketTimeout(Preferences.instance().getInteger("connection.timeout"));
		SSH.addEventHandler(new SshEventAdapter() {
			public void onSocketTimeout(TransportProtocol transport) {
				log.debug("onSocketTimeout");
				SFTPSession.this.close();
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
		if(Proxy.isSOCKSProxyEnabled()) {
			log.info("Using SOCKS Proxy");
			properties.setTransportProvider(SshConnectionProperties.USE_SOCKS5_PROXY); //todo V4?
			properties.setProxyHost(Proxy.getSOCKSProxyHost());
			properties.setProxyPort(Proxy.getSOCKSProxyPort());
			if(Proxy.isSOCKSAuthenticationEnabled()) {
				properties.setProxyUsername(Proxy.getSOCKSProxyUser());
				properties.setProxyPassword(Proxy.getSOCKSProxyPassword());
			}
		}
		SSH.connect(properties, host.getHostKeyVerificationController());
		if(SSH.isConnected()) {
			this.log("SSH connection opened", Message.PROGRESS);
			String id = SSH.getServerId();
			this.host.setIdentification(id);
			this.log(id, Message.TRANSCRIPT);
			log.info(SSH.getAvailableAuthMethods(host.getCredentials().getUsername()));
			this.login();
			this.log("Starting SFTP subsystem...", Message.PROGRESS);
			this.SFTP = SSH.openSftpChannel(encoding);
			this.log("SFTP subsystem ready", Message.PROGRESS);
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
				if(prompts != null) {
					for(int i = 0; i < prompts.length; i++) {
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
		if(keyFile.isPassphraseProtected()) {
			passphrase = credentials.getPasswordFromKeychain("SSHKeychain", credentials.getPrivateKeyFile());
			if(null == passphrase || passphrase.equals("")) {
				host.setCredentials(credentials.promptUser("The Private Key is password protected. Enter the passphrase for the key file '"+credentials.getPrivateKeyFile()+"'."));
				if(host.getCredentials().tryAgain()) {
					passphrase = credentials.getPassword();
					if(keyFile.isPassphraseProtected()) {
						if(credentials.usesKeychain()) {
							credentials.addPasswordToKeychain("SSHKeychain", credentials.getPrivateKeyFile(), passphrase);
						}
					}
				}
				else {
					throw new SshException("Login as user "+credentials.getUsername()+" canceled.");
				}
			}
		}
		// Get the key
		pk.setKey(keyFile.toPrivateKey(passphrase));
		// Try the authentication
		return SSH.authenticate(pk);
	}

	private synchronized void login() throws IOException {
		log.debug("login");
		Login credentials = host.getCredentials();
		if(credentials.check()) {
			this.log("Authenticating as '"+credentials.getUsername()+"'", Message.PROGRESS);
			if(credentials.usesPublicKeyAuthentication()) {
				if(AuthenticationProtocolState.COMPLETE == this.loginUsingPublicKeyAuthentication(credentials)) {
					this.log("Login successful", Message.PROGRESS);
					this.setAuthenticated();
					return;
				}
			}
			else {
				if(AuthenticationProtocolState.COMPLETE == this.loginUsingPasswordAuthentication(credentials) ||
				    AuthenticationProtocolState.COMPLETE == this.loginUsingKBIAuthentication(credentials)) {
					this.log("Login successful", Message.PROGRESS);
					credentials.addInternetPasswordToKeychain();
					this.setAuthenticated();
					return;
				}
			}
			this.log("Login failed", Message.PROGRESS);
			host.setCredentials(credentials.promptUser("Authentication for user "+credentials.getUsername()+" failed."));
			if(host.getCredentials().tryAgain()) {
				this.login();
			}
			else {
				throw new SshException("Login as user "+credentials.getUsername()+" canceled.");
			}
		}
		throw new IOException("Login as user "+host.getCredentials().getUsername()+" failed.");
	}

	public synchronized Path workdir() {
		try {
			return PathFactory.createPath(this, SFTP.getDefaultDirectory());
		}
		catch(SshException e) {
			this.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			this.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		return null;
	}

	public synchronized void noop() throws IOException {
		this.SSH.noop();
	}

	public synchronized void check() throws IOException {
		this.log("Working", Message.START);
		//		this.log("Checking connection...", Message.PROGRESS);
		if(null == this.SSH) {
			this.connect();
			return;
		}
		this.host.getIp();
		if(!this.SSH.isConnected()) {
			this.close();
			this.connect();
		}
	}
}