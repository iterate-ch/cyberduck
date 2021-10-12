package ch.cyberduck.core.gmxcloud;/*
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.JSON;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GmxcloudMultipartUploadCompleter {

    private final GmxcloudSession session;

    public GmxcloudMultipartUploadCompleter(final GmxcloudSession session) {
        this.session = session;
    }

    public GmxcloudUploadResponse getCompletedUploadResponse(String uploadUri, long cumulativeLength, final String cdash64) throws IOException {
        final CloseableHttpClient client = session.getClient();
        final HttpPost httpPost = new HttpPost(uploadUri);
        GmxcloudUploadCompletionRequest requestModel = new GmxcloudUploadCompletionRequest();
        requestModel.setTotalSze(cumulativeLength);
        requestModel.setCdash64(cdash64);
        final String requestString = new JSON().getContext(GmxcloudUploadResponse.class).writeValueAsString(requestModel);
        final HttpEntity entity = EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON).setText(requestString).build();
        httpPost.setEntity(entity);
        final HttpResponse response = client.execute(httpPost);
        try {
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_CREATED:
                    return GmxcloudUploadHelper.getGmxcloudUploadResponse(response);
                case HttpStatus.SC_NO_CONTENT:
                    break;
                default:
                    throw new GmxcloudExceptionMappingService().map(new ApiException(response.getStatusLine().getStatusCode(), response.getFirstHeader("X-UI-Enhanced-Status").getValue(), Collections.emptyMap(),
                        EntityUtils.toString(response.getEntity())));
            }
        }
        catch(BackgroundException e) {
            throw new IOException(e);
        }
        finally {
            EntityUtils.consume(response.getEntity());
        }
        throw new IOException("Complete Uploaded Failed!");
    }


    private static class GmxcloudUploadCompletionRequest {

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
