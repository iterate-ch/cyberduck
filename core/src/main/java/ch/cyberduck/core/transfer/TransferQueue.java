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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Semaphore;

public final class TransferQueue {
    private static final Logger log = LogManager.getLogger(TransferQueue.class);

    private final ApplicationBadgeLabeler label = ApplicationBadgeLabelerFactory.get();
    private final NotificationService notification = NotificationServiceFactory.get();

    private final ResizeableSemaphore semaphore;

    /**
     * Adjustable number of connections
     */
    private int permits;

    public TransferQueue() {
        this(PreferencesFactory.get().getInteger("queue.connections.limit"));
    }

    public TransferQueue(final int size) {
        this.permits = size == TransferConnectionLimiter.AUTO ?
                PreferencesFactory.get().getInteger("queue.connections.limit.default") : size;
        this.semaphore = new ResizeableSemaphore(permits, true);
    }

    /**
     * Idle this transfer until a free slot is available depending on the maximum number of concurrent transfers allowed
     * in the Preferences.
     *
     * @param t This transfer should respect the settings for maximum number of transfers
     */
    public void add(final Transfer t, final ProgressListener listener) {
        log.debug("Add transfer {} to queue", t);
        if(!semaphore.tryAcquire()) {
            // The maximum number of transfers is already reached. Wait for transfer slot.
            log.info("Queuing transfer {}", t);
            listener.message(LocaleFactory.localizedString("Maximum allowed connections exceeded. Waiting", "Status"));
            notification.notify(t.getName(), t.getUuid(), "Transfer queued", t.getName());
            semaphore.acquireUninterruptibly();
        }
        label.badge(String.valueOf(permits - semaphore.availablePermits() + semaphore.getQueueLength()));
    }

    /**
     * @param t Transfer to drop from queue
     */
    public void remove(final Transfer t) {
        log.debug("Remove {} from queue", t);
        semaphore.release();
        final int size = permits - semaphore.availablePermits() + semaphore.getQueueLength();
        if(0 == size) {
            label.clear();
        }
        else {
            label.badge(String.valueOf(size));
        }
    }

    /**
     * Resize queue with current setting in preferences.
     *
     * @param size New limit
     */
    public void resize(int size) {
        int limit = size == TransferConnectionLimiter.AUTO ?
                PreferencesFactory.get().getInteger("queue.connections.limit.default") : size;
        log.debug("Resize queue to {}", limit);
        if(limit < permits) {
            // Reduce number of permits
            semaphore.reducePermits(permits - limit);
        }
        else {
            // Increase number of permits
            semaphore.release(limit - permits);
        }
        this.permits = limit;
    }

    private static final class ResizeableSemaphore extends Semaphore {
        public ResizeableSemaphore(final int permits, final boolean fair) {
            super(permits, fair);
        }

        @Override
        protected void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }
    }
}
