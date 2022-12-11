package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileResponse;

public class B2CopyFeature implements Copy {

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2CopyFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final B2FileResponse response = session.getClient().copyFile(fileid.getVersionId(source),
                fileid.getVersionId(containerService.getContainer(target)),
                containerService.getKey(target));
            listener.sent(status.getLength());
            fileid.cache(target, response.getFileId());
            return target.withAttributes(new B2AttributesFinderFeature(session, fileid).toAttributes(response));
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map("Cannot copy {0}", e, source);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(source.getType().contains(Path.Type.upload)) {
            return false;
        }
        return containerService.getContainer(source).equals(containerService.getContainer(target));
    }
}
