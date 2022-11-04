package ch.cyberduck.core.brick;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.BundlesApi;
import ch.cyberduck.core.brick.io.swagger.client.model.BundlesBody;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.PromptUrlProvider;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;

public class BrickShareFeature implements PromptUrlProvider {

    private final BrickSession session;

    public BrickShareFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        return type == Type.download;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Object options, final PasswordCallback callback) throws BackgroundException {
        try {
            final Credentials password = callback.prompt(session.getHost(),
                    LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                    MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                    new LoginOptions().anonymous(true).keychain(false).icon(session.getHost().getProtocol().disk()));
            return new DescriptiveUrl(URI.create(new BundlesApi(new BrickApiClient(session))
                    .postBundles(new BundlesBody().password(password.isPasswordAuthentication() ? password.getPassword() : null).paths(Collections.singletonList(
                            StringUtils.removeStart(file.getAbsolute(), String.valueOf(Path.DELIMITER))))).getUrl()), DescriptiveUrl.Type.signed);
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map(e);
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Object options, final PasswordCallback callback) throws BackgroundException {
        return DescriptiveUrl.EMPTY;
    }
}
