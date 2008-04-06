package ch.cyberduck.core;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSData;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSMutableData;
import com.apple.cocoa.foundation.NSPropertyListSerialization;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * @version $Id$
 */
public class TransferCollection extends Collection {
    private static Logger log = Logger.getLogger(TransferCollection.class);

    private static TransferCollection instance;

    private TransferCollection() {
        ;
    }

    private static final Object lock = new Object();

    public static TransferCollection instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new TransferCollection();
            }
            return instance;
        }
    }

    private static final Local QUEUE_FILE
            = new Local(Preferences.instance().getProperty("application.support.path"), "Queue.plist");

    static {
        QUEUE_FILE.getParent().mkdir();
    }

    public synchronized boolean add(Object o) {
        boolean r = super.add(o);
        this.save();
        return r;
    }

    /**
     * Saves the collection after adding the new item
     * @param row
     * @param o
     * @see #save()
     */
    public synchronized void add(int row, Object o) {
        super.add(row, o);
        this.save();
    }

    /**
     * Does not save the collection after modifiying
     * @param row
     * @return the element that was removed from the list.
     * @see #save()
     */
    public synchronized Object remove(int row) {
        return super.remove(row);
    }

    public void save() {
        this.save(QUEUE_FILE);
    }

    private synchronized void save(Local f) {
        log.debug("save");
        synchronized(this) {
            if(Preferences.instance().getBoolean("queue.save")) {
                NSMutableArray list = new NSMutableArray();
                for(int i = 0; i < this.size(); i++) {
                    list.addObject(((Transfer) this.get(i)).getAsDictionary());
                }
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list,
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if(errorString[0] != null) {
                    log.error("Problem writing queue file: " + errorString[0]);
                }

                try {
                    if(collection.writeToURL(new URL(f.toURL()), true)) {
                        log.info("Queue sucessfully saved to :" + f.toString());
                    }
                    else {
                        log.error("Error saving queue to :" + f.toString());
                    }
                }
                catch(MalformedURLException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public void load() {
        this.load(QUEUE_FILE);
    }

    private synchronized void load(Local f) {
        log.debug("load");
        synchronized(this) {
            if(f.exists()) {
                log.info("Found Queue file: " + f.toString());
                NSData plistData = null;
                try {
                    plistData = new NSData(new URL(f.toURL()));
                }
                catch(MalformedURLException e) {
                    log.error(e.getMessage());
                }
                String[] errorString = new String[]{null};
                Object propertyListFromXMLData =
                        NSPropertyListSerialization.propertyListFromData(plistData,
                                NSPropertyListSerialization.PropertyListImmutable,
                                new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                                errorString);
                if(errorString[0] != null) {
                    log.error("Problem reading queue file: " + errorString[0]);
                }
                if(propertyListFromXMLData instanceof NSArray) {
                    NSArray entries = (NSArray) propertyListFromXMLData;
                    java.util.Enumeration i = entries.objectEnumerator();
                    while(i.hasMoreElements()) {
                        Object element = i.nextElement();
                        if(element instanceof NSDictionary) {
                            super.add(TransferFactory.create((NSDictionary) element));
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public synchronized int numberOfRunningTransfers() {
        int running = 0;
        // Count the number of running transfers
        for(Iterator iter = this.iterator(); iter.hasNext(); ) {
            Transfer t = (Transfer) iter.next();
            if(null == t) {
                continue;
            }
            if(t.isRunning()) {
                running++;
            }
        }
        log.debug("numberOfRunningTransfers:"+running);
        return running;
    }

    /**
     * 
     * @return
     */
    public synchronized int numberOfQueuedTransfers() {
        int queued = 0;
        // Count the number of queued transfers
        for(Iterator iter = this.iterator(); iter.hasNext(); ) {
            Transfer t = (Transfer) iter.next();
            if(null == t) {
                continue;
            }
            if(t.isQueued()) {
                queued++;
            }
        }
        log.debug("numberOfQueuedTransfers:"+queued);
        return queued;
    }
}