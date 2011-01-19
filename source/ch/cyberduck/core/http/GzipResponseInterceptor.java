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

import org.apache.http.*;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * This {@link org.apache.http.HttpResponseInterceptor} decompresses the {@link org.apache.http.HttpEntity}
 * of the {@link org.apache.http.HttpResponse} on the fly when
 * the content encoding indicates it should by replacing the entity of the response.
 */
public final class GzipResponseInterceptor implements HttpResponseInterceptor {

    /**
     * @param response
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        final HttpEntity entity = response.getEntity();
        if(null != entity) {
            final Header encoding = entity.getContentEncoding();
            if(null != encoding) {
                for(HeaderElement codec : encoding.getElements()) {
                    if(codec.getName().equalsIgnoreCase("gzip")) {
                        response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                        return;
                    }
                }
            }
        }
    }
}