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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.MoveChildrenApi;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.ArrayList;
import java.util.EnumSet;

public class GmxcloudMoveFeature implements Move {

    private final GmxcloudSession session;
    private final GmxcloudIdProvider fileid;

    public GmxcloudMoveFeature(final GmxcloudSession session, final GmxcloudIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path file, final Path target, final TransferStatus status, final Delete.Callback delete, final ConnectionCallback callback) throws BackgroundException {
        try {
            final GmxcloudApiClient client = new GmxcloudApiClient(session);
            final ArrayList<String> fileList = new ArrayList<>();
            final String fileTobeMoved = session.getBasePath() + Constant.RESOURCE + fileid.getFileId(file, new DisabledListProgressListener());
            fileList.add(fileTobeMoved);
            new MoveChildrenApi(client)
                .resourceResourceIdChildrenMovePost(fileid.getFileId(target, new DisabledListProgressListener()),
                    fileList, null, null, null, null, null);
            final Path movedFilePath = new Path(target, file.getName(), EnumSet.of(Path.Type.file));
            fileid.cache(file, null);
            return target.withAttributes(new GmxcloudAttributesFinderFeature(session, fileid).find(movedFilePath, new DisabledListProgressListener()));
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }
}
