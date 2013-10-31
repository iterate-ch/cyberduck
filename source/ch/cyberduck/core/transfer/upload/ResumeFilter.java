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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ResumeFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(ResumeFilter.class);

    private Write write;

    private Find find;

    private Attributes attributes;

    public ResumeFilter(final SymlinkResolver symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new UploadFilterOptions());
    }

    public ResumeFilter(final SymlinkResolver symlinkResolver, final Session<?> session, final UploadFilterOptions options) {
        super(symlinkResolver, session, options);
        this.find = session.getFeature(Find.class);
        this.write = session.getFeature(Write.class);
        this.attributes = session.getFeature(Attributes.class);
    }

    @Override
    public boolean accept(final Path file, final TransferStatus parent) throws BackgroundException {
        if(file.attributes().isFile()) {
            if(parent.isExists()) {
                if(find.find(file)) {
                    final long remote = attributes.find(file).getSize();
                    if(remote >= file.getLocal().attributes().getSize()) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Skip file %s with remote size %d", file, remote));
                        }
                        // No need to resume completed transfers
                        return false;
                    }
                }
            }
        }
        return super.accept(file, parent);
    }

    @Override
    public TransferStatus prepare(final Path file, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = super.prepare(file, parent);
        if(file.attributes().isFile()) {
            if(parent.isExists()) {
                final Write.Append append = write.append(file, attributes);
                if(append.append) {
                    // Append to existing file
                    status.setAppend(true);
                    status.setCurrent(append.size);
                }
            }
        }
        return status;
    }
}