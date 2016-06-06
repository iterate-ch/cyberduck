package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.io.ContentLengthInputStream;

public class SwiftReadFeature implements Read {
    private static final Logger log = Logger.getLogger(SwiftReadFeature.class);

    private PathContainerService containerService
            = new SwiftPathContainerService();

    private SwiftSession session;

    private SwiftRegionService regionService;

    public SwiftReadFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final ContentLengthInputStream stream;
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                if(-1 == range.getEnd()) {
                    stream = session.getClient().getObject(regionService.lookup(file),
                            containerService.getContainer(file).getName(), containerService.getKey(file),
                            range.getStart());
                }
                else {
                    stream = session.getClient().getObject(regionService.lookup(file),
                            containerService.getContainer(file).getName(), containerService.getKey(file),
                            range.getStart(), range.getLength());
                }
            }
            else {
                stream = session.getClient().getObject(regionService.lookup(file),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Reading stream with content length %d", stream.getLength()));
            }
            return stream;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return true;
    }
}
