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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;

public class CryptoWriteFeature<Reply> implements Write<Reply> {

    private final Session<?> session;
    private final Write<Reply> delegate;
    private final Find finder;
    private final AttributesFinder attributes;
    private final CryptoVault vault;

    public CryptoWriteFeature(final Session<?> session, final Write<Reply> delegate, final CryptoVault vault) {
        this(session, delegate,
                new CryptoFindFeature(session, new DefaultFindFeature(session), vault),
                new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), vault),
                vault);
    }

    public CryptoWriteFeature(final Session<?> session, final Write<Reply> delegate, final Find finder, final AttributesFinder attributes, final CryptoVault vault) {
        this.session = session;
        this.delegate = delegate;
        this.finder = finder;
        this.attributes = attributes;
        this.vault = vault;
    }

    @Override
    public StatusOutputStream<Reply> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(vault.contains(file)) {
            try {
                final Path encrypted = vault.encrypt(session, file);
                final Cryptor cryptor = vault.getCryptor();
                // Header
                final FileHeader header = cryptor.fileHeaderCryptor().decryptHeader(status.getHeader());
                final StatusOutputStream<Reply> proxy;
                if(status.getOffset() == 0) {
                    proxy = delegate.write(encrypted, status.length(vault.toCiphertextSize(status.getLength())), callback);
                    proxy.write(cryptor.fileHeaderCryptor().encryptHeader(header).array());
                }
                else {
                    proxy = delegate.write(encrypted, status.length(vault.toCiphertextSize(status.getLength()) -
                            cryptor.fileHeaderCryptor().headerSize()), callback);
                }
                return new CryptoOutputStream<Reply>(proxy, cryptor, header, status.getNonces());
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        return delegate.write(file, status, callback);
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(vault.encrypt(session, file))) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(vault.encrypt(session, file));
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return delegate.temporary();
    }

    @Override
    public boolean random() {
        return delegate.random();
    }

    @Override
    public ChecksumCompute checksum() {
        return new CryptoChecksumCompute(delegate.checksum(), vault);
    }
}
