package ch.cyberduck.core.eue;

/*
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.eue.io.swagger.client.JSON;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EueMultipartUploadCompleter {

    private final EueSession session;

    public EueMultipartUploadCompleter(final EueSession session) {
        this.session = session;
    }

    public EueUploadHelper.UploadResponse getCompletedUploadResponse(String uploadUri, long length, final String cdash64) throws BackgroundException {
        try {
            final HttpPost request = new HttpPost(uploadUri);
            final UploadCompletionRequest uploadCompletionRequest = new UploadCompletionRequest();
            uploadCompletionRequest.setTotalSze(length);
            uploadCompletionRequest.setCdash64(cdash64);
            final String requestString = new JSON().getContext(EueUploadHelper.UploadResponse.class).writeValueAsString(uploadCompletionRequest);
            final HttpEntity entity = EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON).setText(requestString).build();
            request.setEntity(entity);
            final HttpResponse response = session.getClient().execute(request);
            try {
                switch(response.getStatusLine().getStatusCode()) {
                    case HttpStatus.SC_OK:
                    case HttpStatus.SC_CREATED:
                        return EueUploadHelper.parseUploadCompletedResponse(response);
                    default:
                        throw new EueExceptionMappingService().map(response);
                }
            }
            finally {
                EntityUtils.consume(response.getEntity());
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private static final class UploadCompletionRequest {
        @JsonProperty("cdash64")
        private String cdash64 = null;

        @JsonProperty("totalSize")
        private Long totalSze = null;

        public String getCdash64() {
            return cdash64;
        }

        public void setCdash64(final String cdash64) {
            this.cdash64 = cdash64;
        }

        public Long getTotalSze() {
            return totalSze;
        }

        public void setTotalSze(final Long totalSze) {
            this.totalSze = totalSze;
        }
    }
}
