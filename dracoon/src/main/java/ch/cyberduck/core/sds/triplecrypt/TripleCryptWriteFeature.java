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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.sds.SDSNodeIdProvider;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.SDSTripleCryptEncryptorFeature;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.fasterxml.jackson.databind.ObjectReader;

public class TripleCryptWriteFeature implements Write<Node> {
    private static final Logger log = LogManager.getLogger(TripleCryptWriteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final Write<Node> proxy;

    public TripleCryptWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final Write<Node> proxy) {
        this.session = session;
        this.nodeid = nodeid;
        this.proxy = proxy;
    }

    @Override
    public StatusOutputStream<Node> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Read file key for file %s", file));
            }
            if(null == status.getFilekey()) {
                status.setFilekey(SDSTripleCryptEncryptorFeature.generateFileKey());
            }
            final FileKey fileKey = reader.readValue(status.getFilekey().array());
            return new TripleCryptEncryptingOutputStream(session, nodeid, proxy.write(file, status, callback),
                    Crypto.createFileEncryptionCipher(TripleCryptConverter.toCryptoPlainFileKey(fileKey)), status
            );
        }
        catch(CryptoSystemException | UnknownVersionException e) {
            throw new TripleCryptExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return proxy.append(file, status);
    }

    @Override
    public boolean random() {
        return proxy.random();
    }
}
