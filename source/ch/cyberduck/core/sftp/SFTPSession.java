package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
* Opens a connection to the remote server via sftp protocol
 * @version $Id$
 */
public class SFTPSession extends Session {

    private static Logger log = Logger.getLogger(Session.class);

    protected SftpSubsystemClient SFTP;
    private SshClient SSH;
    private SessionChannelClient channel;

    /**
	* @param client The client to use which does implement the ftp protocol
     * @param action The <code>TransferAction</code> to execute after the connection has been opened
     * @param transfer The <code>Bookmark</code> object
     * @param secure If the connection is secure
     */
    public SFTPSession(Host h) {
	super(h);
	SSH = new SshClient();
    }

    public synchronized void close() {
	this.callObservers(new Message(Message.CLOSE, "Closing session."));
	try {
	    if(channel != null) {
		this.log("Closing SSH Session Channel", Message.PROGRESS);
		channel.close();
	    }
	    if(SSH != null) {
		this.log("Disconnecting...", Message.PROGRESS);
		SSH.disconnect();
	    }
	    this.log("Disconnected", Message.PROGRESS);
	}
	catch(SshException e) {
	    this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
	finally {
	    this.setConnected(false);
	}
    }

    public synchronized void connect() throws IOException {
//		host.status.fireActiveEvent();
	this.callObservers(new Message(Message.OPEN, "Opening session."));
	this.log("Opening SSH connection to " + host.getIp()+"...", Message.PROGRESS);
	SshConnectionProperties properties = new SshConnectionProperties();
	properties.setHost(host.getName());
	properties.setPort(host.getPort());
	
		    // Sets the prefered client->server encryption cipher
//		    properties.setPrefCSEncryption("blowfish-cbc");
		    // Sets the preffered server->client encryption cipher
//		    properties.setPrefSCEncryption("3des-cbc");
	
		    // Sets the preffered client->server message authenticaiton
//		    properties.setPrefCSMac("hmac-sha1");
		    // Sets the preffered server->client message authentication
//		    properties.setPrefSCMac("hmac-md5");

	SSH.connect(properties, host.getHostKeyVerification());
	this.log("SSH connection opened", Message.PROGRESS);
	this.log(SSH.getServerId(), Message.TRANSCRIPT);

	log.debug(SSH.getAvailableAuthMethods(host.login.getUsername()));

	this.login();
	this.log("Opening SSH session channel", Message.PROGRESS);
		    // The connection is authenticated we can now do some real work!
	channel = SSH.openSessionChannel();
	this.log("Starting SFTP subsystem", Message.PROGRESS);
	SFTP = new SftpSubsystemClient();
	channel.startSubsystem(SFTP);
	this.log("SFTP subsystem ready.", Message.PROGRESS);
	this.setConnected(true);
    }

    public synchronized void mount() {
	new Thread() {
	    public void run() {
		try {
		    connect();
		    SFTPPath home = (SFTPPath)SFTPSession.this.workdir();
		    home.list();
		}
		catch(SshException e) {
		    SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
		    SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
	    }
	}.start();
    }

    private synchronized void login() throws IOException {
	log.debug("login");
	// password authentication
	this.log("Authenticating as '"+host.login.getUsername()+"'", Message.PROGRESS);
	PasswordAuthenticationClient auth = new PasswordAuthenticationClient();
	auth.setUsername(host.login.getUsername());
	auth.setPassword(host.login.getPassword());

	// Try the authentication
	int result = SSH.authenticate(auth);
//	this.log(SSH.getAuthenticationBanner(100), Message.TRANSCRIPT);
	// Evaluate the result
	if (AuthenticationProtocolState.COMPLETE == result) {
	    this.log("Login sucessfull", Message.PROGRESS);
	}
	else {
	    this.log("Login failed", Message.PROGRESS);
	    String explanation = null;
	    if(AuthenticationProtocolState.PARTIAL == result)
		explanation = "Authentication as user "+host.login.getUsername()+" succeeded but another authentication method is required.";
	    else //(AuthenticationProtocolState.FAILED == result)
		explanation = "Authentication as user "+host.login.getUsername()+" failed.";
	    if(host.getLogin().loginFailure(explanation))
		this.login();
	    else {
		throw new SshException("Login as user "+host.login.getUsername()+" failed.");
	    }
	}

	/*
	//public key authentication
	PublicKeyAuthenticationClient auth = new PublicKeyAuthenticationClient();
	auth.setUsername(host.login.getUsername());
	// Open up the private key file
	SshPrivateKeyFile file = SshPrivateKeyFile.parse(new File(System.getProperty("user.home"), ".ssh/privateKey"));
	// Get the key
	SshPrivateKey key = file.toPrivateKey(host.login.getPassword());
	// Set the key and authenticate
	auth.setKey(key);
	int result = session.authenticate(auth);
	 */
	
    }

    public Path workdir() {
	try {
	    this.check();
	    return new SFTPPath(this, SFTP.getDefaultDirectory());
	}
	catch(SshException e) {
	    this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
	return  null;
    }

    public void check() throws IOException {
	log.debug("check");
	if(!SSH.isConnected()) {
	  //  host.recycle();
	    this.setConnected(false);
	    this.connect();
	    while(true) {
		if(this.isConnected())
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