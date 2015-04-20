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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.notification.NotificationServiceFactory;
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
public class TransferBackgroundAction extends WorkerBackgroundAction<Boolean> implements TransferItemCallback {
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

    private TransferPrompt prompt;

    private NotificationService growl = NotificationServiceFactory.get();

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
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(session.getHost())), new KeychainX509KeyManager());
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
        this.worker = new WorkerFinder().find(login, callback, key, session, progress, transfer, options, prompt, this,
                error, meter, stream, transcript, x509Trust, x509Key, cache);
        this.meter = meter;
        this.transfer = transfer.withCache(cache);
        this.options = options;
        this.listener = listener;
        this.prompt = prompt;
    }

    private static final class WorkerFinder {
        private AbstractTransferWorker find(final LoginService login,
                                            final ConnectionCallback callback,
                                            final HostKeyCallback key,
                                            final Session<?> session,
                                            final ProgressListener progress,
                                            final Transfer transfer,
                                            final TransferOptions options,
                                            final TransferPrompt prompt,
                                            final TransferItemCallback item,
                                            final TransferErrorCallback error,
                                            final TransferSpeedometer meter,
                                            final StreamListener stream,
                                            final TranscriptListener transcript,
                                            final X509TrustManager x509Trust,
                                            final X509KeyManager x509Key,
                                            final PathCache cache) {
            switch(session.getTransferType()) {
                case concurrent:
                    switch(transfer.getType()) {
                        case copy:
                        case move:
                            break;
                        case upload:
                            final Upload feature = session.getFeature(Upload.class);
                            if(feature.pooled()) {
                                // Already pooled internally.
                                break;
                            }
                        default:
                            // Setup concurrent worker if not already pooled internally
                            final int connections = PreferencesFactory.get().getInteger("queue.maxtransfers");
                            if(connections > 1) {
                                return new ConcurrentTransferWorker(new LoginConnectionService(login, key, progress, transcript), transfer, options,
                                        meter, prompt, error, item, callback, progress, stream, x509Trust, x509Key, cache,
                                        connections);
                            }
                    }
            }
            return new SingleTransferWorker(session, transfer, options,
                    meter, prompt, error, item, progress, stream, callback);
        }
    }

    @Override
    protected void reset() throws BackgroundException {
        super.reset();
        transfer.start();
    }

    @Override
    protected boolean connect(final Session session) throws BackgroundException {
        final boolean opened = super.connect(session);
        switch(transfer.getType()) {
            case copy:
                final Session target = ((CopyTransfer) transfer).getDestination();
                if(connection.check(target, PathCache.empty())) {
                    // New connection opened
                    growl.notify("Connection opened", session.getHost().getHostname());
                }
        }
        return opened;
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

    @Override
    public void complete(final TransferItem item) {
        // Reset repeat counter. #8223
        repeat = 0;
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        super.prepare();
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
