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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.JSON;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.PostChildrenApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationRepresentationArrayInner;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntries;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.Uifs;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GmxcloudUploadHelper {

    private GmxcloudUploadHelper() {
    }

    public static GmxcloudUploadResponse getGmxcloudUploadResponse(final HttpResponse response) throws IOException, NotfoundException {
        GmxcloudUploadResponse gmxcloudUploadResponse = new GmxcloudUploadResponse();
        if(response.getEntity().getContent().available() == 0) {
            final String refId = "X-UI-CDOS-RefId";
            final String storeId = "X-UI-CDOS-StoreId";
            if(!response.containsHeader(refId) && !response.containsHeader(storeId)) {
                throw new NotfoundException(String.format("Header: %s and %s are not available in the response", refId, storeId));
            }
            gmxcloudUploadResponse.setReferenceId(response.getFirstHeader(refId).getValue());
            gmxcloudUploadResponse.setStoreId(response.getFirstHeader(storeId).getValue());
            return gmxcloudUploadResponse;
        }
        final UiFsModel uiFsModel = new JSON().getContext(UiFsModel.class).readValue(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8),
            UiFsModel.class);
        final Uifs uifs = uiFsModel.getUifs();
        gmxcloudUploadResponse.setTotalSze(uifs.getSize().longValue());
        gmxcloudUploadResponse.setCdash64(uifs.getCdash64());
        gmxcloudUploadResponse.setReferenceId(uifs.getReferenceId());
        gmxcloudUploadResponse.setStoreId(uifs.getStoreId());
        return gmxcloudUploadResponse;
    }

    public static ResourceCreationResponseEntry getUploadResourceCreationResponseEntry(final GmxcloudSession gmxcloudSession, final Path file, final ResourceCreationRepresentationArrayInner.UploadTypeEnum uploadType, final String resourceId) throws BackgroundException {
        final GmxcloudApiClient client = new GmxcloudApiClient(gmxcloudSession);
        final PostChildrenApi postChildrenApi = new PostChildrenApi(client);
        List<ResourceCreationRepresentationArrayInner> resourceCreationRepresentationArrayInners = new ArrayList<>();
        ResourceCreationRepresentationArrayInner resourceCreationRepresentationArrayInner = new ResourceCreationRepresentationArrayInner();
        final String fileName = file.getName();
        resourceCreationRepresentationArrayInner.setPath(fileName);
        resourceCreationRepresentationArrayInner.setUploadType(uploadType);
        resourceCreationRepresentationArrayInner.setResourceType(ResourceCreationRepresentationArrayInner.ResourceTypeEnum.FILE);
        resourceCreationRepresentationArrayInners.add(resourceCreationRepresentationArrayInner);
        try {
            final ResourceCreationResponseEntries resourceCreationResponseEntries = postChildrenApi.resourceResourceIdChildrenPost(resourceId, resourceCreationRepresentationArrayInners, null, null, null, null, null);
            return resourceCreationResponseEntries.get(fileName);
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map(e);
        }
    }

}
