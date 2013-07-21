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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.UploadSymlinkResolver;
import ch.cyberduck.core.transfer.upload.OverwriteFilter;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;

/**
 * @version $Id$
 */
public class CopyTransferFilter extends OverwriteFilter {
    private static final Logger log = Logger.getLogger(CopyTransferFilter.class);

    private Session<?> destination;

    private final Map<Path, Path> files;

    public CopyTransferFilter(final Session<?> destination, final Map<Path, Path> files) {
        super(new UploadSymlinkResolver(destination.getFeature(Symlink.class, null),
                new ArrayList<Path>(files.keySet())));
        this.destination = destination;
        this.files = files;
    }

    @Override
    public TransferStatus prepare(final Session session, final Path source, final TransferStatus parent) throws BackgroundException {
        return super.prepare(destination, source, parent);
    }

    @Override
    public void complete(final Session<?> session, final Path source, final TransferOptions options,
                         final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        super.complete(destination, source, options, status, listener);
    }
}