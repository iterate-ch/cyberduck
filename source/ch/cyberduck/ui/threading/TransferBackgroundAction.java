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
import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.HostKeyControllerFactory;
import ch.cyberduck.ui.LoginControllerFactory;
import ch.cyberduck.ui.TransferErrorCallbackControllerFactory;
import ch.cyberduck.ui.TransferPromptControllerFactory;
import ch.cyberduck.ui.action.SingleTransferWorker;
import ch.cyberduck.ui.growl.Growl;
import ch.cyberduck.ui.growl.GrowlFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class TransferBackgroundAction extends ControllerBackgroundAction<Boolean> {
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

    private ScheduledThreadPool timerPool;

    private TransferListener listener;

    private SingleTransferWorker worker;

    private ConnectionService connection;

    private Growl growl = GrowlFactory.get();

    public TransferBackgroundAction(final Controller controller,
                                    final Session session,
                                    final TransferListener transferListener,
                                    final ProgressListener progressListener,
                                    final Transfer transfer, final TransferOptions options) {
        this(controller, session, transferListener, progressListener, transfer, options,
                TransferPromptControllerFactory.get(controller, transfer, session),
                TransferErrorCallbackControllerFactory.get(controller));
    }

    public TransferBackgroundAction(final Controller controller,
                                    final Session session,
                                    final TransferListener transferListener,
                                    final ProgressListener progressListener,
                                    final Transfer transfer, final TransferOptions options,
                                    final TransferPrompt prompt, final TransferErrorCallback error) {
        super(controller, session, Cache.empty(), progressListener);
        this.connection = new LoginConnectionService(LoginControllerFactory.get(controller),
                HostKeyControllerFactory.get(controller),
                PasswordStoreFactory.get(), progressListener);
        this.worker = new SingleTransferWorker(session, transfer, options, prompt, error);
        this.transfer = transfer;
        this.options = options;
        this.listener = transferListener;
        this.meter = new TransferSpeedometer(transfer);
        this.timerPool = new ScheduledThreadPool();
    }

    @Override
    protected boolean connect(final Session session) throws BackgroundException {
        if(super.connect(session)) {
            if(transfer instanceof CopyTransfer) {
                final Session target = ((CopyTransfer) transfer).getDestination();
                if(connection.check(target, Cache.empty())) {
                    // New connection opened
                    growl.notify("Connection opened", session.getHost().getHostname());
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
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
    public void prepare() throws ConnectionCanceledException {
        listener.start(transfer);
        timerPool = new ScheduledThreadPool();
        meter.reset();
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
    public Boolean run() throws BackgroundException {
        return worker.run();
    }

    @Override
    public void finish() {
        super.finish();
        progressTimer.cancel(false);
        listener.stop(transfer);
        timerPool.shutdown();
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

    public Transfer getTransfer() {
        return transfer;
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
}
