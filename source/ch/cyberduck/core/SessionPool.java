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
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * A pool to limit the concurrent connections to a remote host.
 *
 * @version $Id$
 */
public class SessionPool extends Hashtable {
	private static Logger log = Logger.getLogger(SessionPool.class);

	private static SessionPool instance;

	private SessionPool() {
		//
	}

	public static SessionPool instance() {
		if(null == instance) {
			instance = new SessionPool();
		}
		return instance;
	}

	/**
	 * @return The number of free slots in the connection pool for @param h
	 */
	public int getSize(Host h) {
		String key = h.getURL();
		if(this.containsKey(key))
			return Preferences.instance().getInteger("connection.pool.max")-((List)this.get(key)).size();
		return Preferences.instance().getInteger("connection.pool.max");
	}

	public synchronized void add(Session session) throws IOException {
		this.add(session, false);
	}

	/**
	 * Adding a session to the connection pool of the remote host. This method
	 * will block until the session has been added to the pool; e.g if the size
	 * of the pool is less than the maximum pool size defined in the preferences.
	 *
	 * @throws IOException If the timeout to wait for a place in the pool has exceeded.
	 */
	public synchronized void add(Session session, boolean force) throws IOException {
		String key = session.getHost().getURL();
		List connections = null;
		if(this.containsKey(key)) {
			connections = (List)this.get(key);
			while(connections.size() >= Preferences.instance().getInteger("connection.pool.max")) {
				try {
					if(force) {
						((Session)connections.get(connections.size()-1)).close();
					}
					else {
						session.log(NSBundle.localizedString("Maximum allowed connections exceeded. Waiting...", ""), Message.PROGRESS);
						this.wait(Preferences.instance().getInteger("connection.pool.timeout")*1000);
					}
				}
				catch(InterruptedException ignored) {
					//
				}
				if(connections.size() >= Preferences.instance().getInteger("connection.pool.max")) {
					// not awakened by another session but because of the timeout
					//I gave up after waiting for "+Preferences.instance().getProperty("connection.pool.timeout")+" seconds
					throw new IOException(NSBundle.localizedString("Too many simultaneous connections. You may want to adjust the number of allowed concurrent connections in the Preferences.", ""));
				}
			}
		}
		else {
			connections = new Vector();
		}
		connections.add(session);
		this.put(key, connections);
	}

	public synchronized void release(Session session) {
		log.debug("release:"+session);
		String key = session.getHost().getURL();
		if(this.containsKey(key)) {
			((List)this.get(key)).remove(session);
			this.notifyAll();
		}
	}
}