package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.LoginCallbackFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TransferErrorCallbackControllerFactory;
import ch.cyberduck.core.TransferPromptControllerFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.worker.ConcurrentTransferWorker;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TransferBackgroundAction extends TransferWorkerBackgroundAction<Boolean> {

    private final Transfer transfer;

    private final TransferOptions options;
    /**
     * Keeping track of the current transfer rate
     */
    private final TransferSpeedometer meter;

    /**
     * Timer to update the progress indicator
     */
    private ScheduledFuture progressTimer;

    private ScheduledThreadPool timerPool;

    private final TransferListener listener;

    private final TransferPrompt prompt;

    public TransferBackgroundAction(final Controller controller,
                                    final SessionPool source,
                                    final SessionPool destination,
                                    final TransferListener listener,
                                    final Transfer transfer,
                                    final TransferOptions options) {
        this(controller, source, destination, listener, controller, transfer, options,
                TransferPromptControllerFactory.get(controller, transfer, source, destination),
                TransferErrorCallbackControllerFactory.get(controller),
                new TransferSpeedometer(transfer), new DisabledStreamListener());
    }

    public TransferBackgroundAction(final Controller controller,
                                    final SessionPool source,
                                    final SessionPool destination,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final Transfer transfer,
                                    final TransferOptions options) {
        this(controller, source, destination, listener, progress, transfer, options,
                TransferPromptControllerFactory.get(controller, transfer, source, destination),
                TransferErrorCallbackControllerFactory.get(controller));
    }

    public TransferBackgroundAction(final Controller controller,
                                    final SessionPool source,
                                    final SessionPool destination,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final Transfer transfer,
                                    final TransferOptions options,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error) {
        this(controller, source, destination, listener, progress, transfer, options, prompt, error,
                new TransferSpeedometer(transfer), new DisabledStreamListener());
    }

    public TransferBackgroundAction(final Controller controller,
                                    final SessionPool source,
                                    final SessionPool destination,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final Transfer transfer,
                                    final TransferOptions options,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error,
                                    final TransferSpeedometer meter,
                                    final StreamListener stream) {
        this(LoginCallbackFactory.get(controller),
                controller, source, destination, listener, progress, transfer, options, prompt, error, meter, stream);
    }

    public TransferBackgroundAction(final ConnectionCallback callback,
                                    final Controller controller,
                                    final SessionPool source,
                                    final SessionPool destination,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final Transfer transfer,
                                    final TransferOptions options,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error,
                                    final TransferSpeedometer meter,
                                    final StreamListener stream) {
        super(controller, source, destination, new ConcurrentTransferWorker(source, destination, transfer, options, meter, prompt, error, callback, progress, stream));
        this.options = options;
        this.meter = meter;
        this.transfer = transfer;
        this.listener = listener;
        this.prompt = prompt;
    }

    @Override
    public void prepare() {
        super.prepare();
        transfer.start();
        listener.transferDidStart(transfer);
        timerPool = new ScheduledThreadPool();
        progressTimer = timerPool.repeat(new Runnable() {
            @Override
            public void run() {
                if(transfer.isReset()) {
                    listener.transferDidProgress(transfer, meter.getStatus());
                }
            }
        }, 100L, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean alert(final BackgroundException e) {
        if(super.alert(e)) {
            // Upon retry do not suggest to overwrite already completed items from the transfer
            options.reloadRequested = false;
            options.resumeRequested = true;
            return true;
        }
        return false;
    }

    @Override
    public void message(final String message) {
        super.message(message);
        prompt.message(message);
    }

    public void finish() {
        super.finish();
        progressTimer.cancel(false);
        transfer.stop();
        listener.transferDidStop(transfer);
        timerPool.shutdown();
    }

    @Override
    public String getActivity() {
        return transfer.getName();
    }

    public TransferSpeedometer getMeter() {
        return meter;
    }

    public Transfer getTransfer() {
        return transfer;
    }
}
