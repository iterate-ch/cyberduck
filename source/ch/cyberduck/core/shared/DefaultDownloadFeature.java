package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public class DefaultDownloadFeature implements Download {

    private Read reader;

    public DefaultDownloadFeature(final Session<?> session) {
        this.reader = session.getFeature(Read.class);
    }

    public DefaultDownloadFeature(final Read reader) {
        this.reader = reader;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status, final LoginCallback callback) throws BackgroundException {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = reader.read(file, status);
                out = local.getOutputStream(status.isAppend());
                new StreamCopier(status, status)
                        .withOffset(0L)
                        .withLimit(status.getLength())
                        .withListener(listener)
                        .transfer(new ThrottledInputStream(in, throttle), out);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, file);
        }
    }
}
