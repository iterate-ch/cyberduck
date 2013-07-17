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

import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SleepPreventer;
import ch.cyberduck.core.SleepPreventerFactory;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.transfer.Queue;
import ch.cyberduck.core.transfer.QueueFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.growl.Growl;
import ch.cyberduck.ui.growl.GrowlFactory;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class TransferBackgroundAction extends ControllerBackgroundAction {
    private static final Logger log = Logger.getLogger(TransferBackgroundAction.class);

    protected Transfer transfer;
    protected TransferPrompt prompt;
    protected TransferOptions options;

    private SleepPreventer sleep = SleepPreventerFactory.get();

    private Queue queue = QueueFactory.get();

    private Growl growl = GrowlFactory.get();

    /**
     * Keeping track of the current transfer rate
     */
    private TransferSpeedometer meter;

    /**
     * Timer to update the progress indicator
     */
    private ScheduledFuture progressTimer;

    private Controller controller;

    private TransferListener transferListener;

    public TransferBackgroundAction(final Controller controller,
                                    final AlertCallback alert,
                                    final TransferListener transferListener,
                                    final ProgressListener progressListener,
                                    final TranscriptListener transcriptListener,
                                    final Transfer transfer,
                                    final TransferPrompt prompt,
                                    final TransferOptions options) {
        super(controller, alert, progressListener, transcriptListener);
        this.controller = controller;
        this.prompt = prompt;
        this.transfer = transfer;
        this.options = options;
        this.transferListener = transferListener;
        this.meter = new TransferSpeedometer(transfer);
    }

    @Override
    public void run() throws BackgroundException {
        final String lock = sleep.lock();
        try {
            queue.add(transfer, progressListener);
            progressTimer = controller.schedule(new Runnable() {
                @Override
                public void run() {
                    final TransferProgress status = meter.getStatus();
                    transferListener.progress(status);
                }
            }, 100L, TimeUnit.MILLISECONDS);
            transferListener.start(transfer);
            transfer.start(prompt, options);
        }
        finally {
            sleep.release(lock);
            queue.remove(transfer);
            progressTimer.cancel(false);
            transferListener.stop(transfer);
        }
    }

    @Override
    public void cancel() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel background action for transfer %s", transfer));
        }
        transfer.cancel();
    }

    @Override
    public void cleanup() {
        if(transfer.isReset() && transfer.isComplete() && !transfer.isCanceled() && !(transfer.getTransferred() == 0)) {
            growl.notify(transfer.getStatus(), transfer.getName());
        }
        super.cleanup();
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
        if(log.isDebugEnabled()) {
            log.debug(String.format("Pause background action for transfer %s", transfer));
        }
        // Upon retry do not suggest to overwrite already completed items from the transfer
        options.reloadRequested = false;
        options.resumeRequested = true;
        super.pause();
    }

    @Override
    public boolean isCanceled() {
        return transfer.isCanceled();
    }
}
