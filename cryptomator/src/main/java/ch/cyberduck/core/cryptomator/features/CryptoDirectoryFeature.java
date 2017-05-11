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
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.serializer.PathAttributesDictionary;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.nio.charset.Charset;

public class CryptoDirectoryFeature<Reply> implements Directory<Reply> {
    private static final Logger log = Logger.getLogger(CryptoDirectoryFeature.class);

    private final Session<?> session;
    private final Directory<Reply> proxy;
    private final CryptoVault vault;
    private final RandomStringService random
            = new UUIDRandomStringService();

    public CryptoDirectoryFeature(final Session<?> session, final Directory<Reply> delegate, final Write<Reply> writer, final CryptoVault cryptomator) {
        this.session = session;
        this.proxy = delegate.withWriter(new CryptoWriteFeature<Reply>(session, writer, cryptomator));
        this.vault = cryptomator;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        if(!status.isExists()) {
            folder.attributes().setDirectoryId(random.random());
        }
        final Path encrypt = vault.encrypt(session, folder);
        // Create metadata file for directory
        final Path directoryMetadataFile = vault.encrypt(session, folder, true);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write metadata %s for folder %s", directoryMetadataFile, folder));
        }
        new ContentWriter(session).write(directoryMetadataFile, encrypt.attributes().getDirectoryId().getBytes(Charset.forName("UTF-8")));
        final Path intermediate = encrypt.getParent();
        if(!session._getFeature(Find.class).find(intermediate)) {
            session._getFeature(Directory.class).mkdir(intermediate, region, new TransferStatus());
        }
        // Write header
        final Cryptor cryptor = vault.getCryptor();
        final FileHeader header = cryptor.fileHeaderCryptor().create();
        status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RandomNonceGenerator());
        proxy.mkdir(encrypt, region, status);
        final Path copy = new Path(folder.getParent(), folder.getName(), folder.getType(),
                new PathAttributesDictionary().deserialize(folder.attributes().serialize(SerializerFactory.get())));
        copy.getType().add(Path.Type.decrypted);
        copy.attributes().setEncrypted(encrypt);
        copy.attributes().setVault(vault.getHome());
        return copy;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return proxy.isSupported(workdir);
    }

    @Override
    public CryptoDirectoryFeature<Reply> withWriter(final Write<Reply> writer) {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDirectoryFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
