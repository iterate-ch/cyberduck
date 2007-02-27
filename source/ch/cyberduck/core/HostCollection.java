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

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.Iterator;

/**
 * @version $Id$
 */
public class HostCollection extends Collection {
    private static Logger log = Logger.getLogger(HostCollection.class);

    private static HostCollection instance;

    private HostCollection() {
        ;
    }

    private static final Object lock = new Object();

    public static HostCollection instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new HostCollection();
                instance.load();
            }
            return instance;
        }
    }

    public Object get(int row) {
        return super.get(row);
    }

    /**
     * @see Host
     * @param o
     * @return
     */
    public boolean add(Object o) {
        this.add(this.size(), o);
        return true;
    }

    /**
     * @see Host
     * @param row
     * @param o
     */
    public void add(int row, Object o) {
        final Host host = (Host)o;
        String proposal = host.getNickname();
        int no = 0;
        do {
            host.setNickname(proposal);
            no++;
            proposal = host.getNickname() + " (" + no + ")";
        }
        while(this.contains(o) && !(this.get(this.indexOf(o)) == o));
        super.add(row, host);
        this.save();
    }

    public Object remove(int row) {
        super.remove(row);
        this.save();
        return null;
    }

    /**
     *
     */
    private static final File BOOKMARKS_FILE
            = new File(Preferences.instance().getProperty("application.support.path"), "Favorites.plist");

    static {
        BOOKMARKS_FILE.getParentFile().mkdir();
    }

    /**
     *
     */
    public void save() {
        this.save(BOOKMARKS_FILE);
    }

    /**
     * Saves this collection of bookmarks in to a file to the users's application support directory
     * in a plist xml format
     */
    private synchronized void save(File f) {
        if(Preferences.instance().getBoolean("favorites.save")) {
            try {
                NSMutableArray list = new NSMutableArray();
                java.util.Iterator i = this.iterator();
                while(i.hasNext()) {
                    Host bookmark = (Host) i.next();
                    list.addObject(bookmark.getAsDictionary());
                }
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list,
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if(errorString[0] != null) {
                    log.error("Problem writing bookmark file: " + errorString[0]);
                }

                if(collection.writeToURL(f.toURL(), true)) {
                    if(log.isInfoEnabled())
                        log.info("Bookmarks sucessfully saved to :" + f.toString());
                }
                else {
                    log.error("Error saving Bookmarks to :" + f.toString());
                }
            }
            catch(java.net.MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     *
     */
    public void load() {
        this.load(BOOKMARKS_FILE);
    }

    /**
     * Deserialize all the bookmarks saved previously in the users's application support directory
     */
    private void load(File f) {
        if(f.exists()) {
            final int pool = NSAutoreleasePool.push();
            try {
                log.info("Found Bookmarks file: " + f.toString());
                NSData plistData = new NSData(f);
                String[] errorString = new String[]{null};
                Object propertyListFromXMLData =
                        NSPropertyListSerialization.propertyListFromData(plistData,
                                NSPropertyListSerialization.PropertyListImmutable,
                                new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                                errorString);
                if(errorString[0] != null) {
                    log.error("Problem reading bookmark file: " + errorString[0]);
                    return;
                }
                if(propertyListFromXMLData instanceof NSArray) {
                    NSArray entries = (NSArray) propertyListFromXMLData;
                    java.util.Enumeration i = entries.objectEnumerator();
                    while(i.hasMoreElements()) {
                        Object element = i.nextElement();
                        if(element instanceof NSDictionary) {
                            super.add(new Host((NSDictionary) element));
                        }
                    }
                }
            }
            finally {
                NSAutoreleasePool.pop(pool);
            }
        }
    }
}
