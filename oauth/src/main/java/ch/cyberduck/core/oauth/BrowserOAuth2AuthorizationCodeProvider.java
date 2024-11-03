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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.local.BrowserLauncher;
import ch.cyberduck.core.local.BrowserLauncherFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrowserOAuth2AuthorizationCodeProvider implements OAuth2AuthorizationCodeProvider {
    private static final Logger log = LogManager.getLogger(BrowserOAuth2AuthorizationCodeProvider.class);

    public final BrowserLauncher browser = BrowserLauncherFactory.get();

    protected void open(final String authorizationCodeRequestUrl) throws LoginCanceledException {
        if(!browser.open(authorizationCodeRequestUrl)) {
            throw new LoginCanceledException(new LocalAccessDeniedException(String.format("Failed to launch web browser for %s", authorizationCodeRequestUrl)));
        }
    }

    @Override
    public String prompt(final Host bookmark, final LoginCallback prompt, final String authorizationCodeUrl, final String redirectUri, final String state) throws BackgroundException {
        log.debug("Evaluate redirect URI {}", redirectUri);
        if(StringUtils.endsWith(URIEncoder.decode(redirectUri), ":oauth")) {
            return new CustomSchemeHandlerOAuth2AuthorizationCodeProvider().prompt(
                    bookmark, prompt, authorizationCodeUrl, redirectUri, state);
        }
        if(StringUtils.contains(redirectUri, "://oauth")) {
            return new CustomSchemeHandlerOAuth2AuthorizationCodeProvider().prompt(
                    bookmark, prompt, authorizationCodeUrl, redirectUri, state);
        }
        log.debug("Prompt for authentication code for state {}", state);
        return new PromptOAuth2AuthorizationCodeProvider().prompt(
                bookmark, prompt, authorizationCodeUrl, redirectUri, state);
    }
}
