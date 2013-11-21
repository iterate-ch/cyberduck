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
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Id$
 */
public class HttpUploadFeature<Output, Digest> implements Upload<Output> {
    private static final Logger log = Logger.getLogger(HttpUploadFeature.class);

    private AbstractHttpWriteFeature writer;

    public HttpUploadFeature(final AbstractHttpWriteFeature<?> writer) {
        this.writer = writer;
    }

    @Override
    public Output upload(final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status) throws BackgroundException {
        return this.upload(file, local, throttle, listener, status, status, status);
    }

    public Output upload(final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status,
                         final StreamCancelation cancel, final StreamProgress progress) throws BackgroundException {
        try {
            InputStream in = null;
            ResponseOutputStream<Output> out = null;
            final Digest digest = this.digest();
            try {
                in = this.decorate(local.getInputStream(), digest);
                out = writer.write(file, status);
                new StreamCopier(cancel, progress).transfer(in, status.getCurrent(), new ThrottledOutputStream(out, throttle), listener, status.getLength());
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
            final Output response = out.getResponse();
            this.post(digest, response);
            return response;
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }

    protected InputStream decorate(final InputStream in, final Digest digest) throws IOException {
        return in;
    }

    protected Digest digest() {
        return null;
    }

    protected void post(final Digest pre, final Output response) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Received response %s", response));
        }
    }
}