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
import ch.cyberduck.core.http.HTTPSession;

//import ch.cyberduck.core.Path;

import com.sshtools.j2ssh.transport.HostKeyVerification;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

public class Host extends Observable {

    public Status status = new Status();
    public Login login;
    private String protocol = Preferences.instance().getProperty("connection.protocol.default");
    private int port = Integer.parseInt(Preferences.instance().getProperty("connection.port.default"));
    private String name;
//    private Path path;
    private String path = Preferences.instance().getProperty("connection.path.default");
    
    private HostKeyVerification hostKeyVerification;

    private transient Session session;

    private static Logger log = Logger.getLogger(Host.class);

    public Host(String protocol, String name, int port, String path, Login login) {
        this.protocol = protocol != null ? protocol : this.protocol;
        this.port = port != -1 ? port : this.port;
        this.name = name;
        this.path = path != null ? path : this.path;
        this.login = login != null ? login : this.login;
	
    }

    public Host(String protocol, String name, int port, Login login) {
	this(protocol, name, port, null, login);
    }

    public void callObservers(Object arg) {
	this.setChanged();
	this.notifyObservers(arg);
    }

//    public Session getSession(TransferAction action) throws IOException {
    public Session getSession() {//throws IOException {
        log.debug("getSession");
//        if(this.getProtocol().equalsIgnoreCase(Session.HTTPS)) {
//            return new HTTPSession(this);
//        }
        if(this.getProtocol().equalsIgnoreCase(Session.HTTP)) {
            return new HTTPSession(this);
        }
	if(this.getProtocol().equalsIgnoreCase(Session.FTP)) {
            return new FTPSession(this);//, action);
        }
        if(this.getProtocol().equalsIgnoreCase(Session.SFTP)) {
            return new SFTPSession(this);//, action);
        }
        return null;
//        throw new IOException("Unknown protocol '" + protocol + " '.");
    }

    public Login getLogin() {
	return this.login;
    }
    
    public String getName() {
	return this.name;
    }

    public String getPath() { //public Path getPath()
	return this.path;
    }
    
    public int getPort() {
	return this.port;
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
