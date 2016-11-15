package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public abstract class ResponseOutputStream<T> extends ProxyOutputStream {
    private static final Logger log = Logger.getLogger(AbstractHttpWriteFeature.class);

    public ResponseOutputStream(final OutputStream proxy) {
        super(proxy);
    }

    /**
     * Return the response after closing the stream. Must close the stream first to prevent deadlock.
     *
     * @return A specific response header
     */
    public abstract T getResponse() throws BackgroundException;

    @Override
    public void close() throws IOException {
        super.close();
        try {
            final T response = this.getResponse();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Closed stream %s with response value %s", this, response));
            }
        }
        catch(BackgroundException e) {
            throw new IOException(e.getDetail(), e);
        }
    }
}
