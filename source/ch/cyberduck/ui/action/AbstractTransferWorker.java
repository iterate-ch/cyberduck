package ch.cyberduck.ui.action;

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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SleepPreventer;
import ch.cyberduck.core.SleepPreventerFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;
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
import ch.cyberduck.ui.growl.Growl;
import ch.cyberduck.ui.growl.GrowlFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class AbstractTransferWorker extends Worker<Boolean> implements TransferWorker {
    private static final Logger log = Logger.getLogger(AbstractTransferWorker.class);

    private SleepPreventer sleep = SleepPreventerFactory.get();

    private Growl growl = GrowlFactory.get();

    private Transfer transfer;

    /**
     * Overwrite prompt
     */
    private TransferPrompt prompt;

    /**
     * Error prompt
     */
    private TransferErrorCallback error;

    private TransferItemCallback callback;

    /**
     * Login prompt
     */
    private LoginCallback login;

    private TransferOptions options;

    private TransferSpeedometer meter;

    /**
     * Transfer status determined by filters
     */
    private Map<Path, TransferStatus> table
            = new HashMap<Path, TransferStatus>();

    /**
     * Workload
     */
    private Cache<TransferItem> cache
            = new Cache<TransferItem>(Integer.MAX_VALUE);

    private FailureDiagnostics<Exception> diagnostics
            = new DefaultFailureDiagnostics();

    private ProgressListener listener;

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter, final TransferErrorCallback error,
                                  final TransferItemCallback callback, final ProgressListener listener, final LoginCallback login) {
        this.transfer = transfer;
        this.prompt = prompt;
        this.meter = meter;
        this.error = error;
        this.login = login;
        this.options = options;
        this.listener = listener;
        this.callback = callback;
    }

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter, final TransferErrorCallback error,
                                  final TransferItemCallback callback, final ProgressListener listener, final LoginCallback login, final Cache<TransferItem> cache) {
        this.transfer = transfer;
        this.options = options;
        this.prompt = prompt;
        this.meter = meter;
        this.error = error;
        this.login = login;
        this.cache = cache;
        this.listener = listener;
        this.callback = callback;
    }

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferSpeedometer meter, final LoginCallback login,
                                  final TransferErrorCallback error, final TransferItemCallback callback, final ProgressListener listener,
                                  final Map<Path, TransferStatus> table) {
        this.transfer = transfer;
        this.options = options;
        this.prompt = prompt;
        this.meter = meter;
        this.error = error;
        this.login = login;
        this.table = table;
        this.listener = listener;
        this.callback = callback;
    }

    protected abstract Session<?> borrow() throws BackgroundException;

    protected abstract void release(Session session) throws BackgroundException;

    @Override
    public void cancel() {
        for(TransferStatus status : table.values()) {
            status.setCanceled();
        }
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
    public Boolean run() throws BackgroundException {
        final String lock = sleep.lock();
        try {
            transfer.start();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Start transfer with prompt %s and options %s", prompt, options));
            }
            final Session<?> session = this.borrow();
            try {
                // Determine the filter to match files against
                final TransferAction action = transfer.action(session, options.resumeRequested, options.reloadRequested, prompt);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Selected transfer action %s", action));
                }
                if(action.equals(TransferAction.cancel)) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Transfer %s canceled by user", this));
                    }
                    throw new ConnectionCanceledException();
                }
                // Determine transfer filter implementation from selected overwrite action
                final TransferPathFilter filter = transfer.filter(session, action, listener);
                // Reset the cached size of the transfer and progress value
                transfer.reset();
                // Calculate information about the files in advance to give progress information
                for(TransferItem next : transfer.getRoots()) {
                    this.prepare(next.remote, next.local, new TransferStatus().exists(true), filter);
                }
                this.await();
                meter.reset();
                // Transfer all files sequentially
                for(TransferItem next : transfer.getRoots()) {
                    this.transfer(next, filter);
                }
                this.await();
            }
            finally {
                this.release(session);
            }
        }
        finally {
            transfer.stop();
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
     * @param file   File
     * @param filter Filter to apply to exclude files from transfer
     */
    public void prepare(final Path file, final Local local,
                        final TransferStatus parent, final TransferPathFilter filter) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer status of %s for transfer %s", file, this));
        }
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        if(prompt.isSelected(new TransferItem(file, local))) {
            // Only prepare the path it will be actually transferred
            if(filter.accept(file, local, parent)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Accepted file %s in transfer %s", file, this));
                }
                this.submit(new TransferCallable() {
                    @Override
                    public TransferStatus call() throws BackgroundException {
                        // Transfer
                        final Session<?> session = borrow();
                        listener.message(MessageFormat.format(LocaleFactory.localizedString("Prepare {0}", "Status"), file.getName()));
                        try {
                            // Determine transfer status
                            final TransferStatus status = filter.prepare(file, local, parent);
                            table.put(file, status);
                            // Apply filter
                            filter.apply(file, local, status, listener);
                            // Add transfer length to total bytes
                            transfer.addSize(status.getLength());
                            // Add skipped bytes
                            transfer.addTransferred(status.getCurrent());
                            // Recursive
                            if(file.isDirectory()) {
                                // Call recursively for all children
                                final List<TransferItem> children
                                        = transfer.list(session, file, local, new ActionListProgressListener(AbstractTransferWorker.this, listener));
                                // Put into cache for later reference when transferring
                                cache.put(file.getReference(), new AttributedList<TransferItem>(children));
                                // Call recursively
                                for(TransferItem f : children) {
                                    // Change download path relative to parent local folder
                                    prepare(f.remote, f.local, status, filter);
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
                            if(diagnostics.determine(e) == FailureDiagnostics.Type.network) {
                                throw e;
                            }
                            // Prompt to continue or abort for application errors
                            if(error.prompt(e)) {
                                // Continue
                                log.warn(String.format("Ignore transfer failure %s", e));
                                return null;
                            }
                            else {
                                throw new ConnectionCanceledException(e);
                            }
                        }
                        finally {
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
                if(log.isInfoEnabled()) {
                    log.info(String.format("Skip file %s by filter %s for transfer %s", file, filter, this));
                }
            }
        }
        else {
            log.info(String.format("Skip unchecked file %s for transfer %s", file, this));
        }
    }

    /**
     * @param item   Transfer
     * @param filter Filter to apply to exclude files from transfer
     */
    public void transfer(final TransferItem item, final TransferPathFilter filter) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        // Only transfer if accepted by filter and stored in table with transfer status
        if(table.containsKey(item.remote)) {
            final TransferStatus status = table.get(item.remote);
            this.submit(new TransferCallable() {
                @Override
                public TransferStatus call() throws BackgroundException {
                    // Transfer
                    final Session<?> session = borrow();
                    try {
                        try {
                            transfer.transfer(session,
                                    status.getRename().remote != null ? status.getRename().remote : item.remote,
                                    status.getRename().local != null ? status.getRename().local : item.local,
                                    options, status, login, listener);
                            callback.complete(item);
                        }
                        catch(ConnectionCanceledException e) {
                            throw e;
                        }
                        catch(BackgroundException e) {
                            status.setFailure();
                            if(diagnostics.determine(e) == FailureDiagnostics.Type.network) {
                                throw e;
                            }
                            // Prompt to continue or abort for application errors
                            if(error.prompt(e)) {
                                // Continue
                                log.warn(String.format("Ignore transfer failure %s", e));
                            }
                            else {
                                throw new ConnectionCanceledException(e);
                            }
                        }
                        // Recursive
                        if(item.remote.isDirectory()) {
                            for(TransferItem f : cache.get(item.remote.getReference())) {
                                // Recursive
                                transfer(f, filter);
                            }
                            cache.remove(item.remote.getReference());
                        }
                        if(!status.isFailure()) {
                            // Post process of file.
                            try {
                                filter.complete(item.remote, item.local, options, status, listener);
                            }
                            catch(BackgroundException e) {
                                log.warn(String.format("Ignore failure in completion filter for %s", item));
                            }
                        }
                        return table.remove(item.remote);
                    }
                    finally {
                        release(session);
                    }
                }

                @Override
                public String toString() {
                    final StringBuilder sb = new StringBuilder("TransferCallable{");
                    sb.append("item=").append(item);
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractTransferWorker{");
        sb.append("transfer=").append(transfer);
        sb.append('}');
        return sb.toString();
    }
}
