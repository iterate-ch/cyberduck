package ch.cyberduck.core.gmxcloud;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.JSON;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.PostChildrenApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.PostChildrenForAliasApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.PostResourceApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.FileUpdateResponseRepresentation;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationRepresentationArrayInner;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntries;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceResourceIdBody;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.Uifs;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UploadType;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class GmxcloudUploadHelper {
    private static final Logger log = Logger.getLogger(GmxcloudUploadHelper.class);

    private static final String refId = "X-UI-CDOS-RefId";
    private static final String storeId = "X-UI-CDOS-StoreId";

    /**
     * Read response headers from segment upload
     */
    public static GmxcloudUploadResponse parseUploadResponse(final HttpResponse response) throws IOException, NotfoundException {
        final GmxcloudUploadResponse uploadResponse = new GmxcloudUploadResponse();
        if(response.containsHeader(storeId)) {
            uploadResponse.setStoreId(response.getFirstHeader(storeId).getValue());
        }
        if(response.containsHeader(refId)) {
            uploadResponse.setReferenceId(response.getFirstHeader(refId).getValue());
        }
        else {
            return parseUploadCompletedResponse(response);
        }
        return uploadResponse;
    }

    /**
     * Read response body from completed multipart upload
     */
    public static GmxcloudUploadResponse parseUploadCompletedResponse(final HttpResponse response) throws IOException {
        final GmxcloudUploadResponse uploadResponse = new GmxcloudUploadResponse();
        final UiFsModel uiFsModel = new JSON().getContext(UiFsModel.class).readValue(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8), UiFsModel.class);
        final Uifs uifs = uiFsModel.getUifs();
        uploadResponse.setTotalSze(uifs.getSize());
        uploadResponse.setCdash64(uifs.getCdash64());
        uploadResponse.setReferenceId(uifs.getReferenceId());
        uploadResponse.setStoreId(uifs.getStoreId());
        return uploadResponse;
    }

    public static FileUpdateResponseRepresentation updateResource(final GmxcloudSession session, final String resourceId,
                                                                  final UploadType uploadType) throws BackgroundException {
        try {
            return new PostResourceApi(new GmxcloudApiClient(session)).resourceResourceIdPost(
                    resourceId, new ResourceResourceIdBody().uploadType(uploadType), null, null, null, null);
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map(e);
        }
    }

    public static ResourceCreationResponseEntry createResource(final GmxcloudSession session, final String resourceId, final String filename,
                                                               final TransferStatus status, final UploadType uploadType) throws BackgroundException {
        final ResourceCreationRepresentationArrayInner resourceCreationRepresentation = new ResourceCreationRepresentationArrayInner();
        resourceCreationRepresentation.setForceOverwrite(true);
        resourceCreationRepresentation.setPath(filename);
        if(TransferStatus.UNKNOWN_LENGTH != status.getLength()) {
            resourceCreationRepresentation.setSize(status.getLength());
        }
        resourceCreationRepresentation.setUploadType(uploadType);
        resourceCreationRepresentation.setResourceType(ResourceCreationRepresentationArrayInner.ResourceTypeEnum.FILE);
        try {
            final ResourceCreationResponseEntries resourceCreationResponseEntries;
            final GmxcloudApiClient client = new GmxcloudApiClient(session);
            switch(resourceId) {
                case GmxcloudResourceIdProvider.ROOT:
                    resourceCreationResponseEntries = new PostChildrenForAliasApi(client)
                            .resourceAliasAliasChildrenPost(resourceId, Collections.singletonList(resourceCreationRepresentation),
                                    null, null, null, null, null);
                    break;
                default:
                    resourceCreationResponseEntries = new PostChildrenApi(client)
                            .resourceResourceIdChildrenPost(resourceId, Collections.singletonList(resourceCreationRepresentation),
                                    null, null, null, null, null);
                    break;
            }
            if(!resourceCreationResponseEntries.containsKey(filename)) {
                throw new NotfoundException(filename);
            }
            final ResourceCreationResponseEntry resourceCreationResponseEntry = resourceCreationResponseEntries.get(filename);
            switch(resourceCreationResponseEntry.getStatusCode()) {
                case HttpStatus.SC_CREATED:
                    break;
                default:
                    log.warn(String.format("Failure %s creating file %s", resourceCreationResponseEntry, filename));
                    if(null == resourceCreationResponseEntry.getEntity()) {
                        throw new GmxcloudExceptionMappingService().map(new ApiException(resourceCreationResponseEntry.getReason(),
                                null, resourceCreationResponseEntry.getStatusCode(), client.getResponseHeaders()));
                    }
                    throw new GmxcloudExceptionMappingService().map(new ApiException(resourceCreationResponseEntry.getEntity().getError(),
                            null, resourceCreationResponseEntry.getStatusCode(), client.getResponseHeaders()));
            }
            return resourceCreationResponseEntry;
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map(e);
        }
    }

    public static class GmxcloudUploadResponse {
        @JsonProperty("cdash64")
        private String cdash64 = null;

        @JsonProperty("referenceId")
        private String referenceId = null;

        @JsonProperty("storeId")
        private String storeId = null;

        @JsonProperty("totalSize")
        private Long totalSze = null;

        public String getCdash64() {
            return cdash64;
        }

        public void setCdash64(final String cdash64) {
            this.cdash64 = cdash64;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(final String referenceId) {
            this.referenceId = referenceId;
        }

        public String getStoreId() {
            return storeId;
        }

        public void setStoreId(final String storeId) {
            this.storeId = storeId;
        }

        public Long getTotalSze() {
            return totalSze;
        }

        public void setTotalSze(final Long totalSze) {
            this.totalSze = totalSze;
        }
    }
}
