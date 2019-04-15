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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;

import java.util.List;

public class StoregateDeleteFeature implements Delete {

    private final StoregateSession session;
    private final StoregateIdProvider id;

    public StoregateDeleteFeature(final StoregateSession session, final StoregateIdProvider id) {
        this.session = session;
        this.id = id;
    }

    @Override
    public void delete(final List<Path> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            try {
                final FilesApi api = new FilesApi(session.getClient());
                api.filesDelete(id.getFileid(file, new DisabledListProgressListener()));
            }
            catch(ApiException e) {
                throw new StoregateExceptionMappingService().map(e);
            }
        }
    }

    @Override
    public boolean isRecursive() {
        return false;
    }
}
