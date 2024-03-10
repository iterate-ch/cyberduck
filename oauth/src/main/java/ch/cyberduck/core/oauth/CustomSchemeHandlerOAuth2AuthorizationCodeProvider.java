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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.urlhandler.SchemeHandler;
import ch.cyberduck.core.urlhandler.SchemeHandlerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class CustomSchemeHandlerOAuth2AuthorizationCodeProvider extends BrowserOAuth2AuthorizationCodeProvider {
    private static final Logger log = LogManager.getLogger(CustomSchemeHandlerOAuth2AuthorizationCodeProvider.class);

    private final SchemeHandler schemeHandler = SchemeHandlerFactory.get();

    @Override
    public String prompt(final Host bookmark, final LoginCallback prompt, final String authorizationCodeUrl, final String redirectUri, final String state) throws BackgroundException {
        this.register(redirectUri);
        this.open(authorizationCodeUrl);
        // Assume scheme handler is registered
        final CountDownLatch signal = new CountDownLatch(1);
        final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
        final AtomicReference<String> authenticationCode = new AtomicReference<>();
        registry.register(state, new OAuth2TokenListener() {
            @Override
            public void callback(final String code) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Callback with code %s", code));
                }
                if(!StringUtils.isBlank(code)) {
                    authenticationCode.set(code);
                }
                signal.countDown();
            }
        });
        if(log.isInfoEnabled()) {
            log.info(String.format("Await callback from custom scheme %s and state %s", redirectUri, state));
        }
        prompt.await(signal, bookmark, String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), BookmarkNameProvider.toString(bookmark, true)),
                LocaleFactory.localizedString("Open web browser to authenticate and obtain an authorization code", "Credentials"));
        bookmark.getCredentials().setSaved(new LoginOptions().save);
        return authenticationCode.get();
    }

    /**
     * Register scheme handler for redirect URI
     */
    private void register(final String redirectUri) {
        final String handler = toScheme(redirectUri);
        if(log.isInfoEnabled()) {
            log.info(String.format("Register OAuth handler %s", handler));
        }
        schemeHandler.setDefaultHandler(new Application(PreferencesFactory.get().getProperty("application.identifier")),
                Collections.singletonList(handler));
    }

    protected static String toScheme(final String redirectUri) {
        return StringUtils.substringBefore(URIEncoder.decode(redirectUri), ':');
    }
}
