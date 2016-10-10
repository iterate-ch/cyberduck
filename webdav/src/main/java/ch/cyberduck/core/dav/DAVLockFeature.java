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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.sardine.impl.SardineException;

public class DAVLockFeature<R> implements Bulk<Map<Path, String>> {

    private final DAVSession session;

    public DAVLockFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public Map<Path, String> pre(final Transfer.Type type, final Map<Path, TransferStatus> files) throws BackgroundException {
        switch(type) {
            case download:
                final Map<Path, String> locks = new HashMap<Path, String>();
                for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
                    final Path file = entry.getKey();
                    try {
                        locks.put(file, session.getClient().lock(new DAVPathEncoder().encode(file)));
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
        return Collections.emptyMap();
    }
}
