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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateFilePropertiesRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.joda.time.DateTime;

public class StoregateTimestampFeature extends DefaultTimestampFeature {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateTimestampFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final FilesApi files = new FilesApi(session.getClient());
            files.filesUpdateFile(fileid.getFileId(file),
                new UpdateFilePropertiesRequest().modified(new DateTime(status.getTimestamp())));
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map("Failure to write attributes of {0}", e, file);
        }
    }
}
