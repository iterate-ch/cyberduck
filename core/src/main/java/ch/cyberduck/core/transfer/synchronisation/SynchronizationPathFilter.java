package ch.cyberduck.core.transfer.synchronisation;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.synchronization.ComparePathFilter;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

public class SynchronizationPathFilter implements TransferPathFilter {
    private static final Logger log = Logger.getLogger(SynchronizationPathFilter.class);

    private ComparePathFilter comparison;

    /**
     * Download delegate filter
     */
    private TransferPathFilter downloadFilter;

    /**
     * Upload delegate filter
     */
    private TransferPathFilter uploadFilter;

    /**
     * Direction
     */
    private TransferAction action = TransferAction.mirror;

    public SynchronizationPathFilter(final ComparePathFilter comparison,
                                     final TransferPathFilter downloadFilter,
                                     final TransferPathFilter uploadFilter,
                                     final TransferAction action) {
        this.comparison = comparison;
        this.downloadFilter = downloadFilter;
        this.uploadFilter = uploadFilter;
        this.action = action;
    }

    @Override
    public TransferPathFilter withCache(final PathCache cache) {
        downloadFilter.withCache(cache);
        uploadFilter.withCache(cache);
        return this;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent)
            throws BackgroundException {
        final Comparison compare = comparison.compare(file, local);
        if(compare.equals(Comparison.remote)) {
            return downloadFilter.prepare(file, local, parent);
        }
        if(compare.equals(Comparison.local)) {
            return uploadFilter.prepare(file, local, parent);
        }
        // Equal comparison. Read attributes from server
        return uploadFilter.prepare(file, local, parent).exists(true);
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent)
            throws BackgroundException {
        final Comparison compare = comparison.compare(file, local);
        if(compare.equals(Comparison.equal)) {
            return file.isDirectory();
        }
        if(compare.equals(Comparison.remote)) {
            if(action.equals(TransferAction.upload)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Skip file %s with comparison result %s because action is %s",
                            file, compare, action));
                }
                return false;
            }
            // Include for mirror and download. Ask the download delegate for inclusion
            return downloadFilter.accept(file, local, parent);
        }
        if(compare.equals(Comparison.local)) {
            if(action.equals(TransferAction.download)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Skip file %s with comparison result %s because action is %s",
                            file, compare, action));
                }
                return false;
            }
            // Include for mirror and download. Ask the upload delegate for inclusion
            return uploadFilter.accept(file, local, parent);
        }
        log.warn(String.format("Invalid comparison %s", compare));
        // Not equal
        return false;
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        final Comparison compare = comparison.compare(file, local);
        if(compare.equals(Comparison.remote)) {
            downloadFilter.apply(file, local, status, listener);
        }
        else if(compare.equals(Comparison.local)) {
            uploadFilter.apply(file, local, status, listener);
        }
        // Ignore equal compare result
    }

    @Override
    public void complete(final Path file, final Local local, final TransferOptions options, final TransferStatus status,
                         final ProgressListener listener) throws BackgroundException {
        final Comparison compare = comparison.compare(file, local);
        if(compare.equals(Comparison.remote)) {
            downloadFilter.complete(file, local, options, status, listener);
        }
        else if(compare.equals(Comparison.local)) {
            uploadFilter.complete(file, local, options, status, listener);
        }
    }
}
