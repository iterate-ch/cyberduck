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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.worker.SessionListWorker;

import org.apache.log4j.Logger;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.cryptomator.cryptolib.api.Cryptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoSessionListWorker extends SessionListWorker {
    private static final Logger log = Logger.getLogger(CryptoSessionListWorker.class);

    private static final Pattern BASE32_PATTERN = Pattern.compile("^0?(([A-Z2-7]{8})*[A-Z2-7=]{8})");

    private final Cache<Path> cache;
    private final Path directory;
    private final ListProgressListener listener;
    private final Cryptor cryptor;
    private final CryptoPathMapper pathMapper;
    private final LongFileNameProvider longFileNameProvider;

    public CryptoSessionListWorker(final Cache<Path> cache, final Path directory, final ListProgressListener listener,
                                   final Cryptor cryptor, final CryptoPathMapper pathMapper,
                                   final LongFileNameProvider longFileNameProvider) {
        super(cache, directory, listener);
        this.cache = cache;
        this.directory = directory;
        this.listener = listener;
        this.cryptor = cryptor;
        this.pathMapper = pathMapper;
        this.longFileNameProvider = longFileNameProvider;
    }

    @Override
    public AttributedList<Path> run(final Session<?> session) throws BackgroundException {
        try {
            if(this.isCached()) {
                final AttributedList<Path> list = cache.get(directory);
                super.chunk(directory, list);
                return list;
            }
            else {
                final CryptoPathMapper.Directory ciphertextDirectory = pathMapper.getCiphertextDir(directory);
                final AttributedList<Path> encryptedList = session.list(ciphertextDirectory.path, this);
                final AttributedList<Path> cleartextList = new AttributedList<>();
                for(Path path : encryptedList) {
                    final Path inflated = inflateIfNeeded(path);
                    if(inflated == null) {
                        continue;
                    }
                    final Path decrypted = decrypt(ciphertextDirectory.dirId, inflated);
                    if(decrypted == null) {
                        continue;
                    }
                    // noch filter wie in CryptoDirectoryStream#iterator?
                    cleartextList.add(decrypted);
                }
                return cleartextList;
            }
        }
        catch(ListCanceledException e) {
            //TODO ist ciphertextList
            return e.getChunk();
        }
        catch(IOException e) {
            throw new BackgroundException(e);
        }
    }

    @Override
    public void chunk(final Path parent, final AttributedList<Path> list) throws ConnectionCanceledException {
        // TODO decrypt
        super.chunk(parent, list);
    }

    private Path inflateIfNeeded(final Path ciphertextPath) {
        final String fileName = ciphertextPath.getName();
        if(LongFileNameProvider.isDeflated(fileName)) {
            try {
                final String longFileName = longFileNameProvider.inflate(fileName);
                return new Path(ciphertextPath.getParent(), longFileName, ciphertextPath.getType(), ciphertextPath.attributes());
            }
            catch(IOException e) {
                log.warn(ciphertextPath + " could not be inflated.");
                return null;
            }
        }
        else {
            return ciphertextPath;
        }
    }

    private Path decrypt(final String dirId, final Path ciphertextPath) {
        final String ciphertextFileName = ciphertextPath.getName();
        final Matcher m = BASE32_PATTERN.matcher(ciphertextFileName);
        if(m.find()) {
            String ciphertext = m.group(1);
            try {
                final String cleartextFilename = cryptor.fileNameCryptor().decryptFilename(ciphertext, dirId.getBytes(StandardCharsets.UTF_8));
                return new Path(directory, cleartextFilename, ciphertextFileName.startsWith(Constants.DIR_PREFIX) ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), ciphertextPath.attributes());
            }
            catch(AuthenticationFailedException e) {
                log.warn(ciphertextPath + " not decryptable due to an unauthentic ciphertext.");
                return null;
            }
        }
        else {
            return null;
        }
    }
}
