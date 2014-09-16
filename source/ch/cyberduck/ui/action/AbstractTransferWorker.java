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
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
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

    /**
     * Login prompt
     */
    private LoginCallback login;

    private TransferOptions options;

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

    private FailureDiagnostics<BackgroundException> diagnostics
            = new DefaultFailureDiagnostics();

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferErrorCallback error,
                                  final LoginCallback login) {
        this.transfer = transfer;
        this.prompt = prompt;
        this.error = error;
        this.login = login;
        this.options = options;
    }

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final TransferErrorCallback error,
                                  final LoginCallback login, final Cache<TransferItem> cache) {
        this.transfer = transfer;
        this.options = options;
        this.prompt = prompt;
        this.error = error;
        this.login = login;
        this.cache = cache;
    }

    public AbstractTransferWorker(final Transfer transfer, final TransferOptions options,
                                  final TransferPrompt prompt, final LoginCallback login,
                                  final TransferErrorCallback error,
                                  final Map<Path, TransferStatus> table) {
        this.transfer = transfer;
        this.options = options;
        this.prompt = prompt;
        this.error = error;
        this.login = login;
        this.table = table;
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

    public void complete() throws BackgroundException {
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
                final TransferPathFilter filter = transfer.filter(session, action);
                // Reset the cached size of the transfer and progress value
                transfer.reset();
                // Calculate information about the files in advance to give progress information
                for(TransferItem next : transfer.getRoots()) {
                    this.prepare(next.remote, next.local, new TransferStatus().exists(true), filter);
                }
                this.complete();
                // Transfer all files sequentially
                for(TransferItem next : transfer.getRoots()) {
                    this.transfer(next.remote, next.local, filter);
                }
                this.complete();
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
                        session.message(MessageFormat.format(LocaleFactory.localizedString("Prepare {0}", "Status"), file.getName()));
                        try {
                            // Determine transfer status
                            final TransferStatus status = filter.prepare(file, local, parent);
                            table.put(file, status);
                            // Apply filter
                            filter.apply(file, local, status);
                            // Add transfer length to total bytes
                            transfer.addSize(status.getLength());
                            // Add skipped bytes
                            transfer.addTransferred(status.getCurrent());
                            // Recursive
                            if(file.isDirectory()) {
                                // Call recursively for all children
                                final List<TransferItem> children
                                        = transfer.list(session, file, local, new ActionListProgressListener(AbstractTransferWorker.this));
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
                            // Prompt to continue or abort
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
     * @param file   File
     * @param filter Filter to apply to exclude files from transfer
     */
    public void transfer(final Path file, final Local local, final TransferPathFilter filter) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        // Only transfer if accepted by filter and stored in table with transfer status
        if(table.containsKey(file)) {
            final TransferStatus status = table.get(file);
            this.submit(new TransferCallable() {
                @Override
                public TransferStatus call() throws BackgroundException {
                    // Transfer
                    final Session<?> session = borrow();
                    try {
                        try {
                            if(status.isRename()) {
                                // Save with different name
                                transfer.transfer(session, status.getRename().remote, local, options, status, login);
                            }
                            else {
                                transfer.transfer(session, file, local, options, status, login);
                            }
                        }
                        catch(ConnectionCanceledException e) {
                            throw e;
                        }
                        catch(BackgroundException e) {
                            status.setFailure();
                            if(diagnostics.determine(e) == FailureDiagnostics.Type.network) {
                                throw e;
                            }
                            // Prompt to continue or abort
                            if(error.prompt(e)) {
                                // Continue
                                log.warn(String.format("Ignore transfer failure %s", e));
                            }
                            else {
                                throw new ConnectionCanceledException(e);
                            }
                        }
                        // Recursive
                        if(file.isDirectory()) {
                            for(TransferItem f : cache.get(file.getReference())) {
                                // Recursive
                                transfer(f.remote, f.local, filter);
                            }
                            cache.remove(file.getReference());
                        }
                        if(!status.isFailure()) {
                            // Post process of file.
                            try {
                                filter.complete(file, local, options, status, session);
                            }
                            catch(BackgroundException e) {
                                log.warn(String.format("Ignore failure in completion filter for %s", file));
                            }
                        }
                        return table.remove(file);
                    }
                    finally {
                        release(session);
                    }
                }
            });
        }
        else {
            log.warn(String.format("Skip file %s with unknown transfer status", file));
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
