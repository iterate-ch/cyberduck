package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

public class DeepboxDeleteFeature implements Delete {

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxDeleteFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Map.Entry<Path, TransferStatus> file : files.entrySet()) {
            try {
                callback.delete(file.getKey());
                final CoreRestControllerApi coreApi = new CoreRestControllerApi(session.getClient());
                coreApi.deletePurgeNode(UUID.fromString(fileid.getFileId(file.getKey())), false);
                fileid.cache(file.getKey(), null);
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(fileid).map(e);
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}
