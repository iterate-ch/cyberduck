package ch.cyberduck.ui.cocoa;

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

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.*;

import java.io.File;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Bookmarks;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Preferences;

/**
 * @version $Id$
 */
public class CDBookmarksImpl extends Bookmarks { //implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDBookmarksImpl.class);

    private static CDBookmarksImpl instance;

    private static final File BOOKMARKS_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Favorites.plist"));

    static {
        BOOKMARKS_FILE.getParentFile().mkdir();
    }

    public static CDBookmarksImpl instance() {
        if (null == instance) {
            instance = new CDBookmarksImpl();
        }
        return instance;
    }

    public void save() {
        this.save(BOOKMARKS_FILE);
    }

    /**
     * Saves this collection of bookmarks in to a file to the users's application support directory
     * in a plist xml format
     */
    public void save(java.io.File f) {
        log.debug("save");
        if (Preferences.instance().getProperty("favorites.save").equals("true")) {
            try {
                NSMutableArray list = new NSMutableArray();
                java.util.Iterator i = this.iterator();
                while (i.hasNext()) {
                    Host bookmark = (Host) i.next();
                    list.addObject(bookmark.getAsDictionary());
                }
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list,
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if (errorString[0] != null) {
                    log.error("Problem writing bookmark file: " + errorString[0]);
                }

                if (collection.writeToURL(f.toURL(), true)) {
                    log.info("Bookmarks sucessfully saved to :" + f.toString());
                }
                else {
                    log.error("Error saving Bookmarks to :" + f.toString());
                }
            }
            catch (java.net.MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void load() {
        this.load(BOOKMARKS_FILE);
    }

    /**
     * Deserialize all the bookmarks saved previously in the users's application support directory
     */
    public void load(java.io.File f) {
        log.debug("load");
        if (f.exists()) {
            log.info("Found Bookmarks file: " + f.toString());
            NSData plistData = new NSData(f);
            String[] errorString = new String[]{null};
            Object propertyListFromXMLData =
                    NSPropertyListSerialization.propertyListFromData(plistData,
                            NSPropertyListSerialization.PropertyListImmutable,
                            new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                            errorString);
            if (errorString[0] != null) {
                log.error("Problem reading bookmark file: " + errorString[0]);
            }
            else {
                log.debug("Successfully read Bookmarks: " + propertyListFromXMLData);
            }
            if (propertyListFromXMLData instanceof NSArray) {
                NSArray entries = (NSArray) propertyListFromXMLData;
                java.util.Enumeration i = entries.objectEnumerator();
                Object element;
                while (i.hasMoreElements()) {
                    element = i.nextElement();
                    if (element instanceof NSDictionary) { //new since 2.1
                        this.addItem(new Host((NSDictionary) element));
                    }
                    if (element instanceof String) { //backward compatibilty <= 2.1beta5 (deprecated)
                        try {
                            this.addItem(new Host((String) element));
                        }
                        catch (java.net.MalformedURLException e) {
                            log.error("Bookmark has invalid URL: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public Host importBookmark(java.io.File file) {
        log.info("Importing bookmark from " + file);
        NSData plistData = new NSData(file);
        String[] errorString = new String[]{null};
        Object propertyListFromXMLData =
                NSPropertyListSerialization.propertyListFromData(plistData,
                        NSPropertyListSerialization.PropertyListImmutable,
                        new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                        errorString);
        if (errorString[0] != null) {
            log.error("Problem reading bookmark file: " + errorString[0]);
        }
        else {
            log.debug("Successfully read bookmark file: " + propertyListFromXMLData);
        }
        if (propertyListFromXMLData instanceof NSDictionary) {
            return new Host((NSDictionary) propertyListFromXMLData);
        }
        log.error("Invalid file format:" + file);
        return null;
    }

    public void exportBookmark(Host bookmark, java.io.File file) {
        try {
            log.info("Exporting bookmark " + bookmark + " to " + file);
            NSMutableData collection = new NSMutableData();
            String[] errorString = new String[]{null};
            collection.appendData(NSPropertyListSerialization.dataFromPropertyList(bookmark.getAsDictionary(),
                    NSPropertyListSerialization.PropertyListXMLFormat,
                    errorString));
            if (errorString[0] != null) {
                log.error("Problem writing bookmark file: " + errorString[0]);
            }
            if (collection.writeToURL(file.toURL(), true)) {
                log.info("Bookmarks sucessfully saved in :" + file.toString());
                NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(file.getAbsolutePath());
            }
            else {
                log.error("Error saving Bookmarks in :" + file.toString());
            }
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }
}
