package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.serializer.TransferReaderFactory;
import ch.cyberduck.core.serializer.TransferWriterFactory;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class TransferCollection extends Collection<Transfer> {
    private static Logger log = Logger.getLogger(TransferCollection.class);

    private static TransferCollection instance;

    private Local file;

    private TransferCollection(Local file) {
        this.file = file;
    }

    private static final Object lock = new Object();

    public static TransferCollection defaultCollection() {
        synchronized(lock) {
            if(null == instance) {
                instance = new TransferCollection(
                        LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "Queue.plist")
                );
            }
            return instance;
        }
    }

    @Override
    public void collectionItemAdded(Transfer item) {
        if(locked) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        super.collectionItemAdded(item);
    }

    @Override
    public boolean add(Transfer o) {
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
    @Override
    public void add(int row, Transfer o) {
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
    @Override
    public Transfer remove(int row) {
        return super.remove(row);
    }

    public void save() {
        this.save(file);
    }

    private void save(Local f) {
        log.debug("save");
        if(Preferences.instance().getBoolean("queue.save")) {
            f.getParent().mkdir(true);
            TransferWriterFactory.instance().write(this, f);
        }
    }

    private boolean locked = true;

    public boolean isLocked() {
        return locked;
    }

    @Override
    public void load() {
        this.load(file);
        locked = false;
        this.collectionLoaded();
    }

    private void load(Local f) {
        log.debug("load");
        if(f.exists()) {
            if(log.isInfoEnabled()) {
                log.info("Found Queue file: " + f.toString());
            }
            this.addAll(TransferReaderFactory.instance().readCollection(f));
        }
    }

    /**
     * @return
     */
    public int numberOfRunningTransfers() {
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
    public int numberOfQueuedTransfers() {
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

    /**
     * @return Transfer progress of all transfers in this collection
     */
    public double getDataTransferred() {
        double size = 0;
        for(Transfer t : this) {
            if(t.isRunning() || t.isQueued()) {
                size += t.getTransferred();
            }
        }
        return size;
    }

    /**
     * @return Transfer size of all transfers in this collection
     */
    public double getDataSize() {
        double size = 0;
        for(Transfer t : this) {
            if(t.isRunning() || t.isQueued()) {
                size += t.getSize();
            }
        }
        return size;
    }
}