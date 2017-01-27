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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.local.ApplicationBadgeLabeler;
import ch.cyberduck.core.local.ApplicationBadgeLabelerFactory;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.notification.NotificationServiceFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class TransferQueue {
    private static final Logger log = Logger.getLogger(TransferQueue.class);

    private final ApplicationBadgeLabeler label
            = ApplicationBadgeLabelerFactory.get();

    private BlockingQueue<Transfer> running;

    private final NotificationService notification
            = NotificationServiceFactory.get();

    private final List<Transfer> temporary
            = new ArrayList<Transfer>();

    private final Map<Transfer, Thread> threads
            = new HashMap<Transfer, Thread>();

    public TransferQueue() {
        this(PreferencesFactory.get().getInteger("queue.maxtransfers"));
    }

    public TransferQueue(final int size) {
        this.running = new ArrayBlockingQueue<Transfer>(size, true);
    }

    /**
     * Idle this transfer until a free slot is available depending on
     * the maximum number of concurrent transfers allowed in the Preferences.
     *
     * @param t This transfer should respect the settings for maximum number of transfers
     */
    public void add(final Transfer t, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Add transfer %s to queue", t));
        }
        if(0 == running.remainingCapacity()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Queuing transfer %s", t));
            }
            listener.message(LocaleFactory.localizedString("Maximum allowed connections exceeded. Waiting", "Status"));
            notification.notify("Transfer queued", t.getName());
        }
        // The maximum number of transfers is already reached. Wait for transfer slot.
        try {
            threads.put(t, Thread.currentThread());
            running.put(t);
        }
        catch(InterruptedException e) {
            log.error(String.format("Error waiting for slot in queue. %s", e.getMessage()));
        }
        finally {
            threads.remove(t);
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Released from queue %s", t));
        }
        label.badge(String.valueOf(running.size()));
    }

    /**
     * @param t Transfer to drop from queue
     */
    public void remove(final Transfer t) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Remove %s from queue", t));
        }
        if(running.remove(t)) {
            if(0 == running.size()) {
                label.badge(StringUtils.EMPTY);
            }
            else {
                label.badge(String.valueOf(running.size()));
            }
        }
        else {
            final Thread removed = threads.remove(t);
            if(removed != null) {
                log.warn(String.format("Interrupt thread %s for transfer %s", removed, t));
                removed.interrupt();
            }
            temporary.remove(t);
        }
        // Transfer has finished.
        this.poll();
    }

    /**
     * Resize queue with current setting in preferences.
     */
    public void resize(int newsize) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Resize queue to %d", newsize));
        }
        running.drainTo(temporary);
        running.clear();
        running = new ArrayBlockingQueue<Transfer>(newsize);
        this.poll();
    }

    /**
     * Poll temporary queue
     */
    private void poll() {
        if(log.isDebugEnabled()) {
            log.debug("Polling overflow queue");
        }
        temporary.removeIf(transfer -> running.offer(transfer));
    }
}