package ch.cyberduck.core.gmxcloud;

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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.UpdateResourceApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceUpdateModel;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceUpdateModelUpdate;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiWin32;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.joda.time.DateTime;

public class GmxcloudTimestampFeature extends DefaultTimestampFeature {

    private final GmxcloudSession session;
    private final GmxcloudIdProvider fileid;

    public GmxcloudTimestampFeature(final GmxcloudSession session, final GmxcloudIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final String resourceId = fileid.getFileId(file, new DisabledListProgressListener());
            ResourceUpdateModel resourceUpdateModel = new ResourceUpdateModel();
            ResourceUpdateModelUpdate resourceUpdateModelUpdate = new ResourceUpdateModelUpdate();
            UiWin32 uiWin32 = new UiWin32();
            uiWin32.setLastModificationMillis(new DateTime(status.getTimestamp()).getMillis());
            resourceUpdateModelUpdate.setUiwin32(uiWin32);
            resourceUpdateModel.setUpdate(resourceUpdateModelUpdate);
            new UpdateResourceApi(new GmxcloudApiClient(session)).resourceResourceIdPatch(resourceId, resourceUpdateModel, null, null, null);
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
