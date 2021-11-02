package ch.cyberduck.core.box;/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.box.io.swagger.client.model.UploadPart;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSessionIdCommitBody;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class BoxMultipartUploadCommitter {
    private static final Logger log = Logger.getLogger(BoxMultipartUploadCommitter.class);
    private final BoxSession session;

    BoxMultipartUploadCommitter(BoxSession session) {
        this.session = session;
    }

    public Files commitUploadSession(String file, String basePath, String uploadSessionId, TransferStatus overall, List<UploadPart> uploadParts) throws IOException {
        final HttpPost request = new HttpPost(String.format("%s/files/upload_sessions/%s/commit", basePath, uploadSessionId));
        if(!Checksum.NONE.equals(overall.getChecksum())) {
            request.addHeader(new BasicHeader("Digest", String.format("sha=%s", overall.getChecksum().hash)));
        }
        final ByteArrayOutputStream content = new ByteArrayOutputStream();
        new JSON().getContext(null).writeValue(content, new UploadSessionIdCommitBody().parts(uploadParts));
        request.setEntity(new ByteArrayEntity(content.toByteArray()));
        if(overall.isExists()) {
            if(StringUtils.isNotBlank(overall.getRemote().getETag())) {
                request.addHeader(new BasicHeader(HttpHeaders.IF_MATCH, overall.getRemote().getETag()));
            }
            else {
                log.warn(String.format("Missing remote attributes in transfer status to read current ETag for %s", file));
            }
        }
        return session.getClient().execute(request, new BoxClientErrorResponseHandler<Files>() {
            @Override
            public Files handleEntity(final HttpEntity entity) throws IOException {
                return new JSON().getContext(null).readValue(entity.getContent(), Files.class);
            }
        });
    }
}
