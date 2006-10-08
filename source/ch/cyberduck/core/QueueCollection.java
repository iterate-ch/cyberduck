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

import ch.cyberduck.ui.cocoa.CDProgressController;

import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;


/**
 * @version $Id$
 */
public class QueueCollection extends Collection {
    private static Logger log = Logger.getLogger(QueueCollection.class);

    private static QueueCollection instance;

    private QueueCollection() {
        ;
    }

    public static QueueCollection instance() {
        if(null == instance) {
            instance = new QueueCollection();
            instance.load();
        }
        return instance;
    }

    private static final File QUEUE_FILE
            = new File(Preferences.instance().getProperty("application.support.path"), "Queue.plist");

    static {
        QUEUE_FILE.getParentFile().mkdir();
    }


    public void save() {
        this.save(QUEUE_FILE);
    }

    private void save(java.io.File f) {
        log.debug("save");
        if(Preferences.instance().getBoolean("queue.save")) {
            try {
                NSMutableArray list = new NSMutableArray();
                for(int i = 0; i < this.size(); i++) {
                    list.addObject(((Queue) this.get(i)).getAsDictionary());
                }
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list,
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if(errorString[0] != null) {
                    log.error("Problem writing queue file: " + errorString[0]);
                }

                if(collection.writeToURL(f.toURL(), true)) {
                    log.info("Queue sucessfully saved to :" + f.toString());
                }
                else {
                    log.error("Error saving queue to :" + f.toString());
                }
            }
            catch(java.net.MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void load() {
        this.load(QUEUE_FILE);
    }

    private void load(java.io.File f) {
        log.debug("load");
        if(f.exists()) {
            log.info("Found Queue file: " + f.toString());
            NSData plistData = new NSData(f);
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
                Object element;
                while(i.hasMoreElements()) {
                    element = i.nextElement();
                    if(element instanceof NSDictionary) {
                        super.add(new CDProgressController(
                                QueueFactory.createQueue((NSDictionary) element)));
                    }
                }
            }
        }
    }

    public boolean add(Object queue) {
        super.add(new CDProgressController((Queue) queue));
        this.save();
        return true;
    }

    public void add(int row, Object queue) {
        super.add(row, new CDProgressController((Queue) queue));
        this.save();
    }

    public boolean remove(Object item) {
        for(int i = 0; i < this.size(); i++) {
            CDProgressController c = this.getController(i);
            if(c.getQueue().equals(item)) {
                super.remove(i);
            }
        }
        return true;
    }

    public Object remove(int row) {
        return super.remove(row);
    }

    /**
     * Get the queue at index row
     *
     * @param row
     * @return The @see ch.cyberduck.core.Queue object at this index
     */
    public Object get(int row) {
        if(row < this.size()) {
            return ((CDProgressController) super.get(row)).getQueue();
        }
        return null;
    }

    /**
     * Get the progress controller at index row
     *
     * @param row
     * @return the @see ch.cyberduck.ui.cocoa.CDProgressController at this index
     */
    public CDProgressController getController(int row) {
        if(row < this.size()) {
            return (CDProgressController) super.get(row);
        }
        return null;
    }
}
