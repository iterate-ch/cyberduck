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
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.sftp.*;
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
//	    log.debug("getParent");
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
	
	public List list() {
	    log.debug("list");
	    host.callObservers(this);
	    List files = null;
	    SftpFile workingDirectory = null;
	    //@todo throw exception if we are not a directory
	    try {
		SFTPSession.this.check();
		SFTPSession.this.log("Listing "+this.getName(), Message.PROGRESS);
		workingDirectory = SFTP.openDirectory(this.getAbsolute());
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
			SFTPFile f = new SFTPFile(this.getAbsolute(), x.getFilename());
			log.debug(f.getName());
			//don't use this, because like this no cached data is used //SFTPFile f = new SFTPFile(x.getAbsolutePath());
			if(!Preferences.instance().getProperty("ftp.showHidden").equals("true")) {
			    if(f.getName().charAt(0) == '.') {
				f.attributes.setVisible(false);
			    }
			}
			f.attributes.setOwner(x.getAttributes().getUID().toString());
			f.attributes.setGroup(x.getAttributes().getGID().toString());
			f.attributes.setSize(x.getAttributes().getSize().intValue());
			f.attributes.setModified(x.getAttributes().getModifiedTime().longValue());
			f.attributes.setMode(x.getAttributes().getPermissionsString());
			f.attributes.setPermission(new Permission(x.getAttributes().getPermissionsString()));
			//			    SFTPSession.this.log(f.getName(), Message.PROGRESS);
   //			    log.debug("Adding "+f.getAbsolute()+" to file listing.");
			files.add(f);
		    }
		}
		SFTPSession.this.log("Listing complete", Message.PROGRESS);
		file = workingDirectory;
		host.callObservers(files);
		host.callObservers(this);
		//		}
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
	    return files;
	}

	public void delete() {
	    log.debug("delete");
	    try {
		SFTPSession.this.check();
		if(this.isDirectory()) {
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
		    SFTPSession.this.log("Deleting "+this.getName(), Message.PROGRESS);
		    SFTP.removeDirectory(this.getAbsolute());
		}
		if(this.isFile()) {
		    SFTPSession.this.log("Deleting "+this.getName(), Message.PROGRESS);
		    SFTP.removeFile(this.getAbsolute());
		}
		this.getParent().list();
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}

	public void rename(String filename) {
	    log.debug("rename");
	    try {
		SFTPSession.this.check();
		SFTPSession.this.log("Renaming "+this.getName()+" to "+filename, Message.PROGRESS);
		SFTP.renameFile(this.getAbsolute(), this.getParent().getAbsolute()+"/"+filename);
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}
	    
	public void mkdir() {
	    log.debug("mkdir");
	    try {
		SFTPSession.this.check();
		SFTPSession.this.log("Make directory "+this.getName(), Message.PROGRESS);
		SFTP.makeDirectory(this.getAbsolute());
		this.getParent().list();
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}

	public void changePermissions(int permissions) {
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

        public void download() {
	    log.debug("download");
	    new Thread() {
		public void run() {
		    try {
			SFTPFile.this.status.fireActiveEvent();
			SFTPSession.this.check();
			if(SFTPFile.this.isDirectory())
			    this.downloadFolder();
			if(SFTPFile.this.isFile())
			    this.downloadFile();
		    }
		    catch(SshException e) {
			SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
		    }
		    catch(IOException e) {
			SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
		    }
		}

		public void downloadFile() throws IOException {
		    OutputStream out = new FileOutputStream(SFTPFile.this.getLocal(), SFTPFile.this.status.isResume());
		    if(out == null) {
			throw new IOException("Unable to buffer data");
		    }
		    SftpFile file = SFTP.openFile(SFTPFile.this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
		    SFTPFile.this.attributes.setSize(file.getAttributes().getSize().intValue());
		    SFTPSession.this.log("Opening data stream...", Message.PROGRESS);
		    SftpFileInputStream in = new SftpFileInputStream(file);
		    if(in == null) {
			throw new IOException("Unable opening data stream");
		    }
		    SFTPSession.this.log("Downloading "+this.getName()+"...", Message.PROGRESS);
		    SFTPFile.this.download(in, out);
		}

		private void downloadFolder() throws IOException {
		    java.util.List files = SFTPFile.this.list();
		    File dir = SFTPFile.this.getLocal();
		    dir.mkdir();
		    java.util.Iterator i = files.iterator();
		    while(i.hasNext()) {
			Path r = (Path)i.next();
			r.download();
			/*
			if(r.isDirectory()) {
//			    FTP.chdir(r.getAbsolute());
			    r.download();
			}
			if(r.isFile()) {
			    r.download();
			}
			 */
		    }
		}
	    }.start();
        }

        public void upload() {
	    log.debug("upload");
	    new Thread() {
		public void run() {
		    try {
			SFTPFile.this.status.fireActiveEvent();
			SFTPSession.this.check();
			if(SFTPFile.this.isDirectory())
			    this.uploadFolder();
			if(SFTPFile.this.isFile())
			    this.uploadFile();			
		    }
		    catch(SshException e) {
			SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
		    }
		    catch(IOException e) {
			SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
		    }
		}

		public void uploadFile() throws IOException {
		    log.debug("not implemented");
		    //@todo
		    /*
		    SftpFile file = SFTP.openFile(SFTPFile.this.getName(), SftpSubsystemClient.OPEN_CREATE | SftpSubsystemClient.OPEN_WRITE);
		    SftpFileOutputStream out = new SftpFileOutputStream(SFTPFile.this.getLocal());
*/
		}

		public void uploadFolder() throws IOException {
		    log.debug("not implemented");
		    //@todo
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

    public void close() {
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
	host.status.fireStopEvent();
    }

public void connect() {
    new Thread() {
	public void run() {
	    host.status.fireActiveEvent();
	    SFTPSession.this.log("Opening SSH connection to " + host.getIp()+"...", Message.PROGRESS);
	    try {
/*
import com.sshtools.j2ssh.configuration.SshConnectionProperties
 SshConnectionProperties properties = new SshConnectionProperties();
 properties.setHost("firestar");
 properties.setPort(22);
 ssh.connect(properties);

 // Sets the prefered client->server encryption cipher
 properties.setPrefCSEncryption("blowfish-cbc");
 // Sets the preffered server->client encryption cipher
 properties.setPrefSCEncryption("3des-cbc");

 // Sets the preffered client->server message authenticaiton
 properties.setPrefCSMac("hmac-sha1");
 // Sets the preffered server->client message authentication
 properties.setPrefSCMac("hmac-md5");
 */
		SSH.connect(host.getName(), host.getHostKeyVerification());
		SFTPSession.this.log(SSH.getServerId(), Message.TRANSCRIPT);
		SFTPSession.this.login();
		String path = host.getWorkdir().equals(Preferences.instance().getProperty("connection.path.default")) ? SFTP.getDefaultDirectory() : host.getWorkdir();
		SFTPFile home = new SFTPFile(path);
		home.list();
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	    finally {
		host.status.fireStopEvent();
	    }
	}
    }.start();
}

    private void login() throws IOException {
	// Create a password authentication instance
	this.log("Authenticating as '"+host.login.getUsername()+"'", Message.PROGRESS);
	PasswordAuthenticationClient auth = new PasswordAuthenticationClient();
	auth.setUsername(host.login.getUsername());
	auth.setPassword(host.login.getPassword());

	// Try the authentication
	int result = SSH.authenticate(auth);
	this.log(SSH.getAuthenticationBanner(1), Message.TRANSCRIPT);
	// Evaluate the result
	if (result == AuthenticationProtocolState.COMPLETE) {
	    this.log("Login sucessfull", Message.PROGRESS);
//	    this.log("Opening SSH session channel", Message.PROGRESS);
	    // The connection is authenticated we can now do some real work!
	    channel = SSH.openSessionChannel();
	    this.log("Starting SFTP subsystem", Message.PROGRESS);
	    SFTP = new SftpSubsystemClient();
	    channel.startSubsystem(SFTP);
	    this.log("SFTP subsystem ready.", Message.PROGRESS);
	}
	else {
	    this.log("Login failed", Message.PROGRESS);
	    if(host.getLogin().loginFailure())
		this.login();
	    else {
		this.log("Login failed", Message.ERROR);
		//this.close();
	    }
	}
    }
    
    public void check() throws IOException {
/*
	TransportProtocolState state = ssh.getConnectionState();
 if (state.getValue()==TransportProtocolState.DISCONNECTED) {
     System.out.println("Transport protocol has disconnected!");
 }
 */
	if(!SSH.isConnected()) {
	    this.connect();
	}
    }
}