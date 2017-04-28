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
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

public class CryptoTouchFeature<Reply> implements Touch<Reply> {

    private final Session<?> session;
    private final Touch<Reply> proxy;
    private final CryptoVault vault;

    public CryptoTouchFeature(final Session<?> session, final Touch<Reply> proxy, final Write<Reply> writer, final CryptoVault cryptomator) {
        this.session = session;
        this.proxy = proxy.withWriter(new CryptoWriteFeature<Reply>(session, writer, cryptomator));
        this.vault = cryptomator;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        if(vault.contains(file)) {
            // Write header
            final Cryptor cryptor = vault.getCryptor();
            final FileHeader header = cryptor.fileHeaderCryptor().create();
            status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
            status.setNonces(new RandomNonceGenerator());
        }
        final Path target = vault.encrypt(session, file);
        proxy.touch(target, status);
        file.getType().add(Path.Type.decrypted);
        file.attributes().setEncrypted(target);
        return file;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return proxy.isSupported(workdir);
    }

    @Override
    public CryptoTouchFeature<Reply> withWriter(final Write<Reply> writer) {
        return this;
    }
}
