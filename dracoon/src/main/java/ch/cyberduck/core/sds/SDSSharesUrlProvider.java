package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.SharesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateUploadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.triplecrypt.CryptoExceptionMappingService;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptKeyPair;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Set;

import eu.ssp_europe.sds.crypto.Crypto;
import eu.ssp_europe.sds.crypto.CryptoException;
import eu.ssp_europe.sds.crypto.model.EncryptedFileKey;
import eu.ssp_europe.sds.crypto.model.PlainFileKey;
import eu.ssp_europe.sds.crypto.model.UserKeyPair;
import eu.ssp_europe.sds.crypto.model.UserPrivateKey;

public class SDSSharesUrlProvider implements PromptUrlProvider<CreateDownloadShareRequest, CreateUploadShareRequest> {
    private static final Logger log = Logger.getLogger(SDSSharesUrlProvider.class);

    private final PathContainerService containerService
        = new SDSPathContainerService();

    private final SDSSession session;

    public SDSSharesUrlProvider(final SDSSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final CreateDownloadShareRequest options,
                                        final PasswordCallback callback) throws BackgroundException {
        try {
            final Set<Acl.Role> roles = new SDSPermissionsFeature(session).getPermission(containerService.getContainer(file)).get(new Acl.CanonicalUser(String.valueOf(session.userAccount().getId())));
            if(roles != null && !roles.contains(SDSPermissionsFeature.DOWNLOAD_SHARE_ROLE)) {
                return DescriptiveUrl.EMPTY;
            }
            final Long fileid = Long.parseLong(new SDSNodeIdProvider(session).getFileid(file, new DisabledListProgressListener()));
            if(containerService.getContainer(file).getType().contains(Path.Type.vault)) {
                // get existing file key associated with the sharing user
                final FileKey key = new NodesApi(session.getClient()).getUserFileKey(StringUtils.EMPTY, fileid);
                final UserPrivateKey privateKey = new UserPrivateKey();
                final UserKeyPairContainer keyPairContainer = session.keyPair();
                privateKey.setPrivateKey(keyPairContainer.getPrivateKeyContainer().getPrivateKey());
                privateKey.setVersion(keyPairContainer.getPrivateKeyContainer().getVersion());
                final UserKeyPair userKeyPair = new UserKeyPair();
                userKeyPair.setUserPrivateKey(privateKey);
                final Credentials passphrase = new TripleCryptKeyPair().unlock(callback, session.getHost(), userKeyPair);
                final PlainFileKey plainFileKey = Crypto.decryptFileKey(TripleCryptConverter.toCryptoEncryptedFileKey(key), privateKey, passphrase.getPassword());
                // encrypt file key with a new key pair
                final UserKeyPair pair = Crypto.generateUserKeyPair(options.getPassword());
                final EncryptedFileKey encryptedFileKey = Crypto.encryptFileKey(plainFileKey, pair.getUserPublicKey());
                options.setPassword(null);
                options.setKeyPair(TripleCryptConverter.toSwaggerUserKeyPairContainer(pair));
                options.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptedFileKey));
            }
            final DownloadShare share = new SharesApi(session.getClient()).createDownloadShare(StringUtils.EMPTY,
                options.nodeId(fileid), null);
            final String help;
            if(null == share.getExpireAt()) {
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"));
            }
            else {
                final Long expiry = share.getExpireAt().getTime();
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3")) + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                    UserDateFormatterFactory.get().getShortFormat(expiry * 1000)
                );
            }
            return new DescriptiveUrl(
                URI.create(String.format("%s://%s/#/public/shares-downloads/%s",
                    session.getHost().getProtocol().getScheme(),
                    session.getHost().getHostname(),
                    share.getAccessKey())
                ),
                DescriptiveUrl.Type.signed, help);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
        }
        catch(CryptoException e) {
            throw new CryptoExceptionMappingService().map(e);
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final CreateUploadShareRequest options, final PasswordCallback callback) throws BackgroundException {
        try {
            final Set<Acl.Role> roles = new SDSPermissionsFeature(session).getPermission(containerService.getContainer(file)).get(new Acl.CanonicalUser(String.valueOf(session.userAccount().getId())));
            if(roles != null && !roles.contains(SDSPermissionsFeature.UPLOAD_SHARE_ROLE)) {
                return DescriptiveUrl.EMPTY;
            }
            final UploadShare share = new SharesApi(session.getClient()).createUploadShare(StringUtils.EMPTY,
                options.targetId(Long.parseLong(new SDSNodeIdProvider(session).getFileid(file, new DisabledListProgressListener()))), null);
            final String help;
            if(null == share.getExpireAt()) {
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"));
            }
            else {
                final Long expiry = share.getExpireAt().getTime();
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3")) + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                    UserDateFormatterFactory.get().getShortFormat(expiry * 1000)
                );
            }
            return new DescriptiveUrl(
                URI.create(String.format("%s://%s/#/public/shares-uploads/%s",
                    session.getHost().getProtocol().getScheme(),
                    session.getHost().getHostname(),
                    share.getAccessKey())
                ),
                DescriptiveUrl.Type.signed, help);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
        }
    }
}
