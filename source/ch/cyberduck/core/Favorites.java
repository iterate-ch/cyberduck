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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.net.URL;

/**
* Keeps track of recently connected hosts
 * @version $Id$
 */
public abstract class Favorites {
    private static Logger log = Logger.getLogger(Favorites.class);
    
    private List data = new ArrayList();

    /**
	* Ensure persistency.
     */
    public abstract void save();

    /**
	* Read from file into memory.
     */
    public abstract void load();

    public void add(Object h) {
	this.data.add(h);
	this.save();
    }

    public Object get(String name) {
	Iterator i = data.iterator();
	Object h;
	while(i.hasNext()) {
	    h = i.next();
	    if(h.toString().equals(name.toString()))
		return h;
	}
	throw new IllegalArgumentException("Host "+name+" not found in Favorites.");
    }

    public Iterator getIterator() {
	return data.iterator();
    }
}
