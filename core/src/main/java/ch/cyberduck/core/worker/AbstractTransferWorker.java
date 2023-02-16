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
import ch.cyberduck.core.BytecountStreamListener;
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
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.threading.TransferBackgroundActionState;
import ch.cyberduck.core.transfer.SynchronizingTransferErrorCallback;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public abstract class AbstractTransferWorker extends TransferWorker<Boolean> {
    private static final Logger log = LogManager.getLogger(AbstractTransferWorker.class);

    private final SleepPreventer sleep = SleepPreventerFactory.get();
    private final NotificationService notification;
    private final Transfer transfer;
    /**
     * Overwrite prompt
     */
    private final TransferPrompt prompt;
    /**
     * Error prompt
     */
    private final TransferErrorCallback error;
    private final ConnectionCallback connect;
    private final TransferOptions options;
    private final TransferSpeedometer meter;
    /**
     * Transfer status determined by filters
     */
    private final Map<TransferItem, TransferStatus> table;
    /**
     * Workload
     */
    private final Cache<TransferItem> cache;
    private final ProgressListener progress;
    private final StreamListener stream;

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter,
                                  final TransferErrorCallback error,
                                  final ProgressListener progress,
                                  final StreamListener stream,
                                  final ConnectionCallback connect,
                                  final NotificationService notification) {
        this(transfer, options, prompt, meter, error, progress, stream, connect, notification, new TransferItemCache(Integer.MAX_VALUE));
    }

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter,
                                  final TransferErrorCallback error,
                                  final ProgressListener progress,
                                  final StreamListener stream,
                                  final ConnectionCallback connect,
                                  final NotificationService notification,
                                  final Cache<TransferItem> cache) {
        this(transfer, options, prompt, meter, error, progress, stream, connect, notification, cache, new ConcurrentHashMap<>());
    }

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter,
                                  final TransferErrorCallback error,
                                  final ProgressListener progress,
                                  final StreamListener stream,
                                  final ConnectionCallback connect,
                                  final NotificationService notification,
                                  final Cache<TransferItem> cache,
                                  final Map<TransferItem, TransferStatus> table) {
        this.transfer = transfer;
        this.options = options;
        this.prompt = prompt;
        this.meter = meter;
        this.error = new SynchronizingTransferErrorCallback(error);
        this.progress = progress;
        this.stream = stream;
        this.connect = connect;
        this.notification = notification;
        this.cache = cache;
        this.table = table;
    }

    protected enum Connection {
        source,
        destination
    }

    /**
     * Submit transfer to pool
     *
     * @param callable Repeatable
     * @return Future transfer status
     * @throws BackgroundException On transfer failure when executed instantly
     */
    protected abstract Future<TransferStatus> submit(TransferCallable callable) throws BackgroundException;

    /**
     * Borrow session from pool for transfer
     */
    protected abstract Session<?> borrow(Connection type) throws BackgroundException;

    /**
     * Release session from pool for transfer
     */
    protected abstract void release(Session session, Connection type, BackgroundException failure);

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public void cancel() {
        for(TransferStatus status : table.values()) {
            for(TransferStatus segment : status.getSegments()) {
                segment.setCanceled();
            }
        }
        super.cancel();
    }

    public void await() throws BackgroundException {
        // No need to implement for single threaded transfer
    }

    @Override
    public Boolean run(final Session<?> source) throws BackgroundException {
        final String lock = sleep.lock();
        final Session<?> destination = this.borrow(Connection.destination);
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Start transfer with prompt %s and options %s", prompt, options));
            }
            // Determine the filter to match files against
            final TransferAction action = transfer.action(source, destination, options.resumeRequested, options.reloadRequested, prompt,
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
                throw new TransferCanceledException();
            }
            // Reset the cached size of the transfer and progress value
            transfer.reset();

            // Normalize Paths before preparing
            transfer.normalize();

            // Calculate information about the files in advance to give progress information
            for(TransferItem next : transfer.getRoots()) {
                // Check if parent directory is found in set to determine status
                this.prepare(next.remote, next.local, new TransferStatus()
                        .exists(!transfer.getRoots().stream().anyMatch(f -> next.remote.isChild(f.remote))), action);
            }
            this.await();
            meter.reset();
            transfer.pre(source, destination, table, transfer.filter(source, destination, action, progress), error, progress, connect);
            // Transfer all files sequentially
            for(TransferItem next : transfer.getRoots()) {
                this.transfer(next, action);
            }
            this.await();
            transfer.post(source, destination, table, error, progress, connect);
        }
        finally {
            this.release(source, Connection.source, null);
            this.release(destination, Connection.destination, null);
            if(transfer.isReset()) {
                notification.notify(transfer.getName(), transfer.getUuid(), transfer.isComplete() ?
                        String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) :
                        "Transfer incomplete", transfer.getName());
            }
            this.shutdown();
            sleep.release(lock);
            table.clear();
            cache.clear();
        }
        return true;
    }

    /**
     * To be called before any file is actually transferred
     *
     * @param file   File to transfer
     * @param action Transfer action for existing files
     */
    public Future<TransferStatus> prepare(final Path file, final Local local, final TransferStatus parent, final TransferAction action) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer status of %s for transfer %s", file, this));
        }
        if(this.isCanceled()) {
            throw new TransferCanceledException();
        }
        if(prompt.isSelected(new TransferItem(file, local))) {
            return this.submit(new RetryTransferCallable(transfer.getSource()) {
                @Override
                public TransferStatus call() throws BackgroundException {
                    parent.validate();
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Prepare {0} ({1})", "Status"),
                            file.getName(), action.getTitle()));
                    final Session<?> source = borrow(Connection.source);
                    final Session<?> destination = borrow(Connection.destination);
                    try {
                        // Determine transfer filter implementation from selected overwrite action
                        final TransferPathFilter filter = transfer.filter(source, destination, action, progress);
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
                            // Determine transfer status
                            final TransferStatus status = filter.prepare(file, local, parent, progress);
                            table.put(new TransferItem(file, local), status);
                            final TransferItem item = new TransferItem(
                                    status.getRename().remote != null ? status.getRename().remote : file,
                                    status.getRename().local != null ? status.getRename().local : local
                            );
                            // Apply filter
                            filter.apply(item.remote, item.local, status, progress);
                            // Add transfer length to total bytes
                            transfer.addSize(status.getLength() + status.getOffset());
                            // Add skipped bytes
                            transfer.addTransferred(status.getOffset());
                            // Recursive
                            if(file.isDirectory()) {
                                // Call recursively for all children
                                final List<TransferItem> children = transfer.list(source, file, local, new WorkerListProgressListener(AbstractTransferWorker.this, progress));
                                // Put into cache for later reference when transferring
                                cache.put(item, new AttributedList<>(children));
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
                    }
                    catch(BackgroundException e) {
                        if(this.retry(e, progress, new TransferBackgroundActionState(parent))) {
                            // Retry immediately
                            return call();
                        }
                        // Prompt to continue or abort for application errors
                        else if(error.prompt(new TransferItem(file, local), parent, e, table.size())) {
                            // Continue
                            log.warn(String.format("Ignore transfer failure %s", e));
                            return null;
                        }
                        else {
                            throw new TransferCanceledException(e);
                        }
                    }
                    finally {
                        release(source, Connection.source, null);
                        release(destination, Connection.destination, null);
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
        return null;
    }

    /**
     * @param item   File to transfer
     * @param action Transfer action for existing files
     */
    public Future<TransferStatus> transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
        if(this.isCanceled()) {
            throw new TransferCanceledException();
        }
        // Only transfer if accepted by filter and stored in table with transfer status
        if(table.containsKey(item)) {
            // Overall transfer status
            final TransferStatus status = table.get(item);
            // Handle submit of one or more segments
            final List<TransferStatus> segments = status.getSegments();
            for(final TransferStatus segment : segments) {
                this.submit(new RetryTransferCallable(transfer.getSource()) {
                    @Override
                    public TransferStatus call() throws BackgroundException {
                        status.validate();
                        if(segment.isComplete()) {
                            if(log.isWarnEnabled()) {
                                log.warn(String.format("Skip transferring already completed item %s", item));
                            }
                        }
                        else {
                            // Do transfer with retry
                            this.transferSegment(segment);
                            final Session<?> source = borrow(Connection.source);
                            final Session<?> destination = borrow(Connection.destination);
                            try {
                                // Determine transfer filter implementation from selected overwrite action
                                final TransferPathFilter filter = transfer.filter(source, destination, action, progress);
                                // Post process of file.
                                filter.complete(
                                        segment.getRename().remote != null ? segment.getRename().remote : item.remote,
                                        segment.getRename().local != null ? segment.getRename().local : item.local,
                                        segment, progress);
                            }
                            finally {
                                release(source, Connection.source, null);
                                release(destination, Connection.destination, null);
                            }
                        }
                        // Recursive
                        if(item.remote.isDirectory()) {
                            if(!cache.isCached(item)) {
                                log.warn(String.format("Missing entry for %s in cache", item));
                            }
                            for(TransferItem f : cache.get(item)) {
                                // Recursive
                                transfer(f, action);
                            }
                            cache.remove(item);
                        }
                        return segment;
                    }

                    private void transferSegment(final TransferStatus segment) throws BackgroundException {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Transfer item %s with status %s", item, segment));
                        }
                        final Session<?> s = borrow(Connection.source);
                        final Session<?> d = borrow(Connection.destination);
                        final BytecountStreamListener counter = new BytecountStreamListener(stream);
                        try {
                            transfer.transfer(s, d,
                                    segment.getRename().remote != null ? segment.getRename().remote : item.remote,
                                    segment.getRename().local != null ? segment.getRename().local : item.local,
                                    options, status, segment, connect, progress, counter);
                        }
                        catch(BackgroundException e) {
                            release(s, Connection.source, e);
                            release(d, Connection.destination, e);
                            log.warn(String.format("Failure %s transferring %s", e, item));
                            // Determine if we should retry depending on failure type
                            if(this.retry(e, progress, new TransferBackgroundActionState(status))) {
                                final Session<?> source = borrow(Connection.source);
                                final Session<?> destination = borrow(Connection.destination);
                                try {
                                    final TransferPathFilter resume = transfer.filter(source, destination, TransferAction.resume, progress);
                                    if(resume.accept(item.remote, item.local, new TransferStatus().exists(true))) {
                                        if(log.isDebugEnabled()) {
                                            log.debug(String.format("Retry transfer of %s", item));
                                        }
                                        final TransferStatus retry = resume.prepare(item.remote, item.local, new TransferStatus().exists(true), progress);
                                        // Add bytes retrieved but not yet counted
                                        stream.recv(retry.getOffset() - counter.getRecv());
                                        stream.sent(retry.getOffset() - counter.getSent());
                                        transfer.addTransferred(retry.getOffset() - counter.getSent());
                                        // Retry immediately
                                        log.info(String.format("Retry %s with transfer status %s", item, retry));
                                        this.transferSegment(segment
                                                .withLength(retry.getLength())
                                                .withOffset(retry.getOffset())
                                                .append(retry.isAppend()));
                                        return;
                                    }
                                }
                                finally {
                                    release(source, Connection.source, null);
                                    release(destination, Connection.destination, null);
                                }
                            }
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Cancel retry for %s", item));
                            }
                            segment.setFailure();
                            // Prompt to continue or abort for application errors
                            if(error.prompt(item, segment, e, table.size())) {
                                // Continue
                                log.warn(String.format("Ignore transfer failure %s", e));
                            }
                            else {
                                throw new TransferCanceledException(e);
                            }
                        }
                        finally {
                            release(s, Connection.source, null);
                            release(d, Connection.destination, null);
                        }
                    }

                    @Override
                    public String toString() {
                        final StringBuilder sb = new StringBuilder("RetryTransferCallable{");
                        sb.append("item=").append(item);
                        sb.append(", status=").append(segment);
                        sb.append('}');
                        return sb.toString();
                    }
                });
            }
            return this.submit(new TransferCallable() {
                @Override
                public TransferStatus call() throws BackgroundException {
                    status.validate();
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
                            final Session<?> source = borrow(Connection.source);
                            final Session<?> destination = borrow(Connection.destination);
                            try {
                                // Determine transfer filter implementation from selected overwrite action
                                final TransferPathFilter filter = transfer.filter(source, destination, action, progress);
                                // Concatenate segments with completed status set
                                filter.complete(
                                        status.getRename().remote != null ? status.getRename().remote : item.remote,
                                        status.getRename().local != null ? status.getRename().local : item.local,
                                        status.complete(), progress);
                            }
                            finally {
                                release(source, Connection.source, null);
                                release(destination, Connection.destination, null);
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
                    sb.append("item=").append(item);
                    sb.append(", status=").append(status);
                    sb.append('}');
                    return sb.toString();
                }
            });
        }
        else {
            log.warn(String.format("Skip file %s with unknown transfer status", item));
        }
        return ConcurrentUtils.constantFuture(null);
    }

    protected void shutdown() {
        // No-op
    }

    @Override
    public String getActivity() {
        return BookmarkNameProvider.toString(transfer.getSource());
    }

    public Map<TransferItem, TransferStatus> getStatus() {
        return table;
    }

    public Cache<TransferItem> getCache() {
        return cache;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractTransferWorker{");
        sb.append("transfer=").append(transfer);
        sb.append('}');
        return sb.toString();
    }
}
