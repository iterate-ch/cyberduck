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
public abstract class History {// extends Observable {
    private static Logger log = Logger.getLogger(History.class);

    private static History instance;
    protected List data = new ArrayList();;

    /*
     * Use #instance instead.
     */
    public History() {
	this.data = new ArrayList();
    }

    public static History instance() {
        if(null == instance) {
            String strVendor = System.getProperty("java.vendor");
            if(strVendor.indexOf("Apple") != -1)
                instance = new ch.cyberduck.ui.cocoa.CDHistoryImpl();
            else
                instance = new ch.cyberduck.ui.swing.HistoryImpl();
            instance.load();
	}
        return instance;
    }

//    public void callObservers(Object arg) {
  //      log.debug("callObservers:"+arg.toString());
//	this.setChanged();
//	this.notifyObservers(arg);
  //  }
    
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
//	this.callObservers(h);
    }
    
    public Host get(String name) {
	Iterator i = data.iterator();
	Host h;
	while(i.hasNext()) {
	    h = (Host)i.next();
	    if(h.getName().equals(name))
		return h;
	}
	throw new IllegalArgumentException("Host "+name+" not found in History.");
    }

    public Iterator iterator() {
	return data.iterator();
    }

    public List getData() {
	return data;
    }
}
