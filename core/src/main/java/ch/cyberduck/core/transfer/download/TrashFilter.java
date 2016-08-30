package ch.cyberduck.core.transfer.download;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.local.features.Trash;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

public class TrashFilter extends AbstractDownloadFilter {
    private static final Logger log = Logger.getLogger(SkipFilter.class);

    private final Trash feature
            = LocalTrashFactory.get();

    public TrashFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session) {
        super(symlinkResolver, session, new DownloadFilterOptions());
    }

    public TrashFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session,
                       final DownloadFilterOptions options) {
        super(symlinkResolver, session, options);
    }

    @Override
    public void apply(Path file, final Local local, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        if(status.isExists()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Trash file %s", local));
            }
            try {
                feature.trash(local);
            }
            catch(AccessDeniedException e) {
                // Ignore. See #8670
                log.warn(e.getMessage());
            }
        }
        super.apply(file, local, status, listener);
    }
}