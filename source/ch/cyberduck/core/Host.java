package ch.cyberduck.core;

/*
 *  ch.cyberduck.core.Host.java
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

import java.io.IOException;

//import ch.cyberduck.core.http.HTTPSession;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ftp.FTPSession;

import ch.cyberduck.core.Path;

import com.sshtools.j2ssh.transport.HostKeyVerification;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

public class Host extends Observable {

    public Status status;
    
    private String protocol;
    private String name;
    private Path path;
    private Login login;
    private int port;

    private HostKeyVerification hostKeyVerification;

    private transient Session session;

    private static Logger log = Logger.getLogger(Host.class);

    public Host(String protocol, String name, int port, Path path, Login login) {
	this.status = new Status();
	this.protocol = protocol;
	this.name = name;
	this.port = port;
	this.path = path;
	this.login = login;
	
    }

    public Host(String protocol, String name, int port, Login login) {
	this(protocol, name, port, null, login);
    }

    public void callObservers(Object arg) {
	this.setChanged();
	this.notifyObservers(arg);
    }

//    public Session getSession(TransferAction action) throws IOException {
    public Session getSession() throws IOException {
        log.debug("getSession");
//        if(this.getProtocol().equalsIgnoreCase(Session.HTTPS)) {
//            return new HTTPSession(this, action);
//        }
//        if(this.getProtocol().equalsIgnoreCase(Session.HTTP)) {
//            return new HTTPSession(this, action);
//        }

	if(this.getProtocol().equalsIgnoreCase(Session.FTP)) {
            return new FTPSession(this);//, action);
        }
        if(this.getProtocol().equalsIgnoreCase(Session.SFTP)) {
            return new SFTPSession(this);//, action);
        }
        throw new IOException("Unknown protocol '" + protocol + " '.");
    }

    public Login getLogin() {
	return this.login;
    }
    
    public String getName() {
	return this.name;
    }

    public Path getPath() {
	return this.path;
    }
    
    public int getPort() {
	return this.port;
    }

    public void setUsername(String u) {
	this.login.setUsername(u);
    }

    public String getUsername() {
	return this.login.getUsername();
    }

    public void setPassword(String p) {
	this.login.setPassword(p);
    }

    public String getPassword() {
	return this.login.getPassword();
    }

    //ssh specific
    public void setHostKeyVerification(HostKeyVerification h) {
	this.hostKeyVerification = h;
    }

    public HostKeyVerification getHostKeyVerification() {
	return this.hostKeyVerification;
    }

    
    public String getProtocol() {
	return this.protocol;
    }

    /**
	* @return The IP address of the remote host if available
     */
    public String getIp() {
        //if we call getByName(null) InetAddress would return localhost
        if(this.name == null)
            return "Unknown host";
        try {
            return java.net.InetAddress.getByName(name).toString();
        }
        catch(java.net.UnknownHostException e) {
            return "Unknown host";
        }
    }
}
