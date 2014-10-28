package ch.cyberduck.core.transfer;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.local.ApplicationBadgeLabeler;
import ch.cyberduck.core.local.ApplicationBadgeLabelerFactory;
import ch.cyberduck.ui.growl.NotificationService;
import ch.cyberduck.ui.growl.NotificationServiceFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @version $Id$
 */
public final class Queue {
    private static final Logger log = Logger.getLogger(Queue.class);

    private ApplicationBadgeLabeler label = ApplicationBadgeLabelerFactory.get();

    /**
     * One transfer at least is always allowed to run. Queued accesses for threads blocked
     * on insertion or removal, are processed in FIFO order
     */
    private ArrayBlockingQueue<Transfer> overflow
            = new ArrayBlockingQueue<Transfer>(1, true);

    private NotificationService growl = NotificationServiceFactory.get();

    /**
     * All running transfers.
     */
    private List<Transfer> running
            = Collections.synchronizedList(new ArrayList<Transfer>());

    private int size;

    public Queue() {
        this(Preferences.instance().getInteger("queue.maxtransfers"));
    }

    public Queue(final int size) {
        this.size = size;
    }

    /**
     * Idle this transfer until a free slot is avilable depending on
     * the maximum number of concurrent transfers allowed in the Preferences.
     *
     * @param t This transfer should respect the settings for maximum number of transfers
     */
    public void add(final Transfer t, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Add transfer %s", t));
        }
        if(running.size() >= size) {
            listener.message(LocaleFactory.localizedString("Maximum allowed connections exceeded. Waiting", "Status"));
            if(log.isInfoEnabled()) {
                log.info(String.format("Queuing transfer %s", t));
            }
            growl.notify("Transfer queued", t.getHost().getHostname());
            while(running.size() >= size) {
                // The maximum number of transfers is already reached. Wait for transfer slot.
                try {
                    overflow.put(t);
                }
                catch(InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Released from queue %s", t));
            }
        }
        running.add(t);
        label.badge(String.valueOf(running.size()));
    }

    /**
     * @param t Transfer to drop from queue
     */
    public void remove(final Transfer t) {
        if(running.remove(t)) {
            if(0 == running.size()) {
                label.badge(StringUtils.EMPTY);
            }
            else {
                label.badge(String.valueOf(running.size()));
            }
            // Transfer has finished.
            this.poll();
        }
        else {
            // Transfer was still in the queue and has not started yet.
            overflow.remove(t);
        }
    }

    /**
     * Resize queue with current setting in preferences.
     */
    public void resize(int newsize) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Resize queue to %d", newsize));
        }
        size = newsize;
        int counter = running.size();
        while(counter < size) {
            if(overflow.isEmpty()) {
                log.debug("No more waiting transfers in queue");
                break;
            }
            this.poll();
            counter++;
        }
    }

    private void poll() {
        log.debug("poll");
        // Clear space for other transfer from the head of the queue
        overflow.poll();
    }
}