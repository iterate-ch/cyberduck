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
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferCollection;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.ui.Controller;

import java.util.List;

/**
 * @version $Id$
 */
public class TransferRepeatableBackgroundAction extends ControllerRepeatableBackgroundAction {

    private Transfer transfer;
    private TransferPrompt prompt;
    private TransferOptions options;

    public TransferRepeatableBackgroundAction(final Controller controller,
                                              final AlertCallback alert,
                                              final ProgressListener progressListener,
                                              final TranscriptListener transcriptListener,
                                              final Transfer transfer,
                                              final TransferPrompt prompt,
                                              final TransferOptions options) {
        super(controller, alert, progressListener, transcriptListener);
        this.prompt = prompt;
        this.transfer = transfer;
        this.options = options;
    }

    @Override
    public void run() throws BackgroundException {
        transfer.start(prompt, options);
    }

    @Override
    public void finish() throws BackgroundException {
        super.finish();
        for(Session s : transfer.getSessions()) {
            s.close();
            // We have our own session independent of any browser.
            s.cache().clear();
        }
    }

    @Override
    public void cleanup() {
        final TransferCollection collection = TransferCollection.defaultCollection();
        if(transfer.isComplete() && !transfer.isCanceled() && transfer.isReset()) {
            if(Preferences.instance().getBoolean("queue.removeItemWhenComplete")) {
                collection.remove(transfer);
            }
        }
        collection.save();
    }

    @Override
    public List<Session<?>> getSessions() {
        return transfer.getSessions();
    }

    @Override
    public String getActivity() {
        return transfer.getName();
    }

    @Override
    public void pause() {
        transfer.fireTransferQueued();
        // Upon retry do not suggest to overwrite already completed items from the transfer
        options.reloadRequested = false;
        options.resumeRequested = true;
        super.pause();
        transfer.fireTransferResumed();
    }

    @Override
    public boolean isCanceled() {
        return transfer.isCanceled();
    }

    private final Object lock = new Object();

    @Override
    public Object lock() {
        // No synchronization with other tasks
        return lock;
    }
}
