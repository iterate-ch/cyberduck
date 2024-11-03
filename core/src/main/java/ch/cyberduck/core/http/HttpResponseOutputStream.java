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
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public abstract class HttpResponseOutputStream<Reply> extends StatusOutputStream<Reply> {
    private static final Logger log = LogManager.getLogger(StatusOutputStream.class);

    private final AttributesAdapter<Reply> attributes;
    private final TransferStatus status;

    public HttpResponseOutputStream(final OutputStream proxy, final AttributesAdapter<Reply> attributes, final TransferStatus status) {
        super(proxy);
        this.attributes = attributes;
        this.status = status;
    }

    @Override
    public void close() throws IOException {
        super.close();
        try {
            final Reply response = this.getStatus();
            if(response != null) {
                log.debug("Closed stream {} with response value {}", this, response);
                status.withResponse(attributes.toAttributes(response)).setComplete();
            }
        }
        catch(BackgroundException e) {
            throw new IOException(e.getDetail(), e);
        }
    }
}
