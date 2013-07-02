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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

/**
 * @version $Id$
 */
public class ResumeFilter extends AbstractUploadFilter {

    public ResumeFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    /**
     * Append to existing file.
     */
    @Override
    public TransferStatus prepare(final Session session, final Path file) throws BackgroundException {
        final TransferStatus status = super.prepare(session, file);
        if(file.getSession().isUploadResumable()) {
            final PathAttributes attributes = file.attributes();
            if(attributes.isFile()) {
                if(file.exists()) {
                    // Do not trust cached value which is from last directory listing
                    // and possibly outdated. Fix #3284.
                    file.readSize();
                    if(attributes.getSize() > 0) {
                        status.setResume(true);
                        status.setCurrent(attributes.getSize());
                    }
                }
            }
        }
        return status;
    }
}
