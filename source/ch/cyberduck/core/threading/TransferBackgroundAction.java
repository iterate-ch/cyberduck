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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.HostKeyCallbackFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginCallbackFactory;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.TransferErrorCallbackControllerFactory;
import ch.cyberduck.core.TransferPromptControllerFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.notification.NotificationServiceFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferItemCallback;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.worker.AbstractTransferWorker;
import ch.cyberduck.core.worker.ConcurrentTransferWorker;
import ch.cyberduck.core.worker.SingleTransferWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class TransferBackgroundAction extends ControllerBackgroundAction<Boolean> implements TransferItemCallback {
    private static final Logger log = Logger.getLogger(TransferBackgroundAction.class);

    private Transfer transfer;

    private TransferOptions options;

    /**
     * Keeping track of the current transfer rate
     */
    private TransferSpeedometer meter;

    /**
     * Timer to update the progress indicator
     */
    private ScheduledFuture progressTimer;

    private ScheduledThreadPool timerPool
            = new ScheduledThreadPool();

    private TransferListener listener;

    private AbstractTransferWorker worker;

    private ConnectionService connection;

    private TransferPrompt prompt;

    private NotificationService growl = NotificationServiceFactory.get();

    public TransferBackgroundAction(final Controller controller,
                                    final Session session,
                                    final Cache cache,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript,
                                    final Transfer transfer, final TransferOptions options) {
        this(controller, session, cache, listener, progress, transcript, transfer, options,
                TransferPromptControllerFactory.get(controller, transfer, session),
                TransferErrorCallbackControllerFactory.get(controller));
    }

    public TransferBackgroundAction(final Controller controller,
                                    final Session session,
                                    final Cache cache,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript,
                                    final Transfer transfer, final TransferOptions options,
                                    final TransferPrompt prompt, final TransferErrorCallback error) {
        super(controller, session, cache, progress);
        final LoginCallback login = LoginCallbackFactory.get(controller);
        this.connection = new LoginConnectionService(login,
                HostKeyCallbackFactory.get(controller, transfer.getHost().getProtocol()),
                PasswordStoreFactory.get(), progress, transcript);
        this.meter = new TransferSpeedometer(transfer);
        this.transfer = transfer.withCache(cache);
        this.options = options;
        this.listener = listener;
        this.prompt = prompt;
        if(PreferencesFactory.get().getInteger("queue.session.pool.size") == 1) {
            this.worker = new SingleTransferWorker(session, transfer, options,
                    meter, prompt, error, this, progress, new DisabledStreamListener(), login);
        }
        else {
            this.worker = new ConcurrentTransferWorker(connection, transfer, options,
                    meter, prompt, error, this, login, progress, new DisabledStreamListener());
        }
    }

    @Override
    protected void reset() {
        transfer.start();
    }

    @Override
    protected boolean connect(final Session session) throws BackgroundException {
        final boolean opened = super.connect(session);
        if(transfer instanceof CopyTransfer) {
            final Session target = ((CopyTransfer) transfer).getDestination();
            if(connection.check(target, Cache.<Path>empty())) {
                // New connection opened
                growl.notify("Connection opened", session.getHost().getHostname());
            }
        }
        return opened;
    }

    @Override
    protected void close(final Session session) throws BackgroundException {
        super.close(session);
        if(transfer instanceof CopyTransfer) {
            final Session target = ((CopyTransfer) transfer).getDestination();
            super.close(target);
        }
    }

    @Override
    public void complete(final TransferItem item) {
        // Reset repeat counter. #8223
        repeat = 0;
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        listener.start(transfer);
        timerPool = new ScheduledThreadPool();
        progressTimer = timerPool.repeat(new Runnable() {
            @Override
            public void run() {
                if(transfer.isReset()) {
                    listener.progress(meter.getStatus());
                }
            }
        }, 100L, TimeUnit.MILLISECONDS);
        super.prepare();
    }

    @Override
    public void message(final String message) {
        prompt.message(message);
        super.message(message);
    }

    @Override
    public Boolean run() throws BackgroundException {
        return worker.run();
    }

    public void finish() {
        super.finish();

        progressTimer.cancel(false);
        transfer.stop();
        listener.stop(transfer);
        timerPool.shutdown();
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
    public void cancel() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel background action for transfer %s", transfer));
        }
        worker.cancel();
        super.cancel();
    }

    @Override
    public String getActivity() {
        return StringUtils.EMPTY;
    }

    public TransferSpeedometer getMeter() {
        return meter;
    }

    public Transfer getTransfer() {
        return transfer;
    }
}
