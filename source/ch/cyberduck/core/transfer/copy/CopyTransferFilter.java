package ch.cyberduck.core.transfer.copy;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Map;

/**
 * @version $Id$
 */
public class CopyTransferFilter implements TransferPathFilter {
    private static final Logger log = Logger.getLogger(CopyTransferFilter.class);

    private Session<?> destination;

    private Find find;

    private final Map<Path, Path> files;

    private UploadFilterOptions options;

    public CopyTransferFilter(final Session destination, final Map<Path, Path> files) {
        this(destination, files, new UploadFilterOptions());
    }

    public CopyTransferFilter(final Session<?> destination, final Map<Path, Path> files, final UploadFilterOptions options) {
        this.destination = destination;
        this.files = files;
        this.options = options;
        this.find = destination.getFeature(Find.class);
    }

    @Override
    public boolean accept(final Path source, final TransferStatus parent) throws BackgroundException {
        return true;
    }

    @Override
    public TransferStatus prepare(final Path source, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = new TransferStatus();
        if(source.attributes().isFile()) {
            status.setLength(source.attributes().getSize());
        }
        if(parent.isExists()) {
            // Do not attempt to create a directory that already exists
            final Path destination = files.get(source);
            if(find.find(destination)) {
                status.setExists(true);
            }
        }
        return status;
    }

    @Override
    public void apply(final Path file, final TransferStatus status) throws BackgroundException {
        //
    }

    @Override
    public void complete(final Path source, final TransferOptions options,
                         final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", source.getAbsolute(), status));
        }
        if(status.isComplete()) {
            if(this.options.permissions) {
                final UnixPermission unix = destination.getFeature(UnixPermission.class);
                if(unix != null) {
                    Permission permission = source.attributes().getPermission();
                    if(!Permission.EMPTY.equals(permission)) {
                        this.permission(source, unix, permission);
                    }
                }
            }
            if(this.options.timestamp) {
                final Timestamp timestamp = destination.getFeature(Timestamp.class);
                if(timestamp != null) {
                    listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing timestamp of {0} to {1}", "Status"),
                            source.getName(), UserDateFormatterFactory.get().getShortFormat(source.attributes().getModificationDate())));
                    this.timestamp(source, timestamp);
                }
            }
        }
    }

    private void timestamp(final Path source, final Timestamp timestamp) {
        try {
            timestamp.setTimestamp(files.get(source), source.attributes().getCreationDate(),
                    source.attributes().getModificationDate(),
                    source.attributes().getAccessedDate());
        }
        catch(BackgroundException e) {
            // Ignore
            log.warn(e.getMessage());
        }
    }

    private void permission(final Path source, final UnixPermission unix, final Permission permission) {
        try {
            unix.setUnixPermission(files.get(source), permission);
        }
        catch(BackgroundException e) {
            // Ignore
            log.warn(e.getMessage());
        }
    }
}