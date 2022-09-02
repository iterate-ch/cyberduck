package ch.cyberduck.core.oauth;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.BackgroundException;

public class PromptOAuth2AuthorizationCodeProvider extends BrowserOAuth2AuthorizationCodeProvider {

    @Override
    public String prompt(final Host bookmark, final LoginCallback prompt, final String authorizationCodeUrl, final String redirectUri, final String state) throws BackgroundException {
        this.open(authorizationCodeUrl);
        final Credentials input = prompt.prompt(bookmark,
                String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), BookmarkNameProvider.toString(bookmark, true)),
                LocaleFactory.localizedString("Paste the authentication code from your web browser", "Credentials"),
                new LoginOptions(bookmark.getProtocol()).keychain(true).user(false).oauth(true)
                        .passwordPlaceholder(LocaleFactory.localizedString("Authentication Code", "Credentials"))
        );
        bookmark.getCredentials().setSaved(input.isSaved());
        return input.getPassword();
    }
}
