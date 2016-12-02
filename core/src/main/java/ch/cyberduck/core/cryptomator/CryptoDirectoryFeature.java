package ch.cyberduck.core.cryptomator;

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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.transfer.TransferStatus;

import java.nio.charset.Charset;

public class CryptoDirectoryFeature implements Directory {

    private final Session<?> session;
    private final Directory delegate;
    private final Vault vault;

    public CryptoDirectoryFeature(final Session<?> session, final Directory delegate, final Vault cryptomator) {
        this.session = session;
        this.delegate = delegate;
        this.vault = cryptomator;
    }

    @Override
    public void mkdir(final Path directory) throws BackgroundException {
        this.mkdir(directory, null, null);
    }

    @Override
    public void mkdir(final Path directory, final String region, final TransferStatus status) throws BackgroundException {
        final Path directoryMetafile = vault.encrypt(session, directory, true);
        final Path directoryPath = vault.encrypt(session, directory);
        final String directoryId = directoryPath.attributes().getDirectoryId();
        final ContentWriter writer = new ContentWriter(session);
        writer.write(directoryMetafile, directoryId.getBytes(Charset.forName("UTF-8")));

        final Path firstLevel = directoryPath.getParent();
        if(!session._getFeature(Find.class).find(firstLevel)) {
            delegate.mkdir(firstLevel);
        }
        delegate.mkdir(directoryPath);
    }
}
