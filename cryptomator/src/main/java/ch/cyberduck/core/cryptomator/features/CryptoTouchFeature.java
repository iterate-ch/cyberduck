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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.cryptomator.cryptolib.api.FileHeader;

public class CryptoTouchFeature<Reply> implements Touch<Reply> {

    private final Session<?> session;
    private final Touch<Reply> proxy;
    private final CryptoVault vault;

    public CryptoTouchFeature(final Session<?> session, final Touch<Reply> proxy, final Write<Reply> writer, final CryptoVault cryptomator) {
        this.session = session;
        this.proxy = proxy.withWriter(new CryptoWriteFeature<>(session, writer, cryptomator));
        this.vault = cryptomator;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        // Write header
        final FileHeader header = vault.getFileHeaderCryptor().create();
        status.setHeader(vault.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RandomNonceGenerator());
        final Path target = proxy.touch(vault.encrypt(session, file), new TransferStatus(status) {
            @Override
            public void setResponse(final PathAttributes attributes) {
                status.setResponse(attributes);
                // Will be converted back to clear text when decrypting file below set in default touch feature implementation using writer.
                super.setResponse(new PathAttributes(attributes).withSize(vault.toCiphertextSize(0L, attributes.getSize())));
            }
        });
        final Path decrypt = vault.decrypt(session, target);
        decrypt.attributes().withVersionId(target.attributes().getVersionId());
        return decrypt;
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        return proxy.isSupported(workdir, StringUtils.EMPTY) && vault.getFilenameProvider().isValid(filename);
    }

    @Override
    public CryptoTouchFeature<Reply> withWriter(final Write<Reply> writer) {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoTouchFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
