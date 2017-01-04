package ch.cyberduck.core.cryptomator.impl;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.log4j.Logger;

public class CryptoDirectoryIdProvider {
    private static final Logger log = Logger.getLogger(CryptoDirectoryIdProvider.class);

    private final RandomStringService random = new UUIDRandomStringService();

    public String load(final Session<?> session, final Path directoryMetafile) throws BackgroundException {
        try {
            return new ContentReader(session).readToString(directoryMetafile);
        }
        catch(NotfoundException e) {
            log.warn(String.format("Return new random string for metadata file %s", directoryMetafile));
            return random.random();
        }
    }

    public void close() {
    }
}
