package ch.cyberduck.core;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public abstract class Session extends Observable {
    private static Logger log = Logger.getLogger(Session.class);
	
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String FTP = "ftp";
    public static final String SFTP = "sftp";
	
    /**
		* Default port for http
     */
    public static final int HTTP_PORT = 80;
    /**
		* Default port for https
     */
    public static final int HTTPS_PORT = 443;
    /**
		* Default port for ftp
     */
    public static final int FTP_PORT = 21;
    /**
		* Default port for ssh
     */
    public static final int SSH_PORT = 22;
	
    /**
		* Encapsulating all the information of the remote host
     */
    public Host host;
	
    private List history = null;
	
    private boolean connected;
	
    public Session(Host h) {//, boolean secure) {
		log.debug("Session("+h+")");
		this.host = h;
		this.history = new ArrayList();
        this.log("-------" + new Date().toString(), Message.TRANSCRIPT);
        this.log("-------" + host.getIp(), Message.TRANSCRIPT);
    }
	
    public void callObservers(Object arg) {
		log.debug("callObservers:"+arg);
		log.debug(this.countObservers()+" observer(s) known.");
		this.setChanged();
		this.notifyObservers(arg);
    }
    
    /**
		* Connect to the remote Host
     * The protocol specific implementation has to  be coded in the subclasses.
     * @see Host
     */
    public abstract void connect() throws IOException;
	
    /**
		* Connect to the remote host and mount the home directory
     */
    public abstract void mount() ;
    /**
		* Close the connecion to the remote host.
	 * The protocol specific implementation has to  be coded in the subclasses.
     * @see Host
     */
    public abstract void close();
	
	public void recycle() throws IOException {
		log.info("Recycling session");
		this.close();
		this.connect();
	}
	
	public Host getHost() {
		return this.host;
	}
    
    /**
		* @return The current working directory (pwd)
     */
    public abstract Path workdir();
    /**
		* Assert that the connection to the remote host is still alive. Open connection if needed.
     * @throws IOException The connection to the remote host failed.
     * @see Host
     */
    public abstract void check() throws IOException;
	
    public abstract Session copy();
    
    /**
		* @return boolean True if the session has not yet been closed. 
     */
    public boolean isConnected() {
//		log.debug(this.toString());
//		log.debug("isConnected:"+connected);
		return this.connected;
    }
	
    public void setConnected(boolean connected) {
		log.debug(this.toString());
		log.debug("setConnected:"+connected);
		this.connected = connected;
    }
	
    public void addPathToHistory(Path p) {
		this.history.add(p);
    }
	
    public Path getPreviousPath() {
		log.info("Content of path history:"+history.toString());
		int size = history.size();
		if((size != -1) && (size > 1)) {
			Path p = (Path)history.get(size-2);
			//delete the fetched path - otherwise we produce a loop
			history.remove(size-1);
			history.remove(size-2);
			return p;
		}
		else if(1 == size) {
			return (Path)history.get(size-1);
		}
		return workdir();
    }
    
    public void log(String message, String title) {
        if(title.equals(Message.TRANSCRIPT)) {
            Transcript.instance().transcript(message);
        }
		this.callObservers(new Message(title, message));
    }
}