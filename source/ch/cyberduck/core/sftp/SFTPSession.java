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

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshtoolsPrivateKeyFormat;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.sftp.*;
import com.sshtools.j2ssh.subsystem.*;
import com.sshtools.j2ssh.*;
import ch.cyberduck.core.Preferences;
import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
* Opens a connection to the remote server via sftp protocol
 * @version $Id$
 */
public class SFTPSession extends Session {

    private static Logger log = Logger.getLogger(Session.class);

    class SFTPFile extends Path {
        private SftpFile file;

	public SFTPFile(String parent, String name) {
	    super(parent, name);
	}

	public SFTPFile(String path) {
	    super(path);
	}

	public Path getParent() {
            String abs = this.getAbsolute();
	    if((null == parent) && !abs.equals("/")) {
		int index = abs.lastIndexOf('/');
		String dirname = abs;
		if(index > 0) 
		    dirname = abs.substring(0, index);
		if(index == 0) //parent is root
		    dirname = "/";
		//		try {
  //		    SftpFile file = SFTP.openDirectory(dirname);
  //		    parent = new SFTPFile(file.getAbsolutePath());
		parent = new SFTPFile(dirname);
		//		}
  //		catch(IOException e) {
  //		    e.printStackTrace();
  //		}
	    }
	    log.debug("getParent:"+parent);
	    return parent;
	}
	
	public synchronized void list() {
	    this.list(null == this.cache());
	}
	
	public synchronized void list(boolean refresh) {
	    log.debug("list");
	    if(refresh) {
		new Thread() {
		    public void run() {
			List files = null;
			SftpFile workingDirectory = null;
			boolean showHidden = Preferences.instance().getProperty("listing.showHidden").equals("true");
			//@todo throw exception if we are not a directory
			try {
			    SFTPSession.this.check();
			    SFTPSession.this.log("Listing "+SFTPFile.this.getName(), Message.PROGRESS);
			    workingDirectory = SFTP.openDirectory(SFTPFile.this.getAbsolute());
			    List children = new ArrayList();
			    int read = 1;
			    while(read > 0) {
				read = SFTP.listChildren(workingDirectory, children);
			    }
			    java.util.Iterator i = children.iterator();
			    files = new java.util.ArrayList();
			    while(i.hasNext()) {
				SftpFile x = (SftpFile)i.next();
				if(!x.getFilename().equals(".") && !x.getFilename().equals("..")) {
				    SFTPFile p = new SFTPFile(SFTPFile.this.getAbsolute(), x.getFilename());
				    //log.debug(p.getName());
				    if(p.getName().charAt(0) == '.' && !showHidden) {
					//p.attributes.setVisible(false);
				    }
				    else {
					p.attributes.setOwner(x.getAttributes().getUID().toString());
					p.attributes.setGroup(x.getAttributes().getGID().toString());
					p.status.setSize(x.getAttributes().getSize().intValue());
					p.attributes.setModified(x.getAttributes().getModifiedTime().longValue());
					p.attributes.setMode(x.getAttributes().getPermissionsString());
					p.attributes.setPermission(new Permission(x.getAttributes().getPermissionsString()));
					files.add(p);
				    }
				}
			    }
			    SFTPFile.this.setCache(files);
			    SFTPSession.this.callObservers(SFTPFile.this);
			    SFTPSession.this.log("Listing complete", Message.PROGRESS);
			}
			catch(SshException e) {
			    SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
			    SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
			}
			finally {
			    if(workingDirectory != null) {
				try {
				    workingDirectory.close();
				}
				catch(SshException e) {
				    SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
				}
				catch(IOException e) {
				    SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
				}
			    }
			}
		    }
		}.start();
	    }
	    else {
		SFTPSession.this.callObservers(SFTPFile.this);
	    }
	}

	public synchronized void delete() {
	    log.debug("delete");
	    try {
		SFTPSession.this.check();
		if(this.isDirectory()) {
		    /*
		    List files = this.list();
		    java.util.Iterator iterator = files.iterator();
		    Path file = null;
		    while(iterator.hasNext()) {
			file = (Path)iterator.next();
			if(file.isDirectory()) {
			    file.delete();
			}
			if(file.isFile()) {
			    SFTPSession.this.log("Deleting "+this.getName(), Message.PROGRESS);
			    SFTP.removeFile(file.getAbsolute());
			}
		    }
//		    FTP.cdup();
		     */
		    SFTPSession.this.log("Deleting "+this.getName(), Message.PROGRESS);
		    SFTP.removeDirectory(this.getAbsolute());
		}
		if(this.isFile()) {
		    SFTPSession.this.log("Deleting "+this.getName(), Message.PROGRESS);
		    SFTP.removeFile(this.getAbsolute());
		}
		this.getParent().list(true);
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}

	public synchronized void rename(String filename) {
	    log.debug("rename");
	    try {
		SFTPSession.this.check();
		SFTPSession.this.log("Renaming "+this.getName()+" to "+filename, Message.PROGRESS);
		SFTP.renameFile(this.getAbsolute(), this.getParent().getAbsolute()+"/"+filename);
		this.getParent().list(true);
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}
	    
	public synchronized Path mkdir(String name) {
	    log.debug("mkdir");
	    try {
		SFTPSession.this.check();
		SFTPSession.this.log("Make directory "+name, Message.PROGRESS);
//		SFTP.makeDirectory(this.getAbsolute());
		SFTP.makeDirectory(name);
		this.list(true);
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	    return new SFTPFile(this.getAbsolute(), name);
	}

	public synchronized void changePermissions(int permissions) {
	    log.debug("changePermissions");
	    try {
		SFTPSession.this.check();
//		SFTP.changePermissions(this.getAbsolute(), this.attributes.getPermission().getCode());
		SFTP.changePermissions(this.getAbsolute(), this.attributes.getPermission().getString());
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}

	public synchronized void download() {
            log.debug("download");
	    new Thread() {
		public void run() {
		    SFTPSession downloadSession = new SFTPSession(host);
		    downloadSession.connect();
		    downloadSession.download(SFTPFile.this);

		}
	    }.start();
	}
    }

    private SftpSubsystemClient SFTP;
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
	    SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
	finally {
	    this.setConnected(false);
	}
    }

    public synchronized void connect() {
//		host.status.fireActiveEvent();
	this.callObservers(new Message(Message.OPEN, "Opening session."));
	this.log("Opening SSH connection to " + host.getIp()+"...", Message.PROGRESS);
	try {
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
	}
	catch(SshException e) {
	    this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
    }

    public synchronized void mount() {
	new Thread() {
	    public void run() {
		try {
		    connect();
		    SFTPFile home = (SFTPFile)SFTPSession.this.workdir();
		    home.list();
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

    public Path workdir() throws IOException {
	return new SFTPFile(SFTP.getDefaultDirectory());
    }

    public void download(Path file) {
	log.debug("download:"+file.getName());
	try {
	    file.status.fireActiveEvent();
	    if(file.isDirectory())
		this.downloadFolder(file);
	    if(file.isFile())
		this.downloadFile(file);
	}
	catch(SshException e) {
	    SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
    }

    private void downloadFile(Path file) throws IOException {
	OutputStream out = new FileOutputStream(file.getLocal(), file.status.isResume());
	if(out == null) {
	    throw new IOException("Unable to buffer data");
	}
	SftpFile p = SFTP.openFile(file.getAbsolute(), SftpSubsystemClient.OPEN_READ);
	file.status.setSize(p.getAttributes().getSize().intValue());
	this.log("Opening data stream...", Message.PROGRESS);
	SftpFileInputStream in = new SftpFileInputStream(p);
	if(in == null) {
	    throw new IOException("Unable opening data stream");
	}
	this.log("Downloading "+file.getName()+"...", Message.PROGRESS);
	file.download(in, out);
    }

    private void downloadFolder(Path file) throws IOException {
	log.debug("not implemented");
	/*
	java.util.List files = file.list(); //@todo
	File dir = file.getLocal();
	dir.mkdir();
	java.util.Iterator i = files.iterator();
	while(i.hasNext()) {
	    Path p = (Path)i.next();
	    if(p.isDirectory()) {
		log.debug("changing directory: "+p.toString());
		SFTP.openDirectory(p.getAbsolute());
	    }
	    log.debug("getting file:"+p.toString());
	    this.download(p);
	}
	SFTP.openDirectory("..");
	 */
    }

    public void upload(java.io.File file) {
	try {
	    if(file.isDirectory())
		this.uploadFolder(file);
	    if(file.isFile())
		this.uploadFile(file);
	}
	catch(SshException e) {
	    this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
    }

    private void uploadFile(java.io.File file) throws IOException {
	log.debug("not implemented");
		    //@todo
	/*
	 SftpFile file = SFTP.openFile(file.getName(), SftpSubsystemClient.OPEN_CREATE | SftpSubsystemClient.OPEN_WRITE);
	 SftpFileOutputStream out = new SftpFileOutputStream(file);
	 */
    }

    private void uploadFolder(java.io.File file) throws IOException {
	log.debug("not implemented");
		    //@todo
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
}