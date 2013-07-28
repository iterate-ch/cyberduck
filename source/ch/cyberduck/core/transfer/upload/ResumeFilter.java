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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

/**
 * @version $Id$
 */
public class ResumeFilter extends AbstractUploadFilter {

    private Cache cache = new Cache(100);

    public ResumeFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Session session, final Path file, final TransferStatus parent) throws BackgroundException {
        if(file.attributes().isFile()) {
            if(parent.isExists()) {
                if(session.exists(file)) {
                    final long size = this.getSize(session, file);
                    if(file.getLocal().attributes().getSize() >= size) {
                        // No need to resume completed transfers
                        return false;
                    }
                }
            }
        }
        return super.accept(session, file, parent);
    }

    @Override
    public TransferStatus prepare(final Session<?> session, final Path file, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = super.prepare(session, file, parent);
        if(file.attributes().isFile()) {
            if(session.getFeature(Write.class, new DisabledLoginController()).isResumable()) {
                if(parent.isExists()) {
                    if(session.exists(file)) {
                        // Append to existing file
                        status.setResume(true);
                        final long size = this.getSize(session, file);
                        status.setCurrent(size);
                    }
                }
            }
        }
        return status;
    }

    private long getSize(final Session<?> session, final Path file) throws BackgroundException {
        final AttributedList<Path> list;
        if(cache.containsKey(file.getReference())) {
            list = session.list(file.getParent(), new DisabledListProgressListener());
            cache.put(file.getReference(), list);
        }
        else {
            list = cache.get(file.getReference());
        }
        return list.get(file.getReference()).attributes().getSize();
    }
}