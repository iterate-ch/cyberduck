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

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public final class GzipDecompressingEntity extends HttpEntityWrapper {

    public GzipDecompressingEntity(final HttpEntity entity) {
        super(entity);
    }

    /**
     * Decompress compressed entity using GZIP
     *
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        InputStream wrapped = wrappedEntity.getContent();
        // the wrapped entity's getContent() decides about repeatability
        return new GZIPInputStream(wrapped);
    }

    @Override
    public long getContentLength() {
        // length of ungzipped content is not known
        return -1;
    }
}
