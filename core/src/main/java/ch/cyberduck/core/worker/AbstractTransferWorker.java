package ch.cyberduck.core.worker;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SleepPreventer;
import ch.cyberduck.core.SleepPreventerFactory;
import ch.cyberduck.core.TransferItemCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.notification.NotificationServiceFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferItemCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractTransferWorker extends Worker<Boolean> implements TransferWorker {
    private static final Logger log = Logger.getLogger(AbstractTransferWorker.class);

    private final SleepPreventer sleep = SleepPreventerFactory.get();

    private final NotificationService growl = NotificationServiceFactory.get();

    private final Transfer transfer;

    /**
     * Overwrite prompt
     */
    private final TransferPrompt prompt;

    /**
     * Error prompt
     */
    private final TransferErrorCallback error;

    private final TransferItemCallback transferItemCallback;

    private final ConnectionCallback connectionCallback;

    private final TransferOptions options;

    private final TransferSpeedometer meter;

    /**
     * Transfer status determined by filters
     */
    private final Map<Path, TransferStatus> table;

    /**
     * Workload
     */
    private final Cache<TransferItem> cache;

    private final ProgressListener progress;

    private final StreamListener stream;

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter,
                                  final TransferErrorCallback error,
                                  final TransferItemCallback callback,
                                  final ProgressListener progress,
                                  final StreamListener stream,
                                  final ConnectionCallback connectionCallback) {
        this(transfer, options, prompt, meter, error, callback, progress, stream, connectionCallback, new TransferItemCache(Integer.MAX_VALUE));
    }

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter,
                                  final TransferErrorCallback error,
                                  final TransferItemCallback callback,
                                  final ProgressListener progress,
                                  final StreamListener stream,
                                  final ConnectionCallback connectionCallback,
                                  final Cache<TransferItem> cache) {
        this(transfer, options, prompt, meter, error, callback, progress, stream, connectionCallback, cache, new HashMap<Path, TransferStatus>());
    }

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter,
                                  final TransferErrorCallback error,
                                  final TransferItemCallback callback,
                                  final ProgressListener progress,
                                  final StreamListener stream,
                                  final ConnectionCallback connectionCallback,
                                  final Cache<TransferItem> cache,
                                  final Map<Path, TransferStatus> table) {
        this.transfer = transfer;
        this.options = options;
        this.prompt = prompt;
        this.meter = meter;
        this.error = error;
        this.transferItemCallback = callback;
        this.progress = progress;
        this.stream = stream;
        this.connectionCallback = connectionCallback;
        this.cache = cache;
        this.table = table;
    }

    protected abstract Session<?> borrow() throws BackgroundException;

    protected abstract void release(Session session) throws BackgroundException;

    @Override
    public void reset() {
        for(TransferStatus status : table.values()) {
            for(TransferStatus segment : status.getSegments()) {
                segment.setCanceled();
            }
        }
    }

    @Override
    public void cancel() {
        this.reset();
        super.cancel();
    }

    public void await() throws BackgroundException {
        // No need to implement for single threaded transfer
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public Boolean run(final Session<?> ignored) throws BackgroundException {
        final String lock = sleep.lock();
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Start transfer with prompt %s and options %s", prompt, options));
            }
            final Session<?> session = this.borrow();
            final TransferAction action;
            try {
                // Determine the filter to match files against
                action = transfer.action(session, options.resumeRequested, options.reloadRequested, prompt,
                        new DisabledListProgressListener() {
                            @Override
                            public void message(final String message) {
                                progress.message(message);
                            }
                        });
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Selected transfer action %s", action));
                }
                if(action.equals(TransferAction.cancel)) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Transfer %s canceled by user", this));
                    }
                    throw new ConnectionCanceledException();
                }
            }
            finally {
                // Return session to pool
                this.release(session);
            }
            // Reset the cached size of the transfer and progress value
            transfer.reset();
            // Calculate information about the files in advance to give progress information
            for(TransferItem next : transfer.getRoots()) {
                this.prepare(next.remote, next.local, new TransferStatus().exists(true), action);
            }
            this.await();
            meter.reset();
            transfer.pre(session, table);
            // Transfer all files sequentially
            for(TransferItem next : transfer.getRoots()) {
                this.transfer(next, action);
            }
            this.await();
        }
        finally {
            if(transfer.isReset()) {
                growl.notify(transfer.isComplete() ?
                        String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) : "Transfer incomplete", transfer.getName());
            }
            sleep.release(lock);
        }
        return true;
    }

    /**
     * To be called before any file is actually transferred
     *
     * @param file   File to transfer
     * @param action Transfer action for existing files
     */
    public void prepare(final Path file, final Local local, final TransferStatus parent, final TransferAction action) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer status of %s for transfer %s", file, this));
        }
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        final TransferItem item = new TransferItem(file, local);
        if(prompt.isSelected(item)) {
            this.submit(new RetryTransferCallable() {
                @Override
                public TransferStatus call() throws BackgroundException {
                    final Session<?> session = borrow();
                    try {
                        // Determine transfer filter implementation from selected overwrite action
                        final TransferPathFilter filter = transfer.filter(session, action, progress);
                        // Only prepare the path it will be actually transferred
                        if(!filter.accept(file, local, parent)) {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Skip file %s by filter %s for transfer %s", file, filter, this));
                            }
                            return null;
                        }
                        else {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Accepted file %s in transfer %s", file, this));
                            }
                            // Transfer
                            progress.message(MessageFormat.format(LocaleFactory.localizedString("Prepare {0}", "Status"), file.getName()));
                            try {
                                // Determine transfer status
                                final TransferStatus status = filter.prepare(file, local, parent);
                                table.put(file, status);
                                // Apply filter
                                filter.apply(item.remote, item.local, status, progress);
                                // Add transfer length to total bytes
                                transfer.addSize(status.getLength() + status.getOffset());
                                // Add skipped bytes
                                transfer.addTransferred(status.getOffset());
                                // Recursive
                                if(file.isDirectory()) {
                                    final List<TransferItem> children;
                                    // Call recursively for all children
                                    children = transfer.list(session, file, local, new ActionListProgressListener(AbstractTransferWorker.this, progress));
                                    // Put into cache for later reference when transferring
                                    cache.put(item, new AttributedList<TransferItem>(children));
                                    // Call recursively
                                    for(TransferItem f : children) {
                                        // Change download path relative to parent local folder
                                        prepare(f.remote, f.local, status, action);
                                    }
                                }
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Determined transfer status %s of %s for transfer %s", status, file, this));
                                }
                                return status;
                            }
                            catch(ConnectionCanceledException e) {
                                throw e;
                            }
                            catch(BackgroundException e) {
                                if(isCanceled()) {
                                    throw new ConnectionCanceledException(e);
                                }
                                if(table.size() == 0) {
                                    throw e;
                                }
                                if(this.retry(e, progress, new TransferStatus())) {
                                    // Retry immediately
                                    return call();
                                }
                                // Prompt to continue or abort for application errors
                                else if(error.prompt(e)) {
                                    // Continue
                                    log.warn(String.format("Ignore transfer failure %s", e));
                                    return null;
                                }
                                else {
                                    throw new ConnectionCanceledException(e);
                                }
                            }
                        }
                    }
                    finally {
                        // Return session to pool
                        release(session);
                    }
                }

                @Override
                public String toString() {
                    final StringBuilder sb = new StringBuilder("TransferCallable{");
                    sb.append("file=").append(file);
                    sb.append(", local=").append(local);
                    sb.append('}');
                    return sb.toString();
                }
            });
        }
        else {
            log.info(String.format("Skip unchecked file %s for transfer %s", file, this));
        }
    }

    /**
     * @param item   File to transfer
     * @param action Transfer action for existing files
     */
    public void transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        // Only transfer if accepted by filter and stored in table with transfer status
        if(table.containsKey(item.remote)) {
            final TransferStatus status = table.get(item.remote);
            // Handle submit of one or more segments
            final List<TransferStatus> segments = status.getSegments();
            for(final Iterator<TransferStatus> iter = segments.iterator(); iter.hasNext(); ) {
                final TransferStatus segment = iter.next();
                this.submit(new RetryTransferCallable() {
                    @Override
                    public TransferStatus call() throws BackgroundException {
                        // Transfer
                        final Session<?> session = borrow();
                        try {
                            try {
                                transfer.transfer(session,
                                        segment.getRename().remote != null ? segment.getRename().remote : item.remote,
                                        segment.getRename().local != null ? segment.getRename().local : item.local,
                                        options, segment, connectionCallback, progress, stream);

                                transferItemCallback.complete(item);

                                // Recursive
                                if(item.remote.isDirectory()) {
                                    for(TransferItem f : cache.get(item)) {
                                        // Recursive
                                        transfer(f, action);
                                    }
                                    cache.remove(item);
                                }
                                // Determine transfer filter implementation from selected overwrite action
                                final TransferPathFilter filter = transfer.filter(session, action, progress);
                                // Post process of file.
                                filter.complete(item.remote, item.local, options, segment, progress);

                                if(!iter.hasNext()) {
                                    // Free memory when no more segments to transfer
                                    table.remove(item.remote);
                                }
                            }
                            catch(ConnectionCanceledException e) {
                                segment.setFailure();
                                throw e;
                            }
                            catch(BackgroundException e) {
                                segment.setFailure();
                                if(AbstractTransferWorker.this.isCanceled()) {
                                    throw new ConnectionCanceledException(e);
                                }
                                if(this.retry(e, progress, segment)) {
                                    // Retry immediately
                                    return call();
                                }
                                if(table.size() == 1) {
                                    throw e;
                                }
                                // Prompt to continue or abort for application errors
                                else if(error.prompt(e)) {
                                    // Continue
                                    log.warn(String.format("Ignore transfer failure %s", e));
                                }
                                else {
                                    throw new ConnectionCanceledException(e);
                                }
                            }
                        }
                        finally {
                            // Return session to pool
                            release(session);
                        }
                        return segment;
                    }

                    @Override
                    public String toString() {
                        final StringBuilder sb = new StringBuilder("TransferCallable{");
                        sb.append("status=").append(segment);
                        sb.append('}');
                        return sb.toString();
                    }
                });
            }
            this.submit(new TransferCallable() {
                @Override
                public TransferStatus call() throws BackgroundException {
                    if(status.isSegmented()) {
                        // Await completion of all segments
                        boolean complete = true;
                        for(TransferStatus segment : segments) {
                            if(!segment.await()) {
                                log.warn(String.format("Failure to complete segment %s.", segment));
                                complete = false;
                            }
                        }
                        if(complete) {
                            final Session<?> session = borrow();
                            try {
                                // Determine transfer filter implementation from selected overwrite action
                                final TransferPathFilter filter = transfer.filter(session, action, progress);
                                // Concatenate segments with completed status set
                                filter.complete(item.remote, item.local, options, status.complete(), progress);
                            }
                            finally {
                                release(session);
                            }
                        }
                        else {
                            log.warn(String.format("Skip concatenating segments for failed transfer %s", status));
                            status.setFailure();
                        }
                    }
                    return status;
                }

                @Override
                public String toString() {
                    final StringBuilder sb = new StringBuilder("TransferCallable{");
                    sb.append("status=").append(status);
                    sb.append('}');
                    return sb.toString();
                }
            });
        }
        else {
            log.warn(String.format("Skip file %s with unknown transfer status", item));
        }
    }

    @Override
    public String getActivity() {
        return BookmarkNameProvider.toString(transfer.getHost());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractTransferWorker{");
        sb.append("transfer=").append(transfer);
        sb.append('}');
        return sb.toString();
    }
}
