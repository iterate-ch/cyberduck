package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.sardine.impl.SardineException;

public class DavLockFeature<R> implements Bulk<Set<String>> {

    private final DAVSession session;

    public DavLockFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public Set<String> pre(final Transfer.Type type, final Map<Path, TransferStatus> files) throws BackgroundException {
        final Set<String> locks = new HashSet<String>();
        for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
            final Path file = entry.getKey();
            try {
                locks.add(session.getClient().lock(new DAVPathEncoder().encode(file)));
            }
            catch(SardineException e) {
                throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, file);
            }
        }
        return locks;
    }
}
