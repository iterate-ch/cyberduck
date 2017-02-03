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
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.nio.charset.Charset;

public class CryptoDirectoryFeature<Reply> implements Directory<Reply> {

    private final Session<?> session;
    private final Directory<Reply> proxy;
    private final CryptoVault vault;

    public CryptoDirectoryFeature(final Session<?> session, final Directory<Reply> delegate, final Write<Reply> writer, final CryptoVault cryptomator) {
        this.session = session;
        this.proxy = delegate.withWriter(new CryptoWriteFeature<Reply>(session, writer, cryptomator));
        this.vault = cryptomator;
    }

    @Override
    public void mkdir(final Path directory) throws BackgroundException {
        this.mkdir(directory, null, new TransferStatus());
    }

    @Override
    public void mkdir(final Path directory, final String region, final TransferStatus status) throws BackgroundException {
        final Path target = vault.encrypt(session, directory);
        if(vault.contains(directory)) {
            final Path directoryMetafile = vault.encrypt(session, directory, true);
            new ContentWriter(session).write(directoryMetafile, directoryMetafile.attributes().getDirectoryId().getBytes(Charset.forName("UTF-8")));
            final Path intermediate = target.getParent();
            if(!session._getFeature(Find.class).find(intermediate)) {
                session._getFeature(Directory.class).mkdir(intermediate, region, status);
            }
            // Write header
            final Cryptor cryptor = vault.getCryptor();
            final FileHeader header = cryptor.fileHeaderCryptor().create();
            status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
        }
        proxy.mkdir(target, region, status);
    }

    @Override
    public CryptoDirectoryFeature<Reply> withWriter(final Write writer) {
        return this;
    }
}
