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

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
* Keeps track of user bookmarks
 * The hosts are stored in a hashmap where host.getURL() is the key
 * @see ch.cyberduck.core.Host
 * @version $Id$
 */
public abstract class Bookmarks {
    private static Logger log = Logger.getLogger(Bookmarks.class);
    
	public static final String HOSTNAME = "Hostname";
    public static final String NICKNAME = "Nickname";
    public static final String PORT = "Port";
    public static final String PROTOCOL = "Protocol";
    public static final String USERNAME = "Username";
    public static final String PATH = "Path";
	
    private Map data = new HashMap();
	
    public Bookmarks() {
		this.load();
    }
	
    /**
		* Write data to file.
     */
    public abstract void save();
	
    /**
		* Read from file into memory.
     */
    public abstract void load();
	
	
	public abstract Host importBookmark(java.io.File file);
	
	public abstract void exportBookmark(Host bookmark, java.io.File file);
	
    public void addItem(Host h) {
		log.debug("addItem:"+h);
		this.data.put(h.getURL(), h);
    }
	
    public void removeItem(String key) {
		log.debug("removeItem:"+key);
		this.data.remove(key);
    }
	
    /**
		* @param name the Key the host is stored with (ususally host.toString())
     */
    public Host getItem(String name) {
		Host result =  (Host)this.data.get(name);
		if(null == result)
			throw new IllegalArgumentException("Host "+name+" not found in Bookmarks.");
		return result;
    }
	
//	public int indexOf(Host bookmark) {
//		return data.values().indexOf(bookmark);
//	}
	
    public Collection values() {
		return data.values();
    }
	
    public Iterator getIterator() {
		return data.values().iterator();
    }
}
