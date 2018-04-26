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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.CryptoException;
import com.dracoon.sdk.crypto.CryptoUtils;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.dracoon.sdk.crypto.model.UserKeyPair;
import com.dracoon.sdk.crypto.model.UserPrivateKey;

public class CryptoReadFeature implements Read {
    private static final Logger log = Logger.getLogger(CryptoReadFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final SDSReadFeature proxy;

    public CryptoReadFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final SDSReadFeature proxy) {
        this.session = session;
        this.nodeid = nodeid;
        this.proxy = proxy;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final FileKey key = new NodesApi(session.getClient()).getUserFileKey(StringUtils.EMPTY,
                Long.parseLong(nodeid.getFileid(file, new DisabledListProgressListener())));
            final UserPrivateKey privateKey = new UserPrivateKey();
            final UserKeyPairContainer keyPairContainer = session.keyPair();
            privateKey.setPrivateKey(keyPairContainer.getPrivateKeyContainer().getPrivateKey());
            privateKey.setVersion(keyPairContainer.getPrivateKeyContainer().getVersion());
            final UserKeyPair userKeyPair = new UserKeyPair();
            userKeyPair.setUserPrivateKey(privateKey);
            final Credentials passphrase = new TripleCryptKeyPair().unlock(callback, session.getHost(), userKeyPair);
            final PlainFileKey plainFileKey = Crypto.decryptFileKey(TripleCryptConverter.toCryptoEncryptedFileKey(key), privateKey, passphrase.getPassword());
            return new CryptoInputStream(proxy.read(file, status, callback),
                    Crypto.createFileDecryptionCipher(plainFileKey), CryptoUtils.stringToByteArray(plainFileKey.getTag()));
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(CryptoException e) {
            throw new CryptoExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return false;
    }
}
