package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.*;

import java.io.File;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.QueueList;

/**
 * @version $Id$
 */
public class CDQueueList extends QueueList { //implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDQueueList.class);

    private static final File QUEUE_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Queue.plist"));

    static {
        QUEUE_FILE.getParentFile().mkdir();
    }

    public void save() {
        this.save(QUEUE_FILE);
    }

    private void save(java.io.File f) {
        log.debug("save");
        if (Preferences.instance().getProperty("queue.save").equals("true")) {
            try {
                NSMutableArray list = new NSMutableArray();
                for (int i = 0; i < this.size(); i++) {
                    list.addObject(this.getItem(i).getAsDictionary());
                }
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list,
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if (errorString[0] != null) {
                    log.error("Problem writing queue file: " + errorString[0]);
                }

                if (collection.writeToURL(f.toURL(), true)) {
                    log.info("Queue sucessfully saved to :" + f.toString());
                }
                else {
                    log.error("Error saving Queue to :" + f.toString());
                }
            }
            catch (java.net.MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void load() {
        this.load(QUEUE_FILE);
    }

    private void load(java.io.File f) {
        log.debug("load");
        if (f.exists()) {
            log.info("Found Queue file: " + f.toString());
            NSData plistData = new NSData(f);
            String[] errorString = new String[]{null};
            Object propertyListFromXMLData =
                    NSPropertyListSerialization.propertyListFromData(plistData,
                            NSPropertyListSerialization.PropertyListImmutable,
                            new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                            errorString);
            if (errorString[0] != null) {
                log.error("Problem reading queue file: " + errorString[0]);
            }
            else {
                log.debug("Successfully read Queue: " + propertyListFromXMLData);
            }
            if (propertyListFromXMLData instanceof NSArray) {
                NSArray entries = (NSArray)propertyListFromXMLData;
                java.util.Enumeration i = entries.objectEnumerator();
                Object element;
                while (i.hasMoreElements()) {
                    element = i.nextElement();
                    if (element instanceof NSDictionary) {
                        this.addItem(new Queue((NSDictionary)element));
                    }
                }
            }
        }
    }
}
