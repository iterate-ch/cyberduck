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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

import static com.google.api.client.json.Json.MEDIA_TYPE;

public class StoregateDeleteFeature implements Delete {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateDeleteFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Map.Entry<Path, TransferStatus> file : files.entrySet()) {
            try {
                callback.delete(file.getKey());
                final StoregateApiClient client = session.getClient();
                final HttpRequestBase request;
                request = new HttpDelete(String.format("%s/v4.2/files/%s", client.getBasePath(), fileid.getFileId(file.getKey())));
                if(file.getValue().getLockId() != null) {
                    request.addHeader("X-Lock-Id", file.getValue().getLockId().toString());
                }
                request.addHeader(HTTP.CONTENT_TYPE, MEDIA_TYPE);
                final HttpResponse response = client.getClient().execute(request);
                try {
                    switch(response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_NO_CONTENT:
                            break;
                        default:
                            throw new StoregateExceptionMappingService(fileid).map("Cannot delete {0}",
                                    new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file.getKey());
                    }
                }
                finally {
                    EntityUtils.consume(response.getEntity());
                }
                fileid.cache(file.getKey(), null);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, file.getKey());
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}
