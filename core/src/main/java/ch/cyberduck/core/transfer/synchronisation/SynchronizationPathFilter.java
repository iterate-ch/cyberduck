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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.synchronization.ComparePathFilter;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SynchronizationPathFilter implements TransferPathFilter {
    private static final Logger log = LogManager.getLogger(SynchronizationPathFilter.class);

    private final ComparePathFilter comparison;

    /**
     * Download delegate filter
     */
    private final TransferPathFilter downloadFilter;

    /**
     * Upload delegate filter
     */
    private final TransferPathFilter uploadFilter;

    /**
     * Direction
     */
    private final TransferAction action;

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
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener listener)
        throws BackgroundException {
        switch(comparison.compare(file, local, listener)) {
            case remote:
                return downloadFilter.prepare(file, local, parent, listener);
            case local:
                return uploadFilter.prepare(file, local, parent, listener);
        }
        // Equal comparison. Read attributes from server
        return uploadFilter.prepare(file, local, parent, listener).exists(true);
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        switch(comparison.compare(file, local, new DisabledProgressListener())) {
            case equal:
                return file.isDirectory();
            case remote:
                if(action.equals(TransferAction.upload)) {
                    if(log.isInfoEnabled()) {
                        log.info("Skip file {} with comparison result {} because action is {}", file, Comparison.remote, action);
                    }
                    return false;
                }
                // Include for mirror and download. Ask the download delegate for inclusion
                return downloadFilter.accept(file, local, parent);
            case local:
                if(action.equals(TransferAction.download)) {
                    if(log.isInfoEnabled()) {
                        log.info("Skip file {} with comparison result {} because action is {}", file, Comparison.local, action);
                    }
                    return false;
                }
                // Include for mirror and download. Ask the upload delegate for inclusion
                return uploadFilter.accept(file, local, parent);
        }
        // Not equal
        return false;
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        switch(comparison.compare(file, local, listener)) {
            case remote:
                downloadFilter.apply(file, local, status, listener);
                break;
            case local:
                uploadFilter.apply(file, local, status, listener);
                break;
        }
        // Ignore equal compare result
    }

    @Override
    public void complete(final Path file, final Local local, final TransferStatus status,
                         final ProgressListener listener) throws BackgroundException {
        switch(comparison.compare(file, local, listener)) {
            case remote:
                downloadFilter.complete(file, local, status, listener);
                break;
            case local:
                uploadFilter.complete(file, local, status, listener);
        }
    }

    @Override
    public TransferPathFilter withFinder(final Find finder) {
        downloadFilter.withFinder(finder);
        uploadFilter.withFinder(finder);
        return this;
    }

    @Override
    public TransferPathFilter withAttributes(final AttributesFinder attributes) {
        downloadFilter.withAttributes(attributes);
        uploadFilter.withAttributes(attributes);
        return null;
    }
}
