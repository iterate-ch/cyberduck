package ch.cyberduck.core.b2;

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

import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Map;

import synapticloop.b2.request.B2CopyFileByIdRequest;
import synapticloop.b2.request.B2RequestProperties;
import synapticloop.b2.response.B2AuthorizeAccountResponse;

/**
 * Copy file with <code>metadataDirective</code> set to <code>REPLACE</code> to set content type and file info
 * of the new file instead of copying them from the source.
 */
public class B2CopyFileReplaceMetadataRequest extends B2CopyFileByIdRequest {

    private static final String KEY_METADATA_DIRECTIVE = "metadataDirective";
    private static final String METADATA_DIRECTIVE_REPLACE = "REPLACE";

    public B2CopyFileReplaceMetadataRequest(final CloseableHttpClient client, final B2AuthorizeAccountResponse authorization,
                                            final String sourceFileId, final String destinationBucketId, final String fileName,
                                            final String contentType, final Map<String, String> fileInfo) {
        super(client, authorization, sourceFileId, destinationBucketId, fileName);
        this.addProperty(KEY_METADATA_DIRECTIVE, METADATA_DIRECTIVE_REPLACE);
        // Required with REPLACE
        this.addProperty(B2RequestProperties.KEY_CONTENT_TYPE, contentType);
        this.addProperty(B2RequestProperties.KEY_FILE_INFO, fileInfo);
    }
}
