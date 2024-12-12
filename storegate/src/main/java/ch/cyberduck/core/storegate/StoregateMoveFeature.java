package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.JSON;
import ch.cyberduck.core.storegate.io.swagger.client.model.MoveFileRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import static ch.cyberduck.core.features.Move.validate;
import static com.google.api.client.json.Json.MEDIA_TYPE;

public class StoregateMoveFeature implements Move {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateMoveFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback delete, final ConnectionCallback callback) throws BackgroundException {
        try {
            final StoregateApiClient client = session.getClient();
            final MoveFileRequest move = new MoveFileRequest()
                .name(renamed.getName())
                .parentID(fileid.getFileId(renamed.getParent()))
                .mode(1); // Overwrite
            final HttpEntityEnclosingRequestBase request;
            request = new HttpPost(String.format("%s/v4.2/files/%s/move", client.getBasePath(), fileid.getFileId(file)));
            if(status.getLockId() != null) {
                request.addHeader("X-Lock-Id", status.getLockId().toString());
            }
            request.setEntity(new StringEntity(new JSON().getContext(move.getClass()).writeValueAsString(move),
                ContentType.create("application/json", StandardCharsets.UTF_8.name())));
            request.addHeader(HTTP.CONTENT_TYPE, MEDIA_TYPE);
            final HttpResponse response = client.getClient().execute(request);
            try {
                switch(response.getStatusLine().getStatusCode()) {
                    case HttpStatus.SC_NO_CONTENT:
                        final PathAttributes attr = new PathAttributes(file.attributes());
                        fileid.cache(file, null);
                        fileid.cache(renamed, attr.getFileId());
                        return renamed.withAttributes(attr);
                    default:
                        throw new StoregateExceptionMappingService(fileid).map("Cannot rename {0}",
                                new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file);
                }
            }
            finally {
                EntityUtils.consume(response.getEntity());
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        Move.super.preflight(source, target);
        validate(session.getCaseSensitivity(), source, target);
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }
}
