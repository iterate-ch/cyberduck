package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveRequest;
import org.nuxeo.onedrive.client.OneDriveResponse;

import java.net.URL;
import java.util.List;

public class OneDriveDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(OneDriveDeleteFeature.class);

    private final OneDriveSession session;

    public OneDriveDeleteFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            callback.delete(file);

            // evaluating query
            StringBuilder builder = session.getBaseUrlStringBuilder();

            PathContainerService pathContainerService = new PathContainerService();
            session.resolveDriveQueryPath(file, builder, pathContainerService);

            final URL apiUrl = session.getUrl(builder);

            try {
                OneDriveRequest request = new OneDriveRequest(session.getClient(), apiUrl, "DELETE");
                OneDriveResponse response = request.send();
                final int responseCode = response.getResponseCode();
                if(responseCode != 204) {
                    log.error(String.format("Could not delete %s. API reponse %d", file.getAbsolute(), responseCode));
                }
            }
            catch(OneDriveAPIException e) {
                throw new OneDriveExceptionMappingService().map(e);
            }
        }
    }
}
