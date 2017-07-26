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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.sds.SDSExceptionMappingService;
import ch.cyberduck.core.sds.SDSNodeIdProvider;
import ch.cyberduck.core.sds.SDSReadFeature;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.swagger.ExtendedNodesApi;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import eu.ssp_europe.sds.crypto.Crypto;
import eu.ssp_europe.sds.crypto.CryptoException;
import eu.ssp_europe.sds.crypto.CryptoUtils;
import eu.ssp_europe.sds.crypto.model.PlainFileKey;
import eu.ssp_europe.sds.crypto.model.UserKeyPair;
import eu.ssp_europe.sds.crypto.model.UserPrivateKey;

public class CryptoReadFeature implements Read {
    private static final Logger log = Logger.getLogger(CryptoReadFeature.class);

    private final SDSSession session;
    private final SDSReadFeature proxy;

    public CryptoReadFeature(final SDSSession session, final SDSReadFeature proxy) {
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback connectionCallback,
                            final PasswordCallback passwordCallback) throws BackgroundException {
        try {
            final FileKey key = new ExtendedNodesApi(session.getClient()).getUserFileKey(session.getToken(),
                    Long.parseLong(new SDSNodeIdProvider(session).getFileid(file, new DisabledListProgressListener())));
            final UserPrivateKey privateKey = new UserPrivateKey();
            final UserKeyPairContainer keyPairContainer = new UserApi(session.getClient()).getUserKeyPair(session.getToken());
            privateKey.setPrivateKey(keyPairContainer.getPrivateKeyContainer().getPrivateKey());
            privateKey.setVersion(keyPairContainer.getPrivateKeyContainer().getVersion());
            final Host bookmark = session.getHost();
            final VaultCredentials passphrase = new VaultCredentials(
                    PasswordStoreFactory.get().getPassword(bookmark.getHostname(),
                            String.format("Triple-Crypt (%s)", bookmark.getCredentials().getUsername()))) {
            };
            final UserKeyPair userKeyPair = new UserKeyPair();
            userKeyPair.setUserPrivateKey(privateKey);
            while(null == passphrase.getPassword() || !Crypto.checkUserKeyPair(userKeyPair, passphrase.getPassword())) {
                passwordCallback.prompt(passphrase, LocaleFactory.localizedString("Enter your encryption password", "Credentials"),
                        MessageFormat.format(LocaleFactory.localizedString("Enter your encryption password to decrypt {0}.", "Credentials"), file.getName()),
                        new LoginOptions()
                                .user(false)
                                .anonymous(false)
                                .icon(bookmark.getProtocol().disk())
                );
                if(passphrase.getPassword() == null) {
                    throw new LoginCanceledException();
                }
            }
            final PlainFileKey plainFileKey = Crypto.decryptFileKey(TripleCryptConverter.toCryptoEncryptedFileKey(key), privateKey, passphrase.getPassword());
            if(passphrase.isSaved()) {
                if(log.isInfoEnabled()) {
                    log.info("Save passphrase");
                }
                PasswordStoreFactory.get().addPassword(bookmark.getHostname(),
                        String.format("Triple-Crypt (%s)", bookmark.getCredentials().getUsername()), passphrase.getPassword());
            }
            return new CryptoInputStream(proxy.read(file, status, connectionCallback, passwordCallback),
                    Crypto.createFileDecryptionCipher(plainFileKey), CryptoUtils.stringToByteArray(plainFileKey.getTag()),
                    status.getLength() + status.getOffset());
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
