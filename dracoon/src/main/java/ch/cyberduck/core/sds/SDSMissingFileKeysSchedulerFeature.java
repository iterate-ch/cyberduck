package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.FileFileKeys;
import ch.cyberduck.core.sds.io.swagger.client.model.MissingKeysResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeySetBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeySetRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserIdFileIdItem;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.io.swagger.client.model.UserUserPublicKey;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptKeyPair;
import ch.cyberduck.core.shared.AbstractSchedulerFeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.error.InvalidFileKeyException;
import com.dracoon.sdk.crypto.error.InvalidKeyPairException;
import com.dracoon.sdk.crypto.error.InvalidPasswordException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.dracoon.sdk.crypto.model.UserKeyPair;
import com.dracoon.sdk.crypto.model.UserPrivateKey;
import com.dracoon.sdk.crypto.model.UserPublicKey;

public class SDSMissingFileKeysSchedulerFeature extends AbstractSchedulerFeature<List<UserFileKeySetRequest>, SDSApiClient> {
    private static final Logger log = Logger.getLogger(SDSMissingFileKeysSchedulerFeature.class);

    public SDSMissingFileKeysSchedulerFeature() {
        this(PreferencesFactory.get().getLong("sds.encryption.missingkeys.scheduler.period"));
    }

    public SDSMissingFileKeysSchedulerFeature(final long period) {
        super(period);
    }

    @Override
    public List<UserFileKeySetRequest> operate(final Session<SDSApiClient> session, final PasswordCallback callback, final Path file) throws BackgroundException {
        try {
            final UserAccountWrapper account = ((SDSSession) session).userAccount();
            if(!account.isEncryptionEnabled()) {
                log.warn(String.format("No key pair found in user account %s", account));
                return Collections.emptyList();
            }
            final List<UserFileKeySetRequest> processed = new ArrayList<>();
            final UserKeyPairContainer keyPairContainer = ((SDSSession) session).keyPair();
            final UserPrivateKey privateKey = new UserPrivateKey(UserKeyPair.Version.getByValue(keyPairContainer.getPrivateKeyContainer().getVersion()),
                keyPairContainer.getPrivateKeyContainer().getPrivateKey());
            final UserPublicKey publicKey = new UserPublicKey(UserKeyPair.Version.getByValue(keyPairContainer.getPublicKeyContainer().getVersion()),
                keyPairContainer.getPublicKeyContainer().getPublicKey());
            final UserKeyPair userKeyPair = new UserKeyPair(privateKey, publicKey);
            final Credentials passphrase = new TripleCryptKeyPair().unlock(callback, session.getHost(), userKeyPair);
            final IdProvider node = session.getFeature(IdProvider.class);
            final Long fileId = file != null ? Long.parseLong(node.getFileid(file, new DisabledListProgressListener())) : null;
            UserFileKeySetBatchRequest request;
            boolean migrated = false;
            do {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Request a list of missing file keys for file %s", file));
                }
                final MissingKeysResponse missingKeys = new NodesApi(session.getClient()).requestMissingFileKeys(
                    null, null, null, fileId, null, null, null);
                final Map<Long, UserUserPublicKey> publicKeys =
                    missingKeys.getUsers().stream().collect(Collectors.toMap(UserUserPublicKey::getId, Function.identity()));
                final Map<Long, FileFileKeys> files =
                    missingKeys.getFiles().stream().collect(Collectors.toMap(FileFileKeys::getId, Function.identity()));
                request = new UserFileKeySetBatchRequest();
                for(UserIdFileIdItem item : missingKeys.getItems()) {
                    final UserUserPublicKey pubkey = publicKeys.get(item.getUserId());
                    final FileFileKeys fileKeys = files.get(item.getFileId());
                    final UserFileKeySetRequest keySetRequest = new UserFileKeySetRequest()
                        .fileId(item.getFileId())
                        .userId(item.getUserId());
                    processed.add(keySetRequest);
                    EncryptedFileKey encryptFileKey;
                    if(file == null && item.getUserId().equals(((SDSSession) session).userAccount().getId())) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Migrate deprecated file key for %s", file));
                        }
                        final UserKeyPair deprecated = TripleCryptConverter.toCryptoUserKeyPair(((SDSSession) session).keyPairDeprecated());
                        final Credentials credentials = new TripleCryptKeyPair().unlock(callback, session.getHost(), deprecated);
                        encryptFileKey = this.encryptFileKey(deprecated.getUserPrivateKey(), credentials, pubkey, fileKeys);
                        migrated = true;
                    }
                    else {
                        encryptFileKey = this.encryptFileKey(privateKey, passphrase, pubkey, fileKeys);
                    }
                    keySetRequest.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Missing file key for file with id %d processed", item.getFileId()));
                    }
                    request.addItemsItem(keySetRequest);
                }
                if(!request.getItems().isEmpty()) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Set file keys with %s", request));
                    }
                    new NodesApi(session.getClient()).setUserFileKeys(request, StringUtils.EMPTY);
                }
            }
            while(!request.getItems().isEmpty());
            if(migrated) {
                this.deleteDeprecatedKeyPair((SDSSession) session);
            }
            return processed;
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
        }
        catch(CryptoException e) {
            throw new TripleCryptExceptionMappingService().map(e);
        }
    }

    private void deleteDeprecatedKeyPair(SDSSession session) throws ApiException, BackgroundException {
        final MissingKeysResponse missingKeys = new NodesApi(session.getClient()).requestMissingFileKeys(
            null, 1, null, null, session.userAccount().getId(), "previous_user_key", null);
        if(missingKeys.getItems().isEmpty()) {
            log.info("Deleting deprecated key pair");
        }
        new UserApi(session.getClient()).removeUserKeyPair(session.keyPairDeprecated().getPublicKeyContainer().getVersion(), null);
    }

    private EncryptedFileKey encryptFileKey(final UserPrivateKey privateKey, final Credentials passphrase,
                                            final UserUserPublicKey pubkey, final FileFileKeys fileKeys)
        throws InvalidFileKeyException, InvalidKeyPairException, InvalidPasswordException, CryptoSystemException, UnknownVersionException {
        final PlainFileKey plainFileKey = Crypto.decryptFileKey(
            TripleCryptConverter.toCryptoEncryptedFileKey(fileKeys.getFileKeyContainer()), privateKey, passphrase.getPassword());
        return Crypto.encryptFileKey(
            plainFileKey, TripleCryptConverter.toCryptoUserPublicKey(pubkey.getPublicKeyContainer()));
    }
}
