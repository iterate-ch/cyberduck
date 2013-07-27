package ch.cyberduck.ui.threading;

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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.TransferCollection;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.transfer.Queue;
import ch.cyberduck.core.transfer.QueueFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.ui.Controller;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class TransferCollectionBackgroundAction extends TransferBackgroundAction {
    private static final Logger log = Logger.getLogger(TransferCollectionBackgroundAction.class);

    private Queue queue = QueueFactory.get();

    public TransferCollectionBackgroundAction(final Controller controller, final AlertCallback alert,
                                              final TransferListener transferListener,
                                              final ProgressListener progressListener,
                                              final TranscriptListener transcriptListener,
                                              final Transfer transfer, final TransferPrompt prompt, final TransferOptions options) {
        super(controller, alert, transferListener, progressListener, transcriptListener, transfer, prompt, options);
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        queue.add(transfer, progressListener);
        super.prepare();
    }

    @Override
    public void finish() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Finish background action for transfer %s", transfer));
        }
        super.finish();
        for(Session s : transfer.getSessions()) {
            try {
                s.close();
            }
            catch(BackgroundException e) {
                log.warn(String.format("Error closing connection %s", e.getMessage()));
            }
            // We have our own session independent of any browser.
            s.cache().clear();
        }
        queue.remove(transfer);
    }

    @Override
    public void cleanup() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cleanup background action for transfer %s", transfer));
        }
        super.cleanup();
        final TransferCollection collection = TransferCollection.defaultCollection();
        if(transfer.isReset() && transfer.isComplete()) {
            if(Preferences.instance().getBoolean("queue.removeItemWhenComplete")) {
                collection.remove(transfer);
            }
        }
        collection.save();
    }

    @Override
    public Object lock() {
        // No synchronization with other tasks
        return null;
    }
}
