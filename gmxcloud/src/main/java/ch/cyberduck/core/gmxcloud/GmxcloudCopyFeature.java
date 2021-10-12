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
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.CopyChildrenApi;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.ArrayList;
import java.util.EnumSet;

public class GmxcloudCopyFeature implements Copy {

    private final GmxcloudSession session;
    private final GmxcloudIdProvider fileid;

    public GmxcloudCopyFeature(final GmxcloudSession session, final GmxcloudIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path file, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final ArrayList<String> fileList = new ArrayList<>();
            final String fileToBeCopied = session.getBasePath() + Constant.RESOURCE + fileid.getFileId(file, new DisabledListProgressListener());
            fileList.add(fileToBeCopied);
            final CopyChildrenApi childrenApi = new CopyChildrenApi(new GmxcloudApiClient(session));
            childrenApi.resourceResourceIdChildrenCopyPost(fileid.getFileId(target, new DisabledListProgressListener()),
                fileList, null, null, null, null, null);
            listener.sent(status.getLength());
            final Path copiedFilePath = new Path(target, file.getName(), EnumSet.of(Path.Type.file));
            return target.withAttributes(new GmxcloudAttributesFinderFeature(session, fileid).find(copiedFilePath, new DisabledListProgressListener()));
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Cannot copy {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }
}
