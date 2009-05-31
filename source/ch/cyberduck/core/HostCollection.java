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

import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

import java.io.IOException;

/**
 * @version $Id$
 */
public class HostCollection extends BookmarkCollection {
    private static Logger log = Logger.getLogger(HostCollection.class);

    /**
     * Default bookmark file
     */
    private static HostCollection DEFAULT_COLLECTION = new HostCollection(
            new Local(Preferences.instance().getProperty("application.support.path"), "Favorites.plist")
    );

    /**
     * @return
     */
    public static HostCollection defaultCollection() {
        return DEFAULT_COLLECTION;
    }

    /**
     * @param file
     */
    public HostCollection(Local file) {
        this.setFile(file);
//        this.load();
    }

    /**
     * The file to persist this collection in
     */
    protected Local file;

    /**
     * Will create the parent directory if missing
     *
     * @param file
     */
    protected void setFile(Local file) {
        this.file = file;
        this.file.getParent().mkdir(true);
    }

    /**
     * @return
     */
    public Local getFile() {
        return this.file;
    }

    public synchronized Host get(int row) {
        return super.get(row);
    }

    /**
     * @param host
     * @return
     * @see Host
     */
    public synchronized boolean add(Host host) {
        this.add(this.size(), host);
        return true;
    }

    /**
     * @param row
     * @param host
     * @see Host
     */
    public synchronized void add(int row, Host host) {
        super.add(row, this.unique(host));
        this.sort();
        this.save();
    }

    /**
     * Ensures the bookmark nickname is unique
     *
     * @param host
     * @return
     */
    protected Host unique(Host host) {
        final String proposal = host.getNickname();
        for(int i = 1; this.contains(host) && !(this.get(this.indexOf(host)) == host); i++) {
            host.setNickname(proposal + " (" + i + ")");
        }
        return host;
    }

    /**
     * @param row
     * @return the element that was removed from the list.
     */
    public synchronized Host remove(int row) {
        Host previous = super.remove(row);
        this.save();
        return previous;
    }

    protected void sort() {
        //
    }

    /**
     * Saves this collection of bookmarks in to a file to the users's application support directory
     * in a plist xml format
     */
    protected void save() {
        if(Preferences.instance().getBoolean("favorites.save")) {
            NSMutableArray list = NSMutableArray.arrayWithCapacity(1);
            for(Host bookmark : this) {
                list.addObject(bookmark.getAsDictionary());
            }
            NSMutableData collection = NSMutableData.dataWithLength(0);
            try {
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list));
            }
            catch(IOException e) {
                log.error("Problem writing bookmark file: " + e.getMessage());

            }
            if(collection.writeToURL(NSURL.fileURLWithPath(file.getAbsolute()))) {
                if(log.isInfoEnabled()) {
                    log.info("Bookmarks sucessfully saved to :" + file.toString());
                }
            }
            else {
                log.error("Error saving Bookmarks to :" + file.toString());
            }
        }
    }

    /**
     * Deserialize all the bookmarks saved previously in the users's application support directory
     */
    protected void load() {
        if(file.exists()) {
            log.info("Found Bookmarks file: " + file.getAbsolute());
            NSData plistData = NSData.dataWithContentsOfURL(NSURL.fileURLWithPath(file.getAbsolute()));
            if(null == plistData) {
                log.error("Invalid bookmark file:" + file);
                return;
            }
            try {
                NSArray propertyListFromXMLData = Rococoa.cast(NSPropertyListSerialization.propertyListFromData(plistData), NSArray.class);
                final NSEnumerator i = propertyListFromXMLData.objectEnumerator();
                NSObject next;
                while(((next = i.nextObject()) != null)) {
                    super.add(new Host(Rococoa.cast(next, NSDictionary.class)));
                }
                this.sort();

            }
            catch(IOException e) {
                log.error("Problem reading bookmark file:" + e.getMessage());
            }
        }
    }

    public void collectionItemChanged(Host item) {
        this.save();
        super.collectionItemChanged(item);
    }
}