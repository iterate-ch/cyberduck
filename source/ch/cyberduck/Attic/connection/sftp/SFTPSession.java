package ch.cyberduck.connection.sftp;

/*
 *  ch.cyberduck.connection.sftp.SFTPSession.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.*;

/**
* Opens a connection to the remote server via sftp protocol
 * @version $Id$
 */
public class SFTPSession extends Session {

    private SftpSubsystemClient SFTP;
    private SshClient SSH;
    private SessionChannelClient session;

    /**
	* @param client The client to use which does implement the ftp protocol
     * @param action The <code>TransferAction</code> to execute after the connection has been opened
     * @param transfer The <code>Bookmark</code> object
     * @param secure If the connection is secure
     */
    public SFTPSession(Bookmark b, TransferAction action) {
        super(b, action, true);
	SSH = new SshClient();
    }

    public void run() {
	try {
	    bookmark.status.fireActiveEvent();
	    this.log("Checking status...", Status.PROGRESS);
	    if(action.toString().equals(TransferAction.QUIT)) {
		//if(SFTP.isAlive()) {
		this.log("Disconnecting from '" + bookmark.getHost() + "'...", Status.PROGRESS);
		this.quit();
		// }
		this.log("Disconnected", Status.PROGRESS);
		return;
	    }
	    /*    if(!SFTP.isAlive()) {
		this.connect();
	    }
	    try {
		SFTP.noop();
	    }
	    catch(IOException e) {
		Cyberduck.DEBUG(e.getMessage());
		this.connect();
	    }
	    catch(SessionException e) {
		Cyberduck.DEBUG(e.getMessage());
		this.connect();
	    }
	    */
	    this.connect();
	    this.check();

	    if(action.toString().equals(TransferAction.LIST)) {
		Path directory;
		if(action.getParam() == null)
		    directory = bookmark.getCurrentPath();
		else
		    directory = (Path)action.getParam();
		this.log("Listing directory '" + directory + "'...", Status.PROGRESS);
		this.list(directory);
	    }
	    else if(action.toString().equals(TransferAction.MKDIR)) {
                SFTP.openDirectory(bookmark.getCurrentPathAsString());
                this.log("Making directory '" + action.getParam() + "'...", Status.PROGRESS);
		SFTP.makeDirectory((String)action.getParam());
		this.list(bookmark.getCurrentPath());
	    }
	    else {
		throw new SFTPException("Unknown action: " + action.toString());
	    }
	    this.log("Command completed.", Status.PROGRESS);
        }
        catch (SessionException e) {
            this.log("SFTP Error: " + e.getReplyCode() + " " + e.getMessage(), Status.ERROR);
            this.log("Incomplete", Status.PROGRESS);
        }
        catch (IOException e) {
            this.log("IO Error: " + e.getMessage(), Status.ERROR);
            this.log("Incomplete", Status.PROGRESS);
        }
        finally {
            this.saveLog();
            this.bookmark.status.ignoreEvents(false);
            this.bookmark.status.fireStopEvent();
        }
    }

    private void connect() throws IOException, SessionException {
	if(!SSH.isConnected()) {
	    this.log("\nConnecting to " + bookmark.getIp()+"\n", Status.TRANSCRIPT);
	    // Make a client connection
	    this.log("Initializing SSH connection", Status.PROGRESS);
	    // Connect to the host
	    SSH.connect(bookmark.getHost());

	    // Create a password authentication instance
	    this.log("Authenticating as '"+bookmark.getUsername()+"'", Status.PROGRESS);
	    PasswordAuthentication pwd = new PasswordAuthentication();
	    pwd.setUsername(bookmark.getUsername());
	    pwd.setPassword(bookmark.getPassword());

	    // Try the authentication
	    int result = SSH.authenticate(pwd);
	    // Evaluate the result
	    if (result == AuthenticationProtocolState.COMPLETE) {
		this.log("Login sucessfull", Status.PROGRESS);
		this.log("Opening SSH session channel", Status.PROGRESS);
		// The connection is authenticated we can now do some real work!
		session = SSH.openSessionChannel();
		this.log("Starting SFTP subsystem", Status.PROGRESS);
		SFTP = new SftpSubsystemClient();
		session.startSubsystem(SFTP);
		this.log("Secure connection established.", Status.PROGRESS);
	    }
	    else {
		bookmark.status.setPanelProperty(Status.LOGINPANEL);
		throw new SFTPException("Authentication failed.", ""+result);
	    }
	}
    }

    private void list(Path directory) throws IOException, SessionException {
	SftpFile workingDirectory;
	// no specific directory has been given. Open the user's default directory on the server.
	if(directory.getPath().equals("/") && bookmark.getCurrentPath().getPath().equals("/")) {
	    workingDirectory = SFTP.openDirectory(".");
	    java.util.List children = new java.util.ArrayList();
	    int read = 1;
	    while(read > 0) {
		read = SFTP.listChildren(workingDirectory, children);
	    }
	    java.util.Iterator i = children.iterator();
	}
	else {
	    workingDirectory = SFTP.openDirectory(directory.getPath());
//	    bookmark.setCurrentPath(new Path(workingDirectory.getAbsolutePath()+"/"));
	}

	bookmark.setCurrentPath(new Path(workingDirectory.getAbsolutePath()));
	List children = new ArrayList();
	int read = 1;
	while(read > 0) {
	    read = SFTP.listChildren(workingDirectory, children);
	}
	bookmark.setListing(SFTPParser.parseList(workingDirectory.getAbsolutePath(), children));
    }

    private void quit() throws IOException {
	session.close();
	SSH.disconnect();
    }
}
