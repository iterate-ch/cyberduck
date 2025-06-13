package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.FileHeader;

import java.text.MessageFormat;

public class CryptoTouchFeature<Reply> implements Touch<Reply> {

    private final Session<?> session;
    private final Touch<Reply> proxy;
    private final AbstractVault vault;

    public CryptoTouchFeature(final Session<?> session, final Touch<Reply> proxy, final Write<Reply> writer, final AbstractVault cryptomator) {
        this.session = session;
        this.proxy = proxy.withWriter(new CryptoWriteFeature<>(session, writer, cryptomator));
        this.vault = cryptomator;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        // Write header
        final FileHeader header = vault.getFileHeaderCryptor().create();
        status.setHeader(vault.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RandomNonceGenerator(vault.getNonceSize()));
        final Path target = proxy.touch(vault.encrypt(session, file), new TransferStatus(status) {
            @Override
            public TransferStatus setResponse(final PathAttributes attributes) {
                status.setResponse(attributes);
                // Will be converted back to clear text when decrypting file below set in default touch feature implementation using writer.
                super.setResponse(new PathAttributes(attributes).setSize(vault.toCiphertextSize(0L, attributes.getSize())));
                return this;
            }
        });
        final Path decrypt = vault.decrypt(session, target);
        decrypt.attributes().setVersionId(target.attributes().getVersionId());
        return decrypt;
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(!vault.getFilenameProvider().isValid(filename)) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename));
        }
        proxy.preflight(vault.encrypt(session, workdir), filename);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoTouchFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
