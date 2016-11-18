package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TransferCollection;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferQueue;
import ch.cyberduck.core.transfer.TransferQueueFactory;

import org.apache.log4j.Logger;

public class TransferCollectionBackgroundAction extends TransferBackgroundAction {
    private static final Logger log = Logger.getLogger(TransferCollectionBackgroundAction.class);

    private final TransferQueue queue = TransferQueueFactory.get();

    private final Transfer transfer;

    private final ProgressListener progressListener;

    public TransferCollectionBackgroundAction(final Controller controller,
                                              final SessionPool session,
                                              final TransferListener transferListener,
                                              final ProgressListener progressListener,
                                              final Transfer transfer,
                                              final TransferOptions options) {
        super(controller, session, transferListener, progressListener, transfer, options);
        this.transfer = transfer;
        this.progressListener = progressListener;
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        // Update status to running
        super.prepare();
        // Wait for slot in queue
        queue.add(transfer, progressListener);
    }

    @Override
    public void cancel() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel background action for transfer %s", transfer));
        }
        queue.remove(transfer);
        super.cancel();
    }

    @Override
    public void finish() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Finish background action for transfer %s", transfer));
        }
        queue.remove(transfer);
        super.finish();
    }

    @Override
    public void cleanup() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cleanup background action for transfer %s", transfer));
        }
        super.cleanup();
        final TransferCollection collection = TransferCollection.defaultCollection();
        if(PreferencesFactory.get().getBoolean("queue.removeItemWhenComplete")
                && transfer.isReset() && transfer.isComplete()) {
            collection.remove(transfer);
        }
        else {
            collection.collectionItemChanged(transfer);
        }
    }

    @Override
    public Object lock() {
        // No synchronization with other tasks
        return null;
    }
}