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

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import ch.cyberduck.core.Host;

/**
* Keeps track of recently connected hosts
 * @version $Id$
 */
public abstract class Favorites {
    private static Logger log = Logger.getLogger(Favorites.class);
    
    private Map data = new HashMap();

    /**
	* Ensure persistency.
     */
    public abstract void save();

    /**
	* Read from file into memory.
     */
    public abstract void load();

    public void addItem(String url) {
	log.debug("addItem:"+url);
	Host h = new Host(url.substring(0, url.indexOf("://")),
		   url.substring(url.indexOf("@")+1, url.lastIndexOf(":")),
		   Integer.parseInt(url.substring(url.lastIndexOf(":")+1, url.length())),
		   new Login(url.substring(url.indexOf("://")+3, url.lastIndexOf("@"))));
	this.data.put(h.toString(), h);
    }

    /**
	* @param name the Key the host is stored with (ususally host.toString())
     */
    public Object getItem(String name) {
	Object result =  this.data.get(name);
	if(null == result)
	    throw new IllegalArgumentException("Host "+name+" not found in Favorites.");
	return result;
    }

//    public Object get(String name) {
//	Iterator i = data.iterator();
//	Object h;
//	while(i.hasNext()) {
//	    h = i.next();
//	    if(h.toString().equals(name.toString()))
//		return h;
//	}
//	throw new IllegalArgumentException("Host "+name+" not found in Favorites.");
//    }

    public Iterator getIterator() {
	return data.values().iterator();
    }
}
