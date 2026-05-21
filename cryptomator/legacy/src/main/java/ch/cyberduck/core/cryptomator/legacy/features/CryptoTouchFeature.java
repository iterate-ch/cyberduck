package ch.cyberduck.core.cryptomator.legacy.features;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.legacy.CryptomatorVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.FileHeader;

import java.text.MessageFormat;
import java.util.Optional;

public class CryptoTouchFeature<Reply> implements Touch<Reply> {

    private final Session<?> session;
    private final Touch<Reply> proxy;
    private final CryptomatorVault cryptomator;

    public CryptoTouchFeature(final Session<?> session, final Touch<Reply> proxy, final CryptomatorVault cryptomator) {
        this.session = session;
        this.proxy = proxy;
        this.cryptomator = cryptomator;
    }

    @Override
    public Path touch(final Write<Reply> writer, final Path file, final TransferStatus status) throws BackgroundException {
        // Write header
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RandomNonceGenerator(cryptomator.getNonceSize()));
        final Path target = proxy.touch(writer, cryptomator.encrypt(session, file), new TransferStatus(status) {
            @Override
            public TransferStatus setResponse(final PathAttributes attributes) {
                status.setResponse(attributes);
                // Will be converted back to clear text when decrypting file below set in default touch feature implementation using writer.
                super.setResponse(new DefaultPathAttributes(attributes).setSize(cryptomator.toCiphertextSize(0L, attributes.getSize())));
                return this;
            }
        });
        final Path decrypt = cryptomator.decrypt(session, target);
        decrypt.attributes().setVersionId(target.attributes().getVersionId());
        return decrypt;
    }

    @Override
    public void preflight(final Path workdir, final Optional<String> filename) throws BackgroundException {
        if(filename.isPresent()) {
            if(!cryptomator.getFilenameProvider().isValid(filename.get())) {
                throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename));
            }
        }
        proxy.preflight(cryptomator.encrypt(session, workdir), filename);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoTouchFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
