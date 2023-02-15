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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.cryptomator.random.RotatingNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.download.PathPriorityComparator;

import org.cryptomator.cryptolib.api.FileHeader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CryptoBulkFeature<R> implements Bulk<R> {

    private final RandomStringService random
            = new UUIDRandomStringService();

    private final Session<?> session;
    private final Bulk<R> delegate;
    private final CryptoVault cryptomator;

    public CryptoBulkFeature(final Session<?> session, final Bulk<R> delegate, final Delete delete, final CryptoVault cryptomator) {
        this.session = session;
        this.delegate = delegate.withDelete(cryptomator.getFeature(session, Delete.class, delete));
        this.cryptomator = cryptomator;
    }

    @Override
    public R pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Map<TransferItem, TransferStatus> encrypted = new HashMap<>(files.size());
        final ArrayList<Map.Entry<TransferItem, TransferStatus>> sorted = new ArrayList<>(files.entrySet());
        // Sort with folder first in list
        sorted.sort(new Comparator<Map.Entry<TransferItem, TransferStatus>>() {
            @Override
            public int compare(final Map.Entry<TransferItem, TransferStatus> o1, final Map.Entry<TransferItem, TransferStatus> o2) {
                return new PathPriorityComparator().compare(o1.getKey().remote, o2.getKey().remote);
            }
        });
        for(Map.Entry<TransferItem, TransferStatus> entry : sorted) {
            final Path file = entry.getKey().remote;
            final Local local = entry.getKey().local;
            final TransferStatus status = entry.getValue();
            if(null == status.getHeader()) {
                // Write header to be reused in writer
                final FileHeader header = cryptomator.getFileHeaderCryptor().create();
                status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
            }
            if(null == status.getNonces()) {
                status.setNonces(status.getLength() == TransferStatus.UNKNOWN_LENGTH ?
                        new RandomNonceGenerator(cryptomator.getNonceSize()) :
                        new RotatingNonceGenerator(cryptomator.getNonceSize(), cryptomator.numberOfChunks(status.getLength())));
            }
            if(file.isDirectory()) {
                if(!status.isExists()) {
                    switch(type) {
                        case upload:
                            // Preset directory ID for new folders to avert lookup with not found failure in directory ID provider
                            final String directoryId = random.random();
                            encrypted.put(new TransferItem(cryptomator.encrypt(session, file, directoryId, false), local), status);
                            break;
                        default:
                            encrypted.put(new TransferItem(cryptomator.encrypt(session, file), local), status);
                            break;
                    }
                }
                else {
                    encrypted.put(new TransferItem(cryptomator.encrypt(session, file), local), status);
                }
            }
            else {
                encrypted.put(new TransferItem(cryptomator.encrypt(session, file), local), status);
            }
        }
        return delegate.pre(type, encrypted, callback);
    }

    @Override
    public Bulk<R> withDelete(final Delete delete) {
        delegate.withDelete(cryptomator.getFeature(session, Delete.class, delete));
        return this;
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Map<TransferItem, TransferStatus> encrypted = new HashMap<>(files.size());
        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
            final TransferStatus status = entry.getValue();
            encrypted.put(new TransferItem(cryptomator.encrypt(session, entry.getKey().remote), entry.getKey().local), status);
        }
        delegate.post(type, encrypted, callback);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoBulkFeature{");
        sb.append("proxy=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
