package ch.cyberduck.core.sds.triplecrypt;

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
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.CryptoSystemException;
import com.dracoon.sdk.crypto.InvalidFileKeyException;
import com.fasterxml.jackson.databind.ObjectReader;

public class CryptoWriteFeature implements Write<VersionId> {
    private static final Logger log = Logger.getLogger(CryptoWriteFeature.class);

    private final SDSSession session;
    private final Write<VersionId> proxy;

    public CryptoWriteFeature(final SDSSession session, final Write<VersionId> proxy) {
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public StatusOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Read file key for file %s", file));
            }
            final FileKey fileKey = reader.readValue(status.getFilekey().array());
            return new CryptoOutputStream<VersionId>(session, proxy.write(file, status, callback),
                    Crypto.createFileEncryptionCipher(TripleCryptConverter.toCryptoPlainFileKey(fileKey)), status
            );
        }
        catch(CryptoSystemException | InvalidFileKeyException e) {
            throw new CryptoExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return proxy.append(file, length, cache);
    }

    @Override
    public boolean temporary() {
        return proxy.temporary();
    }

    @Override
    public boolean random() {
        return proxy.random();
    }
}
