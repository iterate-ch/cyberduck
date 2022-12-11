package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.CreateShareApi;
import ch.cyberduck.core.eue.io.swagger.client.api.UserInfoApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationRequestEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationRequestModel;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseEntity;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseModel;
import ch.cyberduck.core.eue.io.swagger.client.model.SharePermission;
import ch.cyberduck.core.eue.io.swagger.client.model.Shares;
import ch.cyberduck.core.eue.io.swagger.client.model.UserInfoResponseModel;
import ch.cyberduck.core.eue.io.swagger.client.model.UserSharesModel;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class EueShareFeature implements PromptUrlProvider<ShareCreationRequestModel, ShareCreationRequestModel> {
    private static final Logger log = LogManager.getLogger(EueShareFeature.class);

    private static final String GUEST_E_MAIL = "!ano";

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueShareFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        if(StringUtils.equals(EueResourceIdProvider.TRASH, file.attributes().getFileId())) {
            return false;
        }
        if(type == Type.upload) {
            return file.isDirectory();
        }
        return DescriptiveUrl.EMPTY == file.attributes().getLink();
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final ShareCreationRequestModel options, final PasswordCallback callback) throws BackgroundException {
        return this.toGuestUrl(file, options, callback);
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final ShareCreationRequestModel options, final PasswordCallback callback) throws BackgroundException {
        // Look for existing share
        return this.toGuestUrl(file, options, callback);
    }

    private DescriptiveUrl toGuestUrl(final Path file, final ShareCreationRequestModel options, final PasswordCallback callback) throws BackgroundException {
        // Look for existing share
        final ShareCreationResponseEntity shareForResource = findShareForResource(session.userShares(), fileid.getFileId(file));
        if(null != shareForResource) {
            return EueShareUrlProvider.toUrl(session.getHost(), shareForResource);
        }
        return EueShareUrlProvider.toUrl(session.getHost(), this.createGuestUri(file, callback, options));
    }

    private ShareCreationResponseEntity createGuestUri(final Path file, final PasswordCallback callback, final ShareCreationRequestModel shareCreationRequestModel) throws BackgroundException {
        final EueApiClient client = new EueApiClient(session);
        final CreateShareApi createShareApi = new CreateShareApi(client);
        final String resourceId = fileid.getFileId(file);
        try {
            final ShareCreationRequestModel shareCreationRequestEntries = null != shareCreationRequestModel ? shareCreationRequestModel : this.createShareCreationRequestModel(file, callback);
            final ShareCreationResponseModel shareCreationResponseModel = createShareApi.resourceResourceIdSharePost(resourceId, shareCreationRequestEntries, null, null);
            if(!shareCreationResponseModel.containsKey(GUEST_E_MAIL)) {
                throw new NotfoundException(GUEST_E_MAIL);
            }
            final ShareCreationResponseEntry shareCreationResponseEntry = shareCreationResponseModel.get(GUEST_E_MAIL);
            switch(shareCreationResponseEntry.getStatusCode()) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_CREATED:
                    shareCreationResponseEntry.getEntity().setResourceURI(resourceId);
                    session.userShares().add(shareCreationResponseEntry.getEntity());
                    return shareCreationResponseEntry.getEntity();
                default:
                    log.warn(String.format("Failure %s creating share for %s", shareCreationResponseEntry, file));
                    if(null == shareCreationResponseEntry.getEntity()) {
                        throw new EueExceptionMappingService().map(new ApiException(shareCreationResponseEntry.getReason(),
                                null, shareCreationResponseEntry.getStatusCode(), client.getResponseHeaders()));
                    }
                    throw new EueExceptionMappingService().map(new ApiException(shareCreationResponseEntry.getEntity().getError(),
                            null, shareCreationResponseEntry.getStatusCode(), client.getResponseHeaders()));
            }
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map(e);
        }
    }

    protected ShareCreationRequestModel createShareCreationRequestModel(final Path file, final PasswordCallback callback) throws ApiException, LoginCanceledException {
        final Host bookmark = session.getHost();
        final ShareCreationRequestEntry shareCreationRequestEntry = new ShareCreationRequestEntry()
                .name(new AlphanumericRandomStringService().random())
                .hasPin(false);
        if(new HostPreferences(session.getHost()).getBoolean("eue.share.writable")) {
            final Shares.WritableSharesMinimumProtectionEnum writableSharesMinimumProtection = this.getWritableSharesMinimumProtection();
            switch(writableSharesMinimumProtection) {
                case PIN_AND_EXPIRATION:
                case EXPIRATION:
                    final long expirationInMillis = new HostPreferences(session.getHost()).getLong("eue.share.expiration.millis");
                    shareCreationRequestEntry.setExpirationMillis(expirationInMillis);
                case PIN:
                    final String password = callback.prompt(bookmark,
                            LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                            MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                            new LoginOptions().keychain(false).icon(bookmark.getProtocol().disk())).getPassword();
                    shareCreationRequestEntry.setHasPin(true);
                    shareCreationRequestEntry.setPin(password);
            }
        }
        shareCreationRequestEntry.setGuestEMail(GUEST_E_MAIL);
        final SharePermission sharePermission = new SharePermission();
        sharePermission
                .readable(new HostPreferences(session.getHost()).getBoolean("eue.share.readable"))
                .writable(new HostPreferences(session.getHost()).getBoolean("eue.share.writable"))
                .deletable(new HostPreferences(session.getHost()).getBoolean("eue.share.deletable"));
        sharePermission.setNotificationEnabled(new HostPreferences(session.getHost()).getBoolean("eue.share.notification.enable"));
        shareCreationRequestEntry.setPermission(sharePermission);
        shareCreationRequestEntry.setUnmountable(false);
        final ShareCreationRequestModel shareCreationRequestModel = new ShareCreationRequestModel();
        shareCreationRequestModel.add(shareCreationRequestEntry);
        return shareCreationRequestModel;
    }

    protected static ShareCreationResponseEntity findShareForResource(final UserSharesModel sharesModel, final String resourceId) {
        return sharesModel.stream().filter(sm -> EueResourceIdProvider.getResourceIdFromResourceUri(sm.getResourceURI()).equals(resourceId)).findFirst().orElse(null);
    }

    private Shares.WritableSharesMinimumProtectionEnum getWritableSharesMinimumProtection() throws ApiException {
        final UserInfoResponseModel userInfoResponseModel = new UserInfoApi(new EueApiClient(session)).userinfoGet(null, null);
        return userInfoResponseModel.getSettings().getShares().getWritableSharesMinimumProtection();
    }

    protected static String toBrandedUri(final String guestUri, final String hostname) {
        final String user = StringUtils.substringBefore(StringUtils.substringAfter(StringUtils.substringAfter(guestUri, "guest"), Path.DELIMITER), Path.DELIMITER);
        final String share = StringUtils.substringBefore(StringUtils.substringAfter(StringUtils.substringAfter(guestUri, "share"), Path.DELIMITER), Path.DELIMITER);
        return String.format("https://%s/%s/%s", hostname, user, share);
    }
}
