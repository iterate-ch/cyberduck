package ch.cyberduck.core.brick;

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
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.brick.io.swagger.client.model.FilesPathBody1;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.joda.time.DateTime;

public class BrickTimestampFeature extends DefaultTimestampFeature {

    private final BrickSession session;

    public BrickTimestampFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            new FilesApi(new BrickApiClient(session.getApiKey(), session.getClient())).patchFilesPath(file.getAbsolute(),
                new FilesPathBody1().providedMtime(new DateTime(status.getTimestamp())));
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
