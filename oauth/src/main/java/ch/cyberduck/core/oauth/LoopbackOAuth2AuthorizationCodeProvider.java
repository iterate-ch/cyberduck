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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
                    URI.create(redirectUri).getHost(), URI.create(redirectUri).getPort()), 0);
            // Create handler for OAuth callback
            server.createContext(StringUtils.isBlank(URI.create(redirectUri).getRawPath()) ?
                    String.valueOf(Path.DELIMITER) : URI.create(redirectUri).getRawPath(), new HttpHandler() {
                @Override
                public void handle(final HttpExchange exchange) throws IOException {
                    log.debug("Received callback with query {}", exchange.getRequestURI().getQuery());
                    final List<NameValuePair> pairs = URLEncodedUtils.parse(exchange.getRequestURI(), Charset.defaultCharset());
                    String state = StringUtils.EMPTY;
                    String code = StringUtils.EMPTY;
                    for(NameValuePair pair : pairs) {
                        if(StringUtils.equals(pair.getName(), "state")) {
                            state = StringUtils.equals(pair.getName(), "state") ? pair.getValue() : StringUtils.EMPTY;
                        }
                        if(StringUtils.equals(pair.getName(), "code")) {
                            code = StringUtils.equals(pair.getName(), "code") ? pair.getValue() : StringUtils.EMPTY;
                        }
                    }
                    final OAuth2TokenListenerRegistry oauth = OAuth2TokenListenerRegistry.get();
                    if(oauth.notify(state, code)) {
                        final String response = "<!DOCTYPE html><html><body><script>window.close();</script></body></html>";
                        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                        try(final OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    else {
                        exchange.sendResponseHeaders(400, 0);
                        IOUtils.close(exchange.getResponseBody());
                    }
                }
            });
            server.setExecutor(Executors.newSingleThreadExecutor(new NamedThreadFactory("oauth")));
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
                server.stop(0);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}