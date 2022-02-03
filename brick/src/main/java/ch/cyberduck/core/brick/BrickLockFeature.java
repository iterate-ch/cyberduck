package ch.cyberduck.core.brick;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.LocksApi;
import ch.cyberduck.core.brick.io.swagger.client.model.LocksPathBody;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Lock;

import org.apache.commons.lang3.StringUtils;

public class BrickLockFeature implements Lock<String> {

    private final BrickSession session;

    public BrickLockFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public String lock(final Path file) throws BackgroundException {
        try {
            return new LocksApi(new BrickApiClient(session))
                .postLocksPath(StringUtils.removeStart(file.getAbsolute(), String.valueOf(Path.DELIMITER)),
                    new LocksPathBody().exclusive(true).allowAccessByAnyUser(true)).getToken();
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    @Override
    public void unlock(final Path file, final String token) throws BackgroundException {
        try {
            new LocksApi(new BrickApiClient(session))
                .deleteLocksPath(StringUtils.removeStart(file.getAbsolute(), String.valueOf(Path.DELIMITER)), token);
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
