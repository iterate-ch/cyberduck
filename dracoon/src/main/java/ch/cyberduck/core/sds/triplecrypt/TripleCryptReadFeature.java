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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.sds.SDSExceptionMappingService;
import ch.cyberduck.core.sds.SDSNodeIdProvider;
import ch.cyberduck.core.sds.SDSReadFeature;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.error.InvalidFileKeyException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.dracoon.sdk.crypto.model.UserKeyPair;

public class TripleCryptReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(TripleCryptReadFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final SDSReadFeature proxy;

    public TripleCryptReadFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final SDSReadFeature proxy) {
        this.session = session;
        this.nodeid = nodeid;
        this.proxy = proxy;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final FileKey key = new NodesApi(session.getClient()).requestUserFileKey(
                Long.parseLong(nodeid.getVersionId(file)), null, null);
            final EncryptedFileKey encFileKey = TripleCryptConverter.toCryptoEncryptedFileKey(key);
            try {
                final UserKeyPair userKeyPair = this.getUserKeyPair(encFileKey);
                final PlainFileKey plainFileKey = Crypto.decryptFileKey(encFileKey, userKeyPair.getUserPrivateKey(), this.unlock(callback, userKeyPair).getPassword().toCharArray());
                return new TripleCryptDecryptingInputStream(proxy.read(file, status, callback),
                        Crypto.createFileDecryptionCipher(plainFileKey), plainFileKey.getTag());
            }
            catch(InvalidFileKeyException e) {
                log.warn("Failure {}  decrypting file key for {}. Invalidate cache", e, file);
                session.resetUserKeyPairs();
                final UserKeyPair userKeyPair = this.getUserKeyPair(encFileKey);
                final PlainFileKey plainFileKey = Crypto.decryptFileKey(encFileKey, userKeyPair.getUserPrivateKey(), this.unlock(callback, userKeyPair).getPassword().toCharArray());
                return new TripleCryptDecryptingInputStream(proxy.read(file, status, callback),
                        Crypto.createFileDecryptionCipher(plainFileKey), plainFileKey.getTag());
            }
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Download {0} failed", e, file);
        }
        catch(CryptoException e) {
            throw new TripleCryptExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    private Credentials unlock(final ConnectionCallback callback, final UserKeyPair userKeyPair) throws CryptoException, BackgroundException {
        final Credentials passphrase;
        try {
            passphrase = new TripleCryptKeyPair().unlock(callback, session.getHost(), userKeyPair);
        }
        catch(LoginCanceledException e) {
            throw new AccessDeniedException(LocaleFactory.localizedString("Decryption password required", "SDS"), e);
        }
        return passphrase;
    }

    private UserKeyPair getUserKeyPair(final EncryptedFileKey encFileKey) throws BackgroundException, UnknownVersionException {
        final UserKeyPairContainer keyPairContainer = session.getKeyPairForFileKey(encFileKey.getVersion());
        final UserKeyPair userKeyPair = TripleCryptConverter.toCryptoUserKeyPair(keyPairContainer);
        log.debug("Attempt to unlock private key {}", userKeyPair.getUserPrivateKey());
        return userKeyPair;
    }

    @Override
    public boolean offset(final Path file) {
        return false;
    }
}
