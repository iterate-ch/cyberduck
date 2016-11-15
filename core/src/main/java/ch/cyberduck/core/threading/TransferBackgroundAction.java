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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferItemCallback;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferTypeFinder;
import ch.cyberduck.core.worker.ConcurrentTransferWorker;
import ch.cyberduck.core.worker.SingleTransferWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TransferBackgroundAction extends WorkerBackgroundAction<Boolean> implements TransferItemCallback {
    private static final Logger log = Logger.getLogger(TransferBackgroundAction.class);

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

    private ScheduledThreadPool timerPool
            = new ScheduledThreadPool();

    private final TransferListener listener;

    private final TransferPrompt prompt;

    public TransferBackgroundAction(final Controller controller,
                                    final Session<?> session,
                                    final PathCache cache,
                                    final TransferListener listener,
                                    final Transfer transfer,
                                    final TransferOptions options) {
        this(controller, session, cache, listener, controller, controller, transfer, options,
                TransferPromptControllerFactory.get(controller, transfer, session),
                TransferErrorCallbackControllerFactory.get(controller),
                new TransferSpeedometer(transfer), new DisabledStreamListener());
    }

    public TransferBackgroundAction(final Controller controller,
                                    final Session<?> session,
                                    final PathCache cache,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript,
                                    final Transfer transfer,
                                    final TransferOptions options) {
        this(controller, session, cache, listener, progress, transcript, transfer, options,
                TransferPromptControllerFactory.get(controller, transfer, session),
                TransferErrorCallbackControllerFactory.get(controller),
                new TransferSpeedometer(transfer), new DisabledStreamListener());
    }

    public TransferBackgroundAction(final Controller controller,
                                    final Session<?> session,
                                    final PathCache cache,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript,
                                    final Transfer transfer,
                                    final TransferOptions options,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error) {
        this(controller, session, cache, listener, progress, transcript, transfer, options, prompt, error,
                new TransferSpeedometer(transfer), new DisabledStreamListener());
    }

    public TransferBackgroundAction(final Controller controller,
                                    final Session<?> session,
                                    final PathCache cache,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript,
                                    final Transfer transfer,
                                    final TransferOptions options,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error,
                                    final TransferSpeedometer meter,
                                    final StreamListener stream) {
        this(new KeychainLoginService(LoginCallbackFactory.get(controller), PasswordStoreFactory.get()),
                LoginCallbackFactory.get(controller),
                HostKeyCallbackFactory.get(controller, session.getHost().getProtocol()),
                controller, session, cache, listener, progress, transcript, transfer, options, prompt, error, meter, stream,
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(session.getHost()), controller), new KeychainX509KeyManager(session.getHost(), controller));
    }

    public TransferBackgroundAction(final LoginService login,
                                    final ConnectionCallback callback,
                                    final HostKeyCallback key,
                                    final Controller controller,
                                    final Session<?> session,
                                    final PathCache cache,
                                    final TransferListener listener,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript,
                                    final Transfer transfer,
                                    final TransferOptions options,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error,
                                    final TransferSpeedometer meter,
                                    final StreamListener stream,
                                    final X509TrustManager x509Trust,
                                    final X509KeyManager x509Key) {
        super(new LoginConnectionService(login, key, progress, transcript), controller, session, cache, null);
        // Initialize worker
        switch(new TransferTypeFinder().type(session, transfer)) {
            case concurrent:
                final int connections = PreferencesFactory.get().getInteger("queue.maxtransfers");
                this.worker = new ConcurrentTransferWorker(new LoginConnectionService(login, key, progress, transcript), transfer, options,
                        meter, prompt, error, this, callback, progress, stream, x509Trust, x509Key, cache,
                        connections);
                break;
            default:
                this.worker = new SingleTransferWorker(session, transfer, options,
                        meter, prompt, error, this, progress, stream, callback);
        }
        this.meter = meter;
        this.transfer = transfer.withCache(cache);
        this.options = options;
        this.listener = listener;
        this.prompt = prompt;
    }

    @Override
    protected boolean connect(final Session session) throws BackgroundException {
        switch(transfer.getType()) {
            case copy:
                final Session target = ((CopyTransfer) transfer).getDestination();
                connection.check(target, PathCache.empty());
        }
        switch(new TransferTypeFinder().type(session, transfer)) {
            case concurrent:
                // Skip opening connection when managed in pool
                return false;
            default:
                return super.connect(session);
        }
    }

    @Override
    protected void close(final Session session) throws BackgroundException {
        super.close(session);
        switch(transfer.getType()) {
            case copy:
                final Session target = ((CopyTransfer) transfer).getDestination();
                super.close(target);
        }
    }

    /**
     * @return Return zero. Retry is handled in transfer worker.
     * @param failure Transfer error
     */
    @Override
    protected int retry(final BackgroundException failure) {
        return 0;
    }

    @Override
    public void complete(final TransferItem item) {
        //
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        super.prepare();
        transfer.start();
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
        listener.stop(transfer);
        timerPool.shutdown();
    }

    @Override
    public void pause(final BackgroundException failure) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Pause background action for transfer %s", transfer));
        }
        // Upon retry do not suggest to overwrite already completed items from the transfer
        options.reloadRequested = false;
        options.resumeRequested = true;
        super.pause(failure);
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
