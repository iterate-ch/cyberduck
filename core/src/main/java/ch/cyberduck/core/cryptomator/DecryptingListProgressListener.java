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
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.log4j.Logger;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecryptingListProgressListener extends IndexedListProgressListener {
    private static final Logger log = Logger.getLogger(DecryptingListProgressListener.class);

    private static final Pattern BASE32_PATTERN = Pattern.compile("^0?(([A-Z2-7]{8})*[A-Z2-7=]{8})");

    private final CryptoVault cryptomator;
    private final Path directory;
    private final CryptoPathMapper.Directory ciphertextDirectory;
    private final ListProgressListener delegate;

    public DecryptingListProgressListener(final CryptoVault cryptomator,
                                          final Path directory, final ListProgressListener delegate) throws IOException {
        this.cryptomator = cryptomator;
        this.directory = directory;
        this.ciphertextDirectory = cryptomator.getCryptoPathMapper().getCiphertextDir(directory);
        this.delegate = delegate;
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path file) throws ListCanceledException {
        for(int i = index; i < list.size(); i++) {
            final Path f = list.get(i);
            try {
                final Path inflated = this.inflate(f);
                final Path decrypted = this.decrypt(directory, ciphertextDirectory.dirId, inflated);
                list.set(i, decrypted);
            }
            catch(CryptoAuthenticationException e) {
                log.error(String.format("Failure decrypting %s. %s", f, e.getMessage()));
            }
        }
    }


    private Path inflate(final Path ciphertextPath) throws CryptoAuthenticationException {
        final String fileName = ciphertextPath.getName();
        if(LongFileNameProvider.isDeflated(fileName)) {
            try {
                final String longFileName = cryptomator.getLongFileNameProvider().inflate(fileName);
                return new Path(ciphertextPath.getParent(), longFileName, ciphertextPath.getType(), ciphertextPath.attributes());
            }
            catch(IOException e) {
                throw new CryptoAuthenticationException(
                        String.format("Failure to inflate filename from %s", ciphertextPath.getName()), e);
            }
        }
        else {
            return ciphertextPath;
        }
    }

    private Path decrypt(final Path directory, final String dirId, final Path ciphertextPath) throws CryptoAuthenticationException {
        final Matcher m = BASE32_PATTERN.matcher(ciphertextPath.getName());
        if(m.find()) {
            final String ciphertext = m.group(1);
            try {
                final String cleartextFilename = cryptomator.getCryptor().fileNameCryptor().decryptFilename(
                        ciphertext, dirId.getBytes(StandardCharsets.UTF_8));
                return new Path(directory, cleartextFilename,
                        ciphertextPath.getName().startsWith(Constants.DIR_PREFIX) ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), ciphertextPath.attributes());
            }
            catch(AuthenticationFailedException e) {
                throw new CryptoAuthenticationException(
                        "Failure to decrypt due to an unauthentic ciphertext", e);
            }
        }
        else {
            throw new CryptoAuthenticationException(
                    String.format("Failure to decrypt due to missing pattern match for %s", BASE32_PATTERN));
        }
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) throws ConnectionCanceledException {
        super.chunk(folder, list);
        delegate.chunk(folder, list);
    }

    @Override
    public void message(final String message) {
        delegate.message(message);
    }
}
