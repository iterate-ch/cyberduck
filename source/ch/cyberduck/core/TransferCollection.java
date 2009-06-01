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


/**
 * @version $Id$
 */
public class TransferCollection extends Collection<Transfer> {
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

    public synchronized boolean add(Transfer o) {
        boolean r = super.add(o);
        this.save();
        return r;
    }

    /**
     * Saves the collection after adding the new item
     *
     * @param row
     * @param o
     * @see #save()
     */
    public synchronized void add(int row, Transfer o) {
        super.add(row, o);
        this.save();
    }

    /**
     * Does not save the collection after modifiying
     *
     * @param row
     * @return the element that was removed from the list.
     * @see #save()
     */
    public synchronized Transfer remove(int row) {
        return super.remove(row);
    }

    public void save() {
//        this.save(QUEUE_FILE);
    }

    private synchronized void save(Local f) {
        log.debug("save");
        if(Preferences.instance().getBoolean("queue.save")) {
            NSMutableArray list = NSMutableArray.arrayWithCapacity(this.size());
            for(Transfer transfer : this) {
                list.addObject(transfer.getAsDictionary());
            }
            list.writeToFile(f.getAbsolute());
        }
    }

    public void load() {
        this.load(QUEUE_FILE);
    }

    private synchronized void load(Local f) {
        log.debug("load");
        if(f.exists()) {
            log.info("Found Queue file: " + f.toString());
            NSArray list = NSArray.arrayWithContentsOfFile(f.getAbsolute());
            final NSEnumerator i = list.objectEnumerator();
            NSObject next;
            while(((next = i.nextObject()) != null)) {
                super.add(TransferFactory.create(Rococoa.cast(next, NSDictionary.class)));
            }
        }
    }

    /**
     * @return
     */
    public synchronized int numberOfRunningTransfers() {
        int running = 0;
        // Count the number of running transfers
        for(Transfer t : this) {
            if(null == t) {
                continue;
            }
            if(t.isRunning()) {
                running++;
            }
        }
        log.debug("numberOfRunningTransfers:" + running);
        return running;
    }

    /**
     * @return
     */
    public synchronized int numberOfQueuedTransfers() {
        int queued = 0;
        // Count the number of queued transfers
        for(Transfer t : this) {
            if(null == t) {
                continue;
            }
            if(t.isQueued()) {
                queued++;
            }
        }
        log.debug("numberOfQueuedTransfers:" + queued);
        return queued;
    }
}