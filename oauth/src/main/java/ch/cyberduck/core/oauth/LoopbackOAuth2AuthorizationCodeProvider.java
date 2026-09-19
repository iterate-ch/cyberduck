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
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.sun.net.httpserver.HttpServer;

public class LoopbackOAuth2AuthorizationCodeProvider extends BrowserOAuth2AuthorizationCodeProvider {
    private static final Logger log = LogManager.getLogger(LoopbackOAuth2AuthorizationCodeProvider.class);

    @Override
    public String prompt(final Host bookmark, final LoginCallback prompt, final String authorizationCodeUrl, final String redirectUri, final String state) throws BackgroundException {
        return this.prompt(bookmark, prompt, ignored -> authorizationCodeUrl, redirectUri, state);
    }

    public String prompt(final Host bookmark, final LoginCallback prompt,
                         final Function<String, String> authorizationCodeUrl, final String state) throws BackgroundException {
        return this.prompt(bookmark, prompt, authorizationCodeUrl, null, state);
    }

    private String prompt(final Host bookmark, final LoginCallback prompt,
                          final Function<String, String> authorizationCodeUrl,
                          final String requestedRedirectUri, final String expectedState) throws BackgroundException {
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicReference<String> authenticationCode = new AtomicReference<>();
        OAuth2TokenListenerRegistry.get().register(expectedState, code -> {
            if(StringUtils.isBlank(code)) {
                signal.countDown();
            }
            else {
                authenticationCode.set(code);
            }
        });
        try {
            final URI requested = null == requestedRedirectUri ? null : URI.create(requestedRedirectUri);
            final HttpServer server = HttpServer.create(null == requested ?
                    new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 0) :
                    new InetSocketAddress(requested.getHost(), -1 == requested.getPort() ? 0 : requested.getPort()), 0);
            final String redirectUri = null == requested ? String.format("http://127.0.0.1:%d/oauth/callback", server.getAddress().getPort()) : requestedRedirectUri;
            final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("oauth"));
            // Create handler for OAuth callback
            server.createContext(StringUtils.isBlank(URI.create(redirectUri).getRawPath()) ?
                    String.valueOf(Path.DELIMITER) : URI.create(redirectUri).getRawPath(), exchange -> {
                    final List<NameValuePair> pairs = URLEncodedUtils.parse(exchange.getRequestURI(), StandardCharsets.UTF_8);
                    String state = StringUtils.EMPTY;
                    String code = StringUtils.EMPTY;
                    for(NameValuePair pair : pairs) {
                        if(StringUtils.equals(pair.getName(), "state")) {
                            state = pair.getValue();
                        }
                        if(StringUtils.equals(pair.getName(), "code")) {
                            code = pair.getValue();
                        }
                    }
                    final boolean accepted = StringUtils.equals(expectedState, state) && OAuth2TokenListenerRegistry.get().notify(state, code);
                    try {
                        if(!accepted) {
                            exchange.sendResponseHeaders(400, 0);
                        }
                        else if(null == requested) {
                            final byte[] response = LocaleFactory.localizedString("Login successful", "Credentials").getBytes(StandardCharsets.UTF_8);
                            exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8");
                            exchange.sendResponseHeaders(200, response.length);
                            exchange.getResponseBody().write(response);
                        }
                        else {
                            exchange.getResponseHeaders().add(HttpHeaders.LOCATION, OAuth2AuthorizationService.CYBERDUCK_REDIRECT_URI);
                            exchange.sendResponseHeaders(302, 0L);
                        }
                    }
                    finally {
                        IOUtils.close(exchange.getResponseBody());
                        if(accepted) {
                            signal.countDown();
                        }
                    }
            });
            server.setExecutor(executor);
            server.start();
            log.info("Started OAuth callback server {}", server);
            try {
                // Open browser with authorization URL
                this.open(authorizationCodeUrl.apply(redirectUri));
                // Wait for callback
                log.info("Await callback from custom scheme {} and state {}", redirectUri, expectedState);
                prompt.await(signal, bookmark, String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), BookmarkNameProvider.toString(bookmark, true)),
                        LocaleFactory.localizedString("Open web browser to authenticate and obtain an authorization code", "Credentials"));
                bookmark.getCredentials().setSaved(new LoginOptions().save);
                return authenticationCode.get();
            }
            finally {
                server.stop(0);
                executor.shutdown();
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
