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
import com.dracoon.sdk.crypto.CryptoException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.dracoon.sdk.crypto.model.UserKeyPair;
import com.dracoon.sdk.crypto.model.UserPrivateKey;

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
            final UserPrivateKey privateKey = new UserPrivateKey();
            final UserKeyPairContainer keyPairContainer = ((SDSSession) session).keyPair();
            privateKey.setPrivateKey(keyPairContainer.getPrivateKeyContainer().getPrivateKey());
            privateKey.setVersion(keyPairContainer.getPrivateKeyContainer().getVersion());
            final UserKeyPair userKeyPair = new UserKeyPair();
            userKeyPair.setUserPrivateKey(privateKey);
            final Credentials passphrase = new TripleCryptKeyPair().unlock(callback, session.getHost(), userKeyPair);
            final IdProvider node = session.getFeature(IdProvider.class);
            final Long fileId = file != null ? Long.parseLong(node.getFileid(file, new DisabledListProgressListener())) : null;
            UserFileKeySetBatchRequest request;
            do {
                final MissingKeysResponse missingKeys = new NodesApi(session.getClient()).missingFileKeys(StringUtils.EMPTY,
                    fileId, null, null, null, null);
                final Map<Long, UserUserPublicKey> publicKeys =
                    missingKeys.getUsers().stream().collect(Collectors.toMap(UserUserPublicKey::getId, Function.identity()));
                final Map<Long, FileFileKeys> files =
                    missingKeys.getFiles().stream().collect(Collectors.toMap(FileFileKeys::getId, Function.identity()));
                request = new UserFileKeySetBatchRequest();
                for(UserIdFileIdItem item : missingKeys.getItems()) {
                    final UserUserPublicKey publicKey = publicKeys.get(item.getUserId());
                    final FileFileKeys fileKeys = files.get(item.getFileId());
                    final UserFileKeySetRequest keySetRequest = new UserFileKeySetRequest()
                        .fileId(item.getFileId())
                        .userId(item.getUserId());
                    processed.add(keySetRequest);
                    final PlainFileKey plainFileKey = Crypto.decryptFileKey(
                        TripleCryptConverter.toCryptoEncryptedFileKey(fileKeys.getFileKeyContainer()), privateKey, passphrase.getPassword());
                    final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                        plainFileKey, TripleCryptConverter.toCryptoUserPublicKey(publicKey.getPublicKeyContainer())
                    );
                    keySetRequest.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Missing file key for file with id %d processed", item.getFileId()));
                    }
                    request.addItemsItem(keySetRequest);
                }
                if(!request.getItems().isEmpty()) {
                    new NodesApi(session.getClient()).setUserFileKeys(request, StringUtils.EMPTY);
                }
            }
            while(!request.getItems().isEmpty());
            return processed;
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
        }
        catch(CryptoException e) {
            throw new TripleCryptExceptionMappingService().map(e);
        }
    }
}
