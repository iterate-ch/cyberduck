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

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class Queue {
    protected static Logger log = Logger.getLogger(Transfer.class);

    private static Queue instance;

    private static final Object lock = new Object();

    public static Queue instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new Queue();
            }
            return instance;
        }
    }

    /**
     * One transfer at least is always allowed to run.
     */
    private ArrayBlockingQueue<Transfer> overflow
            = new ArrayBlockingQueue<Transfer>(1);


    /**
     * All running transfers.
     */
    private List<Transfer> running
            = Collections.synchronizedList(new ArrayList<Transfer>());

    /**
     * Idle this transfer until a free slot is avilable depending on
     * the maximum number of concurrent transfers allowed in the Preferences.
     *
     * @param t This transfer should respect the settings for maximum number of transfers
     */
    public void add(final Transfer t) {
        log.debug("add:" + t);
        if(running.size() >= Preferences.instance().getInteger("queue.maxtransfers") - overflow.remainingCapacity()) {
            t.fireTransferQueued();
            log.info("Queuing:" + t);
            // The maximum number of transfers is already reached
            try {
                boolean offer = false;
                while(!offer && !t.isCanceled()) {
                    // Wait for transfer slot.
                    offer = overflow.offer(t, 1, TimeUnit.SECONDS);
                }
            }
            catch(InterruptedException e) {
                log.error(e.getMessage());
            }
            log.info("released from queue:" + t);
            t.fireTransferResumed();
        }
        running.add(t);
    }

    /**
     * @param t
     */
    public void remove(final Transfer t) {
        if(running.remove(t)) {
            // Transfer has finished.
            this.poll();
        }
        else {
            // Transfer was still in the queue and has not started yet.
            overflow.remove(t);
        }
    }

    /**
     *
     */
    public void resize() {
        log.debug("resize");
        int size = running.size();
        while(size < Preferences.instance().getInteger("queue.maxtransfers")) {
            if(overflow.isEmpty()) {
                log.debug("No more waiting transfers in queue");
                break;
            }
            this.poll();
            size++;
        }
    }

    private void poll() {
        log.debug("poll");
        // Clear space for other transfer from the head of the queue
        overflow.poll();
    }
}