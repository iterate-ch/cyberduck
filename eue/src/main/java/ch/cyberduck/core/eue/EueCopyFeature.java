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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.CopyChildrenApi;
import ch.cyberduck.core.eue.io.swagger.client.api.UpdateResourceApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceUpdateModel;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceUpdateModelUpdate;
import ch.cyberduck.core.eue.io.swagger.client.model.Uifs;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

public class EueCopyFeature implements Copy {

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueCopyFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path file, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final EueApiClient client = new EueApiClient(session);
            new CopyChildrenApi(client).resourceResourceIdChildrenCopyPost(fileid.getFileId(target.getParent(), new DisabledListProgressListener()),
                    Collections.singletonList(String.format("%s/resource/%s", session.getBasePath(), fileid.getFileId(file,
                            new DisabledListProgressListener()))), null, null, null,
                    status.isExists() ? "overwrite" : null, null);
            listener.sent(status.getLength());
            if(!StringUtils.equals(file.getName(), target.getName())) {
                final ResourceUpdateModel resourceUpdateModel = new ResourceUpdateModel();
                ResourceUpdateModelUpdate resourceUpdateModelUpdate = new ResourceUpdateModelUpdate();
                final Uifs uifs = new Uifs();
                uifs.setName(target.getName());
                resourceUpdateModelUpdate.setUifs(uifs);
                resourceUpdateModel.setUpdate(resourceUpdateModelUpdate);
                new UpdateResourceApi(client).resourceResourceIdPatch(fileid.getFileId(new Path(target.getParent(), file.getName(), file.getType()),
                        new DisabledListProgressListener()),
                        resourceUpdateModel, null, null, null);
            }
            return target.withAttributes(new EueAttributesFinderFeature(session, fileid).find(target, new DisabledListProgressListener()));
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map("Cannot copy {0}", e, file);
        }
    }
}
