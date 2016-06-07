package ch.cyberduck.core.transfer.upload;

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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

public class SkipFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(SkipFilter.class);

    public SkipFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new UploadFilterOptions());
    }

    public SkipFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session,
                      final UploadFilterOptions options) {
        super(symlinkResolver, session, options);
    }

    /**
     * Skip files that already exist on the server.
     */
    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        if(parent.isExists()) {
            if(local.isFile() && find.find(file)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Skip file %s", file));
                }
                return false;
            }
        }
        return super.accept(file, local, parent);
    }
}
