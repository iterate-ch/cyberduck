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

import org.apache.log4j.Logger;

import java.io.IOException;

import java.util.*;

/**
 * A pool to limit the concurrent connections to a remote host.
 *
 * @version $Id$
 */
public class SessionPool extends Hashtable {
    private static Logger log = Logger.getLogger(SessionPool.class);

	private static SessionPool instance;

	private final int MAX_CONNECTIONS = Integer.parseInt(Preferences.instance().getProperty("connection.pool.max"));
	
	private SessionPool() {
		super();
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
			return MAX_CONNECTIONS-((List)this.get(key)).size();
		return MAX_CONNECTIONS;
	}
	
	/**
	  * Adding a session to the connection pool of the remote host. This method
	  * will block until the session has been added to the pool; e.g if the size
	  * of the pool is less than the maximum pool size defined in the preferences.
	  * @throws IOException If the timeout to wait for a place in the pool has exceeded.
	  */
	public synchronized void add(Session session) throws IOException {
		String key = session.getHost().getURL();
		List connections = null;
		if(this.containsKey(key)) {
		   connections = (List)this.get(key);
		   while(connections.size() >= MAX_CONNECTIONS) {
			   try {
				   session.log("Allowed connections exceeded. Waiting...", Message.PROGRESS);
				   this.wait(Integer.parseInt(Preferences.instance().getProperty("connection.pool.timeout")));
			   }
			   catch (InterruptedException ignored) {
				   throw new IOException("Timeout to wait for a connection from the pool. You may want to adjust the number of allowed concurrent connections in the Preferences."); //@todo localize, better text
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
			this.notify();
		}
	}
}