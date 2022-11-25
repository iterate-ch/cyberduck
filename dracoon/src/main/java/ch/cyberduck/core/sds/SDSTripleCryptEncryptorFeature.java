package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Encryptor;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptEncryptingInputStream;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.fasterxml.jackson.databind.ObjectReader;

public class SDSTripleCryptEncryptorFeature implements Encryptor {
    private static final Logger log = LogManager.getLogger(SDSTripleCryptEncryptorFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSTripleCryptEncryptorFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public InputStream encrypt(final Path file, final InputStream proxy, final TransferStatus status) throws BackgroundException {
        try {
            final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Read file key for file %s", file));
            }
            if(null == status.getFilekey()) {
                status.setFilekey(nodeid.getFileKey());
            }
            final FileKey fileKey = reader.readValue(status.getFilekey().array());
            return new TripleCryptEncryptingInputStream(session, proxy,
                    Crypto.createFileEncryptionCipher(TripleCryptConverter.toCryptoPlainFileKey(fileKey)), status);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(CryptoSystemException | UnknownVersionException e) {
            throw new TripleCryptExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }
}
