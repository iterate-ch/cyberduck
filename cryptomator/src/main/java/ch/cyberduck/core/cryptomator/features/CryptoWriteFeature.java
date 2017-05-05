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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoOutputStream;
import ch.cyberduck.core.cryptomator.CryptoPathCache;
import ch.cyberduck.core.cryptomator.CryptoVault;
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

import java.io.IOException;

public class CryptoWriteFeature<Reply> implements Write<Reply> {

    private final Session<?> session;
    private final Write<Reply> proxy;
    private final Find finder;
    private final AttributesFinder attributes;
    private final CryptoVault vault;

    public CryptoWriteFeature(final Session<?> session, final Write<Reply> proxy, final CryptoVault vault) {
        this(session, proxy,
                new CryptoFindFeature(session, new DefaultFindFeature(session), vault),
                new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), vault),
                vault);
    }

    public CryptoWriteFeature(final Session<?> session, final Write<Reply> proxy, final Find finder, final AttributesFinder attributes, final CryptoVault vault) {
        this.session = session;
        this.proxy = proxy;
        this.finder = finder;
        this.attributes = attributes;
        this.vault = vault;
    }

    @Override
    public StatusOutputStream<Reply> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final Path encrypted = vault.encrypt(session, file);
            final Cryptor cryptor = vault.getCryptor();
            final StatusOutputStream<Reply> proxy;
            if(status.getOffset() == 0) {
                proxy = this.proxy.write(encrypted,
                        new TransferStatus(status).length(vault.toCiphertextSize(status.getLength())), callback);
                proxy.write(status.getHeader().array());
            }
            else {
                proxy = this.proxy.write(encrypted,
                        new TransferStatus(status).length(vault.toCiphertextSize(status.getLength()) - cryptor.fileHeaderCryptor().headerSize()), callback);
            }
            return new CryptoOutputStream<Reply>(proxy, cryptor, cryptor.fileHeaderCryptor().decryptHeader(status.getHeader()),
                    status.getNonces(), vault.numberOfChunks(status.getOffset()));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(new CryptoPathCache(cache)).find(vault.encrypt(session, file))) {
            final PathAttributes attributes = this.attributes.withCache(new CryptoPathCache(cache)).find(vault.encrypt(session, file));
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return proxy.temporary();
    }

    @Override
    public boolean random() {
        return proxy.random();
    }

    @Override
    public ChecksumCompute checksum() {
        return new CryptoChecksumCompute(proxy.checksum(), vault);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoWriteFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
