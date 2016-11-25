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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.EnumSet;

public class CryptoWriteFeature implements Write {

    private final Write delegate;
    private final SessionCryptomatorLoader cryptomator;

    public CryptoWriteFeature(final Write delegate, final SessionCryptomatorLoader cryptomator) {
        this.delegate = delegate;
        this.cryptomator = cryptomator;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final CryptoPathMapper pathMapper = cryptomator.getCryptoPathMapper();
            final CryptoPathMapper.Directory ciphertextDirectory = pathMapper.getCiphertextDir(file.getParent());
            final String ciphertextFileName = pathMapper.getCiphertextFileName(ciphertextDirectory.dirId, file.getName(), EnumSet.of(Path.Type.file));
            final Path cryptoPath = new Path(ciphertextDirectory.path, ciphertextFileName, EnumSet.of(Path.Type.file));

            // hHeader
            final Cryptor cryptor = cryptomator.getCryptor();
            final FileHeader header = cryptor.fileHeaderCryptor().create();
            header.setFilesize(-1);
            final ByteBuffer headerBuffer = cryptor.fileHeaderCryptor().encryptHeader(header);
            final OutputStream cryptoStream = delegate.write(cryptoPath, status);
            cryptoStream.write(headerBuffer.array());

            // content
            return new CryptoOutputStream(cryptoStream, cryptor, header);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        //TODO implement
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean temporary() {
        //TODO check
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }
}
