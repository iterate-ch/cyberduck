package ch.cyberduck.core;

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

import com.apple.cocoa.foundation.NSBundle;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

import ch.cyberduck.ui.cocoa.growl.Growl;

/**
 * @version $Id$
 */
public abstract class Session extends Observable {
	private static Logger log = Logger.getLogger(Session.class);

	public static final String SFTP = "sftp";
	public static final String FTP = "ftp";
	public static final String FTP_TLS = "ftps";

    public static final String FTP_STRING = NSBundle.localizedString("FTP (File Transfer Protocol)", "");
    public static final String FTP_TLS_STRING = NSBundle.localizedString("FTP-SSL (FTP over TLS/SSL)", "");
    public static final String SFTP_STRING = NSBundle.localizedString("SFTP (SSH Secure File Transfer)", "");

	private Cache cache = new Cache();

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
	protected Host host;

	private List history = null;

	private boolean connected;

	private boolean authenticated;

	protected void finalize() throws Throwable {
		log.debug("------------- finalize");
		super.finalize();
	}

	public Session copy() {
		return SessionFactory.createSession(this.host);
	}

	protected Session(Host h) {
		log.debug("Session("+h+")");
		this.host = h;
		this.history = new ArrayList();
//		this.transcript = TranscriptFactory.getImpl(host.getHostname());
	}

	public void callObservers(Object arg) {
		this.setChanged();
		this.notifyObservers(arg);
	}

	/**
		* Assert that the connection to the remote host is still alive. Open connection if needed.
	 *
	 * @throws IOException The connection to the remote host failed.
	 * @see Host
	 */
	public abstract void check() throws IOException;
	
	/**
	 * @return true if the control channel is either tunneled using TLS or SSH
	 */
	public abstract boolean isSecure();
	
	/**
	 * Connect to the remote Host
	 * The protocol specific implementation has to  be coded in the subclasses.
	 *
	 * @see Host
	 */
	public abstract void connect(String encoding) throws IOException;

	public synchronized void connect() throws IOException {
		this.connect(this.host.getEncoding());
	}

//	public void open() {
//		try {
//			this.check();
//		}
//		catch(IOException e) {
//			this.log("IO Error: "+e.getMessage(), Message.ERROR);
//			Growl.instance().notify(NSBundle.localizedString("Connection failed", "Growl Notification"),
//									host.getHostname());
//			this.close();
//		}
//	}
		
	/**
	 * Connect to the remote host and mount the home directory
	 */
	public synchronized void mount(String encoding, Filter filter) {
		this.log(Message.PROGRESS, "Mounting "+host.getHostname()+"...");
		try {
			this.check();
			Path home;
			if(host.hasReasonableDefaultPath()) {
				if(host.getDefaultPath().charAt(0) != '/') {
					home = PathFactory.createPath(this, workdir().getAbsolute(), host.getDefaultPath());
					home.attributes.setType(Path.DIRECTORY_TYPE);
				}
				else {
					home = PathFactory.createPath(this, host.getDefaultPath());
					home.attributes.setType(Path.DIRECTORY_TYPE);
				}
				if(null == home.list(encoding, true, filter)) {
					// the default path does not exist
					home = workdir();
					home.list(encoding, true, filter);
				}
			}
			else {
				home = workdir();
				home.list(encoding, true, filter);
			}
			Growl.instance().notify(NSBundle.localizedString("Connection opened", "Growl Notification"),
									host.getHostname());
		}
		catch(IOException e) {
			this.log(Message.ERROR, "IO Error: "+e.getMessage());
			Growl.instance().notify(NSBundle.localizedString("Connection failed", "Growl Notification"),
									host.getHostname());
			this.close();
		}
	}

	/**
	 * Close the connecion to the remote host.
	 * The protocol specific implementation has to  be coded in the subclasses.
	 *
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

	public abstract void noop() throws IOException;

	public abstract void interrupt();
	
	/**
	 * @return boolean True if the session has not yet been closed.
	 */
	public boolean isConnected() {
		return this.connected;
	}

	public boolean isAuthenticated() {
		return this.authenticated;
	}

	private Timer keepAliveTimer = null;

	public synchronized void setConnected() throws IOException {
		log.debug("setConnected");
		SessionPool.instance().add(this, Preferences.instance().getBoolean("connection.pool.force"));
		this.callObservers(new Message(Message.OPEN, "Session opened."));
		this.connected = true;
	}

	public synchronized void setAuthenticated() {
		this.authenticated = true;
		if(Preferences.instance().getBoolean("connection.keepalive")) {
			this.keepAliveTimer = new Timer();
			this.keepAliveTimer.scheduleAtFixedRate(new KeepAliveTask(),
			    Preferences.instance().getInteger("connection.keepalive.interval"),
			    Preferences.instance().getInteger("connection.keepalive.interval"));
		}
	}

	public synchronized void setClosed() {
		log.debug("setClosed");
		this.connected = false;
		this.callObservers(new Message(Message.CLOSE, "Session closed."));
		if(Preferences.instance().getBoolean("connection.keepalive")) {
			this.keepAliveTimer.cancel();
		}
//		this.cache().clear();
		this.release();
	}

	private void release() {
		SessionPool.instance().release(this);
	}

	private class KeepAliveTask extends TimerTask {
		public void run() {
			try {
				Session.this.noop();
			}
			catch(IOException e) {
				log.error(e.getMessage());
				this.cancel();
			}
		}
	}

	public void addPathToHistory(Path p) {
		if(history.size() > 0) {
			if(!p.equals(history.get(history.size()-1))) {
				this.history.add(p);
			}
		}
		else {
			this.history.add(p);
		}
	}

	public Path getPreviousPath() {
		int size = history.size();
		if(size > 1) {
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

	public Cache cache() {
		return this.cache;
	}

	public void log(String title, String message) {
		if(title.equals(Message.TRANSCRIPT)) {
			TranscriptFactory.getImpl(host.getHostname()).log(message);
		}
		else {
			this.callObservers(new Message(title, message));
		}
	}
}