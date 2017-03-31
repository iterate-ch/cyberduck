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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.util.HashMap;
import java.util.Map;

public class CryptoBulkFeature<R> implements Bulk<R> {
    private final Session<?> session;
    private final Bulk<R> proxy;
    private final CryptoVault cryptomator;
    private final RandomStringService random
            = new UUIDRandomStringService();

    public CryptoBulkFeature(final Session<?> session, final Bulk<R> delegate, final Delete delete, final CryptoVault cryptomator) {
        this.session = session;
        this.proxy = delegate.withDelete(new CryptoDeleteFeature(session, delete, cryptomator));
        this.cryptomator = cryptomator;
    }

    @Override
    public R pre(final Transfer.Type type, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Map<Path, TransferStatus> encrypted = new HashMap<>(files.size());
        for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
            final Path file = entry.getKey();
            final TransferStatus status = entry.getValue();
            if(!status.isExists()) {
                if(file.isDirectory()) {
                    file.attributes().setDirectoryId(random.random());
                }
            }
            encrypted.put(cryptomator.encrypt(session, file), status);
            if(cryptomator.contains(file)) {
                // Write header
                final Cryptor cryptor = cryptomator.getCryptor();
                final FileHeader header = cryptor.fileHeaderCryptor().create();
                status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
            }
        }
        return proxy.pre(type, encrypted, callback);
    }

    @Override
    public Bulk<R> withDelete(final Delete delete) {
        return this;
    }

    @Override
    public void post(final Transfer.Type type, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Map<Path, TransferStatus> encrypted = new HashMap<>(files.size());
        for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
            final Path file = entry.getKey();
            final TransferStatus status = entry.getValue();
            encrypted.put(cryptomator.encrypt(session, file), status);
        }
        proxy.post(type, encrypted, callback);
    }
}
