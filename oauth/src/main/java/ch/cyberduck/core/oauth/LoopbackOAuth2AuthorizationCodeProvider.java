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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class LoopbackOAuth2AuthorizationCodeProvider extends BrowserOAuth2AuthorizationCodeProvider {
    private static final Logger log = LogManager.getLogger(LoopbackOAuth2AuthorizationCodeProvider.class);

    @Override
    public String prompt(final Host bookmark, final LoginCallback prompt, final String authorizationCodeUrl, final String redirectUri, final String state) throws BackgroundException {
        final CountDownLatch signal = new CountDownLatch(1);
        final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
        final AtomicReference<String> authenticationCode = new AtomicReference<>();
        registry.register(state, new OAuth2TokenListener() {
            @Override
            public void callback(final String code) {
                log.info("Callback with code {}", code);
                if(!StringUtils.isBlank(code)) {
                    authenticationCode.set(code);
                }
                signal.countDown();
            }
        });
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(
                    URI.create(redirectUri).getHost(), -1 == URI.create(redirectUri).getPort() ? 0 : URI.create(redirectUri).getPort()), 0);
            final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("oauth"));
            // Create handler for OAuth callback
            server.createContext(StringUtils.isBlank(URI.create(redirectUri).getRawPath()) ?
                    String.valueOf(Path.DELIMITER) : URI.create(redirectUri).getRawPath(), new HttpHandler() {
                @Override
                public void handle(final HttpExchange exchange) throws IOException {
                    log.debug("Received callback with query {}", exchange.getRequestURI().getQuery());
                    final List<NameValuePair> pairs = URLEncodedUtils.parse(exchange.getRequestURI(), Charset.defaultCharset());
                    String callbackState = StringUtils.EMPTY;
                    String code = StringUtils.EMPTY;
                    for(NameValuePair pair : pairs) {
                        if(StringUtils.equals(pair.getName(), "state")) {
                            callbackState = pair.getValue();
                        }
                        if(StringUtils.equals(pair.getName(), "code")) {
                            code = StringUtils.equals(pair.getName(), "code") ? pair.getValue() : StringUtils.EMPTY;
                        }
                    }
                    final OAuth2TokenListenerRegistry oauth = OAuth2TokenListenerRegistry.get();
                    final boolean recognized = StringUtils.equals(state, callbackState) && oauth.notify(callbackState, code);
                    final String successUrl = HostPreferencesFactory.get(bookmark).getProperty("oauth.browser.success.url");
                    exchange.getResponseHeaders().set(HttpHeaders.CACHE_CONTROL, "no-store");
                    exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
                    if(recognized) {
                        // Keep the authorization code and state on the loopback callback.
                        exchange.getResponseHeaders().set(HttpHeaders.LOCATION, StringUtils.isNotBlank(code) && StringUtils.isNotBlank(successUrl) ?
                                URI.create(authorizationCodeUrl).resolve(successUrl).toString() : OAuth2AuthorizationService.CYBERDUCK_REDIRECT_URI);
                        exchange.sendResponseHeaders(302, -1);
                    }
                    else {
                        exchange.sendResponseHeaders(400, -1);
                    }
                    exchange.close();
                }
            });
            server.setExecutor(executor);
            server.start();
            log.info("Started OAuth callback server {}", server);
            try {
                // Open browser with authorization URL
                this.open(authorizationCodeUrl);
                // Wait for callback
                log.info("Await callback from custom scheme {} and state {}", redirectUri, state);
                prompt.await(signal, bookmark, String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), BookmarkNameProvider.toString(bookmark, true)),
                        LocaleFactory.localizedString("Open web browser to authenticate and obtain an authorization code", "Credentials"));
                bookmark.getCredentials().setSaved(new LoginOptions().save);
                return authenticationCode.get();
            }
            finally {
                // Finish serving the callback page before closing the loopback listener.
                server.stop(1);
                executor.shutdown();
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
