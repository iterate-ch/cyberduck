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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.SleepPreventer;
import ch.cyberduck.core.SleepPreventerFactory;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.growl.Growl;
import ch.cyberduck.ui.growl.GrowlFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class TransferBackgroundAction extends ControllerBackgroundAction {
    private static final Logger log = Logger.getLogger(TransferBackgroundAction.class);

    private Transfer transfer;

    /**
     * Overwrite prompt
     */
    private TransferPrompt prompt;

    /**
     * Error prompt
     */
    private TransferErrorCallback error;

    protected TransferOptions options;

    private SleepPreventer sleep = SleepPreventerFactory.get();

    private Growl growl = GrowlFactory.get();

    /**
     * Keeping track of the current transfer rate
     */
    private TransferSpeedometer meter;

    /**
     * Timer to update the progress indicator
     */
    private ScheduledFuture progressTimer;

    private ScheduledThreadPool timerPool;

    private TransferListener transferListener;

    public TransferBackgroundAction(final Controller controller,
                                    final AlertCallback alert,
                                    final TransferListener transferListener,
                                    final ProgressListener progressListener,
                                    final TranscriptListener transcriptListener,
                                    final Transfer transfer,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error,
                                    final TransferOptions options) {
        super(transfer.getSessions(), Cache.empty(), controller, alert, progressListener, transcriptListener);
        this.prompt = prompt;
        this.transfer = transfer;
        this.error = error;
        this.options = options;
        this.transferListener = transferListener;
        this.meter = new TransferSpeedometer(transfer);
        this.timerPool = new ScheduledThreadPool();
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        transferListener.start(transfer);
        super.prepare();
    }

    @Override
    public Boolean run() throws BackgroundException {
        final String lock = sleep.lock();
        try {
            timerPool = new ScheduledThreadPool();
            meter.reset();
            progressTimer = timerPool.repeat(new Runnable() {
                @Override
                public void run() {
                    transferListener.progress(meter.getStatus());
                }
            }, 100L, TimeUnit.MILLISECONDS);
            transfer.start(prompt, options, error);
        }
        finally {
            progressTimer.cancel(false);
            sleep.release(lock);
        }
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        transferListener.stop(transfer);
        timerPool.shutdown();
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
        if(transfer.isReset()) {
            growl.notify(transfer.isComplete() ?
                    String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) : "Transfer incomplete", transfer.getName());
        }
        super.cleanup();
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
