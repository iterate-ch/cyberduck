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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoListService implements ListService {
    private static final Logger log = Logger.getLogger(CryptoListService.class);

    private static final Pattern BASE32_PATTERN = Pattern.compile("^0?(([A-Z2-7]{8})*[A-Z2-7=]{8})");

    private final ListService delegate;
    private final SessionCryptomatorLoader cryptomator;

    public CryptoListService(final ListService delegate, final SessionCryptomatorLoader cryptomator) {
        this.delegate = delegate;
        this.cryptomator = cryptomator;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final CryptoPathMapper.Directory ciphertextDirectory = cryptomator.getCryptoPathMapper().getCiphertextDir(directory);
            final AttributedList<Path> encrypted = delegate.list(ciphertextDirectory.path, listener);
            final AttributedList<Path> cleartext = new AttributedList<>();
            for(Path f : encrypted) {
                try {
                    final Path inflated = inflateIfNeeded(f);
                    final Path decrypted = decrypt(directory, ciphertextDirectory.dirId, inflated);
                    // noch filter wie in CryptoDirectoryStream#iterator?
                    cleartext.add(decrypted);
                }
                catch(CryptoAuthenticationException e) {
                    log.error(String.format("Failure decrypting %s. %s", f, e.getMessage()));
                }
            }
            return cleartext;
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private Path inflateIfNeeded(final Path ciphertextPath) throws AccessDeniedException {
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

    private Path decrypt(final Path directory, final String dirId, final Path ciphertextPath) throws AccessDeniedException {
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
                        "Failure to decrypt due to an unauthentic ciphertext");
            }
        }
        else {
            throw new CryptoAuthenticationException(
                    String.format("Failure to decrypt due to missing pattern match for %s", BASE32_PATTERN));
        }
    }
}
