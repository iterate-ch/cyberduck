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
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FileLocksApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileLock;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileLockRequest;

import org.joda.time.DateTime;

public class StoregateLockFeature implements Lock<String> {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateLockFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public String lock(final Path file) throws BackgroundException {
        try {
            final FileLockRequest request = new FileLockRequest();
            request.setExpire(new DateTime().plusMillis(new HostPreferences(session.getHost()).getInteger("storegate.lock.ttl")));
            request.setOwner(session.getHost().getCredentials().getUsername());
            final FileLock lock = new FileLocksApi(this.session.getClient()).fileLocksCreateLock(fileid.getFileId(file
            ), request);
            return lock.getLockId();
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map("Failure to write attributes of {0}", e, file);
        }
    }

    @Override
    public void unlock(final Path file, final String token) throws BackgroundException {
        try {
            new FileLocksApi(this.session.getClient()).fileLocksDeleteLock(fileid.getFileId(file), token);
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map("Failure to write attributes of {0}", e, file);
        }
    }
}
