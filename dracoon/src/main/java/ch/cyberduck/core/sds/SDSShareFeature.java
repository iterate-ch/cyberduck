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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.Version;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.SharesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateUploadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptKeyPair;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.dracoon.sdk.crypto.model.UserKeyPair;

public class SDSShareFeature implements Share<CreateDownloadShareRequest, CreateUploadShareRequest> {
    private static final Logger log = LogManager.getLogger(SDSShareFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSShareFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        if(file.isRoot()) {
            return false;
        }
        switch(type) {
            case download: {
                if(file.isDirectory()) {
                    if(SDSAttributesAdapter.isEncrypted(file.attributes())) {
                        log.warn(String.format("Not supported for file %s in encrypted room", file));
                        // In encrypted rooms only files can be shared
                        return false;
                    }
                }
                final Acl.Role role = SDSPermissionsFeature.DOWNLOAD_SHARE_ROLE;
                final boolean found = new SDSPermissionsFeature(session, nodeid).containsRole(file, role);
                if(!found) {
                    log.warn(String.format("Not supported for file %s with missing role %s", file, role));
                }
                return found;
            }
            case upload: {
                // An upload account can be created for directories and rooms only
                if(!file.isDirectory()) {
                    log.warn(String.format("Not supported for file %s", file));
                    return false;
                }
                final Acl.Role role = SDSPermissionsFeature.UPLOAD_SHARE_ROLE;
                final boolean found = new SDSPermissionsFeature(session, nodeid).containsRole(file, role);
                if(!found) {
                    log.warn(String.format("Not supported for file %s with missing role %s", file, role));
                }
                return found;
            }
        }
        return false;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, CreateDownloadShareRequest options, final PasswordCallback callback) throws BackgroundException {
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Create download share for %s", file));
            }
            if(null == options) {
                options = new CreateDownloadShareRequest();
                log.warn(String.format("Use default share options %s", options));
            }
            final Long fileid = Long.parseLong(nodeid.getVersionId(file));
            final Host bookmark = session.getHost();
            if(new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(file)) {
                // get existing file key associated with the sharing user
                final FileKey key = new NodesApi(session.getClient()).requestUserFileKey(fileid, null, null);
                final EncryptedFileKey encFileKey = TripleCryptConverter.toCryptoEncryptedFileKey(key);
                final UserKeyPairContainer keyPairContainer = session.getKeyPairForFileKey(encFileKey.getVersion());
                final UserKeyPair userKeyPair = TripleCryptConverter.toCryptoUserKeyPair(keyPairContainer);
                final Credentials passphrase = new TripleCryptKeyPair().unlock(callback, bookmark, userKeyPair);

                final PlainFileKey plainFileKey = Crypto.decryptFileKey(encFileKey, userKeyPair.getUserPrivateKey(), passphrase.getPassword().toCharArray());
                // encrypt file key with a new key pair
                final UserKeyPair pair;
                if(null == options.getPassword()) {
                    pair = Crypto.generateUserKeyPair(session.requiredKeyPairVersion(), callback.prompt(
                            bookmark, LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                            LocaleFactory.localizedString("Provide additional login credentials", "Credentials"), new LoginOptions().icon(session.getHost().getProtocol().disk())
                    ).getPassword().toCharArray());
                }
                else {
                    pair = Crypto.generateUserKeyPair(session.requiredKeyPairVersion(), options.getPassword().toCharArray());
                }
                final EncryptedFileKey encryptedFileKey = Crypto.encryptFileKey(plainFileKey, pair.getUserPublicKey());
                options.setPassword(null);
                options.setKeyPair(TripleCryptConverter.toSwaggerUserKeyPairContainer(pair));
                options.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptedFileKey));
            }
            final DownloadShare share = new SharesApi(session.getClient()).createDownloadShare(
                    options.nodeId(fileid), StringUtils.EMPTY, null);
            final String help;
            if(null == share.getExpireAt()) {
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"));
            }
            else {
                final long expiry = share.getExpireAt().getMillis();
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3")) + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getShortFormat(expiry * 1000)
                );
            }
            final Matcher matcher = Pattern.compile(SDSSession.VERSION_REGEX).matcher(session.softwareVersion().getRestApiVersion());
            if(matcher.matches()) {
                if(new Version(matcher.group(1)).compareTo(new Version("4.26")) < 0) {
                    return new DescriptiveUrl(URI.create(String.format("%s://%s/#/public/shares-downloads/%s",
                            bookmark.getProtocol().getScheme(),
                            bookmark.getHostname(),
                            share.getAccessKey())),
                            DescriptiveUrl.Type.signed, help);
                }
            }
            return new DescriptiveUrl(URI.create(String.format("%s://%s/public/download-shares/%s",
                    bookmark.getProtocol().getScheme(),
                    bookmark.getHostname(),
                    share.getAccessKey())),
                    DescriptiveUrl.Type.signed, help);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map(e);
        }
        catch(CryptoException e) {
            throw new TripleCryptExceptionMappingService().map(e);
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, CreateUploadShareRequest options, final PasswordCallback callback) throws BackgroundException {
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Create upload share for %s", file));
            }
            if(null == options) {
                options = new CreateUploadShareRequest();
                log.warn(String.format("Use default share options %s", options));
            }
            final Host bookmark = session.getHost();
            final UploadShare share = new SharesApi(session.getClient()).createUploadShare(
                    options.targetId(Long.parseLong(nodeid.getVersionId(file))), StringUtils.EMPTY, null);
            final String help;
            if(null == share.getExpireAt()) {
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"));
            }
            else {
                final long expiry = share.getExpireAt().getMillis();
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3")) + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getShortFormat(expiry * 1000)
                );
            }
            final Matcher matcher = Pattern.compile(SDSSession.VERSION_REGEX).matcher(session.softwareVersion().getRestApiVersion());
            if(matcher.matches()) {
                if(new Version(matcher.group(1)).compareTo(new Version("4.26")) < 0) {
                    return new DescriptiveUrl(URI.create(String.format("%s://%s/#/public/shares-uploads/%s",
                            bookmark.getProtocol().getScheme(),
                            bookmark.getHostname(),
                            share.getAccessKey())),
                            DescriptiveUrl.Type.signed, help);
                }
            }
            return new DescriptiveUrl(URI.create(String.format("%s://%s/public/upload-shares/%s",
                    bookmark.getProtocol().getScheme(),
                    bookmark.getHostname(),
                    share.getAccessKey())),
                    DescriptiveUrl.Type.signed, help);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map(e);
        }
    }
}
