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
//import java.util.Observable;
import java.io.*;

/**
* Keeps track of recently connected hosts
 * @version $Id$
 */
public abstract class History {// extends Observable {
    private static Logger log = Logger.getLogger(History.class);

    private static History instance;
    private List data;

    private static final String HISTORY_FILE = "History.data";

    /*
     * Use #instance instead.
     */
    public History() {
	this.data = new ArrayList();
    }

    public static History instance() {
	log.debug("instance");
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

    public abstract File getPath();

    /**
	* Ensure persistency.
     */
    public void save() {
	/*
        log.debug("save");
        FileOutputStream st1 = null;
        ObjectOutputStream st2 = null;
        try {
            st1 = new FileOutputStream(new File(this.getPath(), HISTORY_FILE));
            st2 = new ObjectOutputStream(st1);
            java.util.Iterator iterator = data.iterator();
            while (iterator.hasNext()) {
                st2.writeObject(iterator.next());
            }
        }
        catch(IOException e) {
            log.error("Problem saving history: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            try {
                if (st1 != null)
                    st1.close();
                if (st2 != null)
                    st2.close();
            }
            catch(IOException e) {
                log.error("Problem closing output stream: " + e.getMessage());
                e.printStackTrace();
            }
        }
	 */
    }

    /**
	* Read from file into memory.
     */
    public void load() {
	/*
        log.debug("load");
        FileInputStream st1 = null;
        ObjectInputStream st2 = null;
        if (this.getPath().exists()) {
            try {
                st1 = new FileInputStream(new File(this.getPath(), HISTORY_FILE));
                st2 = new ObjectInputStream(st1);
                while(true) {
                    try {
                        Host h = (Host)st2.readObject();
                        this.add(h);
                    }
                    catch(ClassNotFoundException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            catch(EOFException e) {
                //actually no exception. Just the end of the file.
            }
            catch(IOException e) {
                log.error("Error reading from file: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                try {
                    if (st1 != null)
                        st1.close();
                    if (st2 != null)
                        st2.close();
                }
                catch(IOException e) {
                    log.error("Error closing output stream: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
	 */
    }

    public void add(Host h) {
	log.debug("add:"+h);
	data.add(h);
//	this.callObservers(h);
    }

    public Object get(int index) {
	return data.get(index);
    }

    public int size() {
	return data.size();
    }
}
