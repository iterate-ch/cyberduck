package ch.cyberduck.core.ftp;

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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Opens a connection to the remote server via ftp protocol
 * @version $Id$
 */
public class FTPSession extends Session {
    private static Logger log = Logger.getLogger(Session.class);

    protected FTPClient FTP;

    /**
     * @param client The client to use which does implement the ftp protocol
     * @param action The <code>TransferAction</code> to execute after the connection has been opened
     * @param transfer The <code>Bookmark</code> object
     * @param secure If the connection is secure
     */
    public FTPSession(Host h) {//, TransferAction action) {
        super(h);
	this.FTP = new FTPClient();
//@todo proxy        System.getProperties().put("proxySet", Preferences.instance().getProperty("connection.proxy"));
//@todo proxy        System.getProperties().put("proxyHost", Preferences.instance().getProperty("connection.proxy.host"));
//@todo proxy        System.getProperties().put("proxyPort", Preferences.instance().getProperty("connection.proxy.port"));
    }

    public synchronized void close() {
	this.callObservers(new Message(Message.CLOSE, "Closing session."));
	try {
	    if(FTP != null) {
		this.log("Disconnecting...", Message.PROGRESS);
		FTP.quit();
	    }
	    this.log("Disconnected", Message.PROGRESS);
	}
	catch(FTPException e) {
	    this.log("FTP Error: "+e.getMessage(), Message.ERROR);
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
	this.log("Opening FTP connection to " + host.getIp()+"...", Message.PROGRESS);
	if(Preferences.instance().getProperty("ftp.connectmode").equals("active")) {
	    FTP.setConnectMode(FTPConnectMode.ACTIVE);
	}
	else {
	    FTP.setConnectMode(FTPConnectMode.PASV);
	}
//@todo proxy		    if(Preferences.instance().getProperty("connection.proxy").equals("true")) {
//			FTP.initSOCKS(Preferences.instance().getProperty("connection.proxy.port"), Preferences.instance().getProperty("connection.proxy.host"));
//		    }
//		    if(Preferences.instance().getProperty("connection.proxy.authenticate").equals("true")) {
//			FTP.initSOCKSAuthentication(Preferences.instance().getProperty("connection.proxy.username"), Preferences.instance().getProperty("connection.proxy.password"));
//		    }
	FTP.connect(host.getName(), host.getPort());
	this.log("FTP connection opened", Message.PROGRESS);
	this.login();
	FTP.system();
	this.setConnected(true);
    }


    public synchronized void mount() {
	new Thread() {
	    public void run() {
		try {
		    connect();
		    FTPPath home = (FTPPath)FTPSession.this.workdir();
		    home.list();
		}
		catch(FTPException e) {
		    FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
		    FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
	    }
	}.start();
    }

    private synchronized void login() throws IOException {
	log.debug("login");
	try {
	    this.log("Authenticating as " + host.login.getUsername() + "...", Message.PROGRESS);
	    FTP.login(host.login.getUsername(), host.login.getPassword());
	    this.log("Login successfull.", Message.PROGRESS);
	}
	catch(FTPException e) {
	    this.log("Login failed", Message.PROGRESS);
            if(host.getLogin().loginFailure("Authentication for user "+ host.login.getUsername() + " failed. The server response is: "+e.getMessage())) {
                // let's try again with the new values
		this.login();
            }
	    else {
		throw new FTPException("Login as user "+host.login.getUsername()+" failed.");
	    }
	}
    }

    public Path workdir() {
	try {
	    this.check();
	    return new FTPPath(this, FTP.pwd());
	}
	catch(FTPException e) {
	    this.log("FTP Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
	return  null;
    }


    public void check() throws IOException {
	log.debug("check");
	if(!FTP.isAlive()) {
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
	return new FTPSession(this.host);
    }
}