package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;

public class LocalQuotaFeature implements Quota {

    private final LocalSession session;

    public LocalQuotaFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        final Path home = new DefaultHomeFinderService(session).find();
        try {
            final FileStore store = Files.getFileStore(session.toPath(home));
            return new Space(store.getTotalSpace() - store.getUsableSpace(), store.getUsableSpace());
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Failure to read attributes of {0}", e, home);
        }
    }
}
