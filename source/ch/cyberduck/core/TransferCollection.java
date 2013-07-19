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

import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.impl.TransferReaderFactory;
import ch.cyberduck.core.serializer.impl.TransferWriterFactory;
import ch.cyberduck.core.transfer.Transfer;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public final class TransferCollection extends Collection<Transfer> {
    private static final Logger log = Logger.getLogger(TransferCollection.class);

    private static TransferCollection instance;

    private static final long serialVersionUID = -6879481152545265228L;

    private Local file;

    protected TransferCollection(Local file) {
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
    public boolean add(Transfer o) {
        boolean r = super.add(o);
        this.save();
        return r;
    }

    /**
     * Saves the collection after adding the new item
     *
     * @param row Index of collection
     * @param o   Transfer
     * @see #save()
     */
    @Override
    public void add(int row, Transfer o) {
        super.add(row, o);
        this.save();
    }

    public void save() {
        this.save(file);
    }

    private void save(Local f) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Save collection to %s", f));
        }
        if(Preferences.instance().getBoolean("queue.save")) {
            f.getParent().mkdir();
            TransferWriterFactory.get().write(this, f);
        }
    }

    @Override
    public void load() {
        this.lock();
        try {
            this.load(file);
        }
        finally {
            this.unlock();
        }
        super.load();
    }

    private void load(Local f) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Load collection from %s", f));
        }
        if(f.exists()) {
            this.addAll(TransferReaderFactory.get().readCollection(f));
        }
    }

    /**
     * @return Number of transfers in collection that are running
     * @see ch.cyberduck.core.transfer.Transfer#isRunning()
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
        return running;
    }
}