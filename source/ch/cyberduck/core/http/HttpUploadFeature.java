package ch.cyberduck.core.http;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Id:$
 */
public class HttpUploadFeature implements Upload {
    private static final Logger log = Logger.getLogger(HttpUploadFeature.class);

    private AbstractHttpWriteFeature writer;

    public HttpUploadFeature(final AbstractHttpWriteFeature<?> writer) {
        this.writer = writer;
    }

    @Override
    public void upload(final Path file, Local local, final BandwidthThrottle throttle,
                       final StreamListener listener, final TransferStatus status) throws BackgroundException {
        try {
            InputStream in = null;
            ResponseOutputStream<?> out = null;
            try {
                in = local.getInputStream();
                out = writer.write(file, status);
                new StreamCopier(status).transfer(in, status.getCurrent(), new ThrottledOutputStream(out, throttle), listener);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
            final Object response = out.getResponse();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Received response %s", response));
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }
}