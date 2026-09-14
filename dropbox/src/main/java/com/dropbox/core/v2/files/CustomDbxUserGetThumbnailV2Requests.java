package com.dropbox.core.v2.files;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import java.io.IOException;
import java.util.Collections;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWrappedException;
import com.dropbox.core.stone.StructSerializer;
import com.dropbox.core.v2.DbxRawClientV2;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

/**
 * Replacement for {@link DbxUserFilesRequests#getThumbnailV2Builder} serializing the request argument without the
 * quality field rejected by the server with <code>Error in call to API function "files/get_thumbnail_v2":
 * unexpected key 'quality'</code>.
 */
public final class CustomDbxUserGetThumbnailV2Requests {

    private CustomDbxUserGetThumbnailV2Requests() {
        //
    }

    /**
     * Serializer for {@link ThumbnailV2Arg} omitting the quality field
     */
    private static final StructSerializer<ThumbnailV2Arg> SERIALIZER = new StructSerializer<ThumbnailV2Arg>() {
        @Override
        public void serialize(final ThumbnailV2Arg value, final JsonGenerator g, final boolean collapse) throws IOException {
            if(!collapse) {
                g.writeStartObject();
            }
            g.writeFieldName("resource");
            PathOrLink.Serializer.INSTANCE.serialize(value.getResource(), g);
            g.writeFieldName("format");
            ThumbnailFormat.Serializer.INSTANCE.serialize(value.getFormat(), g);
            g.writeFieldName("size");
            ThumbnailSize.Serializer.INSTANCE.serialize(value.getSize(), g);
            g.writeFieldName("mode");
            ThumbnailMode.Serializer.INSTANCE.serialize(value.getMode(), g);
            if(!collapse) {
                g.writeEndObject();
            }
        }

        @Override
        public ThumbnailV2Arg deserialize(final JsonParser p, final boolean collapsed) {
            throw new UnsupportedOperationException();
        }
    };

    public static DbxDownloader<PreviewResult> getThumbnailV2(final DbxRawClientV2 client, final PathOrLink resource,
                                                              final ThumbnailSize size) throws ThumbnailV2ErrorException, DbxException {
        final ThumbnailV2Arg arg = ThumbnailV2Arg.newBuilder(resource).withSize(size).build();
        try {
            return client.downloadStyle(client.getHost().getContent(),
                    "2/files/get_thumbnail_v2",
                    arg,
                    false,
                    Collections.emptyList(),
                    SERIALIZER,
                    PreviewResult.Serializer.INSTANCE,
                    ThumbnailV2Error.Serializer.INSTANCE);
        }
        catch(DbxWrappedException ex) {
            throw new ThumbnailV2ErrorException("2/files/get_thumbnail_v2", ex.getRequestId(), ex.getUserMessage(),
                    (ThumbnailV2Error) ex.getErrorValue());
        }
    }
}
