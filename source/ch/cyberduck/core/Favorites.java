package ch.cyberduck.core;

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

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

/**
* Keeps track of recently connected hosts
 * @version $Id$
 */
public abstract class Favorites extends Observable {
    private static Logger log = Logger.getLogger(History.class);

    private static Favorites instance;
    private List data;

    /*
     * Use #instance instead.
     */
    public Favorites() {
	this.data = new ArrayList();
    }

    public static Favorites instance() {
        if(null == instance) {
            String strVendor = System.getProperty("java.vendor");
            if(strVendor.indexOf("Apple") != -1)
                instance = new ch.cyberduck.ui.cocoa.CDFavoritesImpl();
            else
//@todo                instance = new ch.cyberduck.ui.swing.FavoritesImpl();
            instance.setDefaults();
            instance.load();
	}
        return instance;
    }

    public void callObservers(Object arg) {
        log.debug("callObservers:"+arg.toString());
	this.setChanged();
	this.notifyObservers(arg);
    }

    /**
	* Ensure persistency.
     */
    public abstract void save();

    /**
	* Read from file into memory.
     */
    public abstract void load();

    public void add(Host h) {
	data.add(h);
	this.callObservers(h);
    }

    public Host get(String name) {
	Iterator i = data.iterator();
	Host h;
	while(i.hasNext()) {
	    h = (Host)i.next();
	    if(h.getName().equals(name))
		return h;
	}
	throw new IllegalArgumentException("Host "+name+" not found in Favorites.");
    }

    public Iterator iterator() {
	return data.iterator();
    }

    public void setDefaults() {
	//@todo add any default hosts?
    }

    public List getData() {
	return data;
    }
}
