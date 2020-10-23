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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
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

import org.apache.log4j.Logger;

import java.io.InputStream;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.CryptoUtils;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.dracoon.sdk.crypto.model.UserKeyPair;

public class TripleCryptReadFeature implements Read {
    private static final Logger log = Logger.getLogger(TripleCryptReadFeature.class);

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
                Long.parseLong(nodeid.getFileid(file, new DisabledListProgressListener())), null, null);
            final EncryptedFileKey encFileKey = TripleCryptConverter.toCryptoEncryptedFileKey(key);
            final UserKeyPairContainer keyPairContainer = this.getKeyPairForFileKey(encFileKey);
            final UserKeyPair userKeyPair = TripleCryptConverter.toCryptoUserKeyPair(keyPairContainer);

            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt to unlock private key %s", userKeyPair.getUserPrivateKey()));
            }
            final Credentials passphrase;
            try {
                passphrase = new TripleCryptKeyPair().unlock(callback, session.getHost(), userKeyPair);
            }
            catch(LoginCanceledException e) {
                throw new AccessDeniedException(LocaleFactory.localizedString("Decryption password required", "SDS"), e);
            }
            final PlainFileKey plainFileKey = Crypto.decryptFileKey(encFileKey, userKeyPair.getUserPrivateKey(), passphrase.getPassword());
            return new TripleCryptInputStream(proxy.read(file, status, callback),
                Crypto.createFileDecryptionCipher(plainFileKey), CryptoUtils.stringToByteArray(plainFileKey.getTag()));
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(CryptoException e) {
            throw new TripleCryptExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return false;
    }

    private UserKeyPairContainer getKeyPairForFileKey(EncryptedFileKey key) throws BackgroundException {
        switch(key.getVersion()) {
            case RSA2048_AES256GCM:
                return session.keyPairDeprecated();
            case RSA4096_AES256GCM:
                return session.keyPair();
            default:
                throw new InteroperabilityException(String.format("Unknown version %s", key.getVersion()));
        }
    }
}
