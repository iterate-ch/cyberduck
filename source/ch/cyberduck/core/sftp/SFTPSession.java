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
import com.sshtools.j2ssh.authentication.PasswordAuthentication;
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
	    try {
//		if((file != null) && file.isDirectory()) {
		    log.debug("Listing " + this.getAbsolute());
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
                            SFTPFile f = new SFTPFile(x.getAbsolutePath());
                            if(!Preferences.instance().getProperty("ftp.showHidden").equals("true")) {
                                if(x.getFilename().charAt(0) == '.') {
                                    f.setVisible(false);
                                }
                            }
                            f.setSize(x.getAttributes().getSize().intValue());
			    f.setModified(x.getAttributes().getModifiedTime().longValue());
			    f.setPermission(new Permission(x.getAttributes().getPermissionsString()));
//			    f.setOwner(x.getAttributes.getOwner());
//			    SFTPSession.this.log(f.getName(), Message.PROGRESS);
//			    log.debug("Adding "+f.getAbsolute()+" to file listing.");
			    files.add(f);
			    //		    host.callObservers(files);
			}
		    }
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
			    SFTP.removeFile(file.getAbsolute());
			}
		    }
//		    FTP.cdup();
		    SFTP.removeDirectory(this.getAbsolute());
		}
		if(this.isFile()) {
		    SFTP.removeFile(this.getAbsolute());
		}
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
	    this.getParent().list();
	}

	public void mkdir() {
	    log.debug("mkdir");
	    try {
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

	public boolean isFile() {
	    try {
		SftpFile workingDirectory = SFTP.openDirectory(this.getAbsolute());
		return workingDirectory.isFile();
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	    return false;
	}

	public boolean isDirectory() {
	    try {
		SftpFile workingDirectory = SFTP.openDirectory(this.getAbsolute());
		return workingDirectory.isDirectory();
	    }
	    catch(SshException e) {
		SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	    return false;
	}

        public void download() {

        }

        public void upload() {

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


    public void run() {
	host.status.fireActiveEvent();
	this.log("Opening SSH connection to " + host.getIp()+"...", Message.PROGRESS);
	try {
	    //if(!SSH.isConnected()) {
	    // Make a client connection
//	    this.log("Initializing SSH connection", Message.PROGRESS);
	    // Connect to the host
	    SSH.connect(host.getName(), host.getHostKeyVerification());
	    this.login();
            String path = host.getPath().equals(Preferences.instance().getProperty("connection.path.default")) ? SFTP.getDefaultDirectory() : host.getPath();
	    SFTPFile home = new SFTPFile(path);
	    home.list();
            host.status.fireStopEvent();
	}
	catch(SshException e) {
	    SFTPSession.this.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    SFTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
        finally {
//            this.saveLog();
//            this.bookmark.status.ignoreEvents(false);
  //          this.bookmark.status.fireStopEvent();
        }
    }

    private void login() throws IOException {
	// Create a password authentication instance
	this.log("Authenticating as '"+host.login.getUsername()+"'", Message.PROGRESS);
	PasswordAuthentication auth = new PasswordAuthentication();
	auth.setUsername(host.login.getUsername());
	auth.setPassword(host.login.getPassword());

	// Try the authentication
	int result = SSH.authenticate(auth);
	this.log(SSH.getAuthenticationBanner(), Message.TRANSCRIPT);
	// Evaluate the result
	if (result == AuthenticationProtocolState.COMPLETE) {
	    this.log("Login sucessfull", Message.PROGRESS);
//	    this.log("Opening SSH session channel", Message.PROGRESS);
	    // The connection is authenticated we can now do some real work!
	    channel = SSH.openSessionChannel();
	    this.log("Starting SFTP subsystem", Message.PROGRESS);
	    SFTP = new SftpSubsystemClient();
	    channel.startSubsystem(SFTP);
	    this.log("Secure connection established.", Message.PROGRESS);
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
}