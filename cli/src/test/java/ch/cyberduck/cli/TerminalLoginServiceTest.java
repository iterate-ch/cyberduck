package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.brick.BrickProtocol;
import ch.cyberduck.core.brick.BrickSession;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProviderFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

public class TerminalLoginServiceTest {
    @Test
    public void testManualApiKeyDoesNotRequirePasswordField() throws Exception {
        final CommandLine input = new DefaultParser().parse(TerminalOptionsBuilder.options(),
                new String[]{"--username", "jane", "--password", "manual-api-key", "--list", "brick://customer.files.com/"});
        final Host host = new Host(this.protocol(), "customer.files.com");
        final LoginOptions options = new LoginOptions(host.getProtocol());
        assertFalse(options.password);
        new TerminalLoginService(input).validate(host, new DefaultX509KeyManager(), LoginCallback.noop, options);
        assertEquals("manual-api-key", host.getCredentials().getPassword());
        assertEquals("jane", host.getCredentials().getUsername());
    }

    @Test
    public void testRejectedExplicitKeyWithAssumeYesFailsWithoutRetryOrBrowser() throws Exception {
        final CommandLine input = new DefaultParser().parse(TerminalOptionsBuilder.options(),
                new String[]{"-y", "--username", "jane", "--password", "rejected-key", "--list", "brick://127.0.0.1/"});
        assertTrue(input.hasOption(TerminalOptionsBuilder.Params.assumeyes.name()));
        final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final AtomicInteger requests = new AtomicInteger();
        final AtomicReference<String> sentKey = new AtomicReference<>();
        server.createContext("/api/rest/v1/files", exchange -> {
            requests.incrementAndGet();
            sentKey.set(exchange.getRequestHeaders().getFirst("X-FilesAPI-Key"));
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        server.start();
        final Profile protocol = mock(Profile.class, delegatesTo(this.protocol()));
        doReturn(Scheme.http).when(protocol).getScheme();
        final Host host = new Host(protocol, "127.0.0.1", server.getAddress().getPort());
        final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final TerminalPromptReader reader = spy(new DisabledTerminalPromptReader());
        final LoginCallback prompt = new TerminalLoginCallback(reader);
        final OAuth2AuthorizationCodeProvider provider = mock(OAuth2AuthorizationCodeProvider.class);
        try(MockedStatic<PasswordStoreFactory> passwords = mockStatic(PasswordStoreFactory.class, withSettings().mockMaker("mock-maker-inline"));
            MockedStatic<OAuth2AuthorizationCodeProviderFactory> codes = mockStatic(OAuth2AuthorizationCodeProviderFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
            passwords.when(PasswordStoreFactory::get).thenReturn(mock(HostPasswordStore.class));
            codes.when(OAuth2AuthorizationCodeProviderFactory::get).thenReturn(provider);
            final TerminalLoginService login = new TerminalLoginService(input);
            login.validate(host, new DefaultX509KeyManager(), prompt, new LoginOptions(protocol));
            session.open(target -> Proxy.DIRECT, HostKeyCallback.noop, prompt, CancelCallback.noop);
            final AccessDeniedException failure = assertThrows(AccessDeniedException.class,
                    () -> login.authenticate(session, ProgressListener.noop, prompt, CancelCallback.noop));
            assertTrue(failure.getCause() instanceof LoginFailureException);
            assertEquals(1, requests.get());
            assertEquals("rejected-key", sentKey.get());
            verifyNoInteractions(reader, provider);
        }
        finally {
            session.close();
            server.stop(0);
        }
    }

    @Test
    public void testNoKeychainSkipsSavedCredentials() throws Exception {
        final CommandLine input = new DefaultParser().parse(TerminalOptionsBuilder.options(),
                new String[]{"--nokeychain", "--list", "brick://customer.files.com/"});
        final TerminalPreferences preferences = mock(TerminalPreferences.class, delegatesTo(new TerminalPreferences(PreferencesFactory.get())));
        doReturn(preferences).when(preferences).withDefaults(input);
        doNothing().when(preferences).setDefault(anyString(), anyString());
        doAnswer(invocation -> {
            doReturn(Boolean.parseBoolean(invocation.getArgument(1))).when(preferences).getBoolean("connection.login.keychain");
            return null;
        }).when(preferences).setDefault(eq("connection.login.keychain"), anyString());
        final HostPasswordStore store = mock(HostPasswordStore.class);
        try(MockedStatic<PreferencesFactory> factory = mockStatic(PreferencesFactory.class, withSettings().mockMaker("mock-maker-inline"));
            MockedStatic<PasswordStoreFactory> passwords = mockStatic(PasswordStoreFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
            factory.when(PreferencesFactory::get).thenReturn(preferences);
            passwords.when(PasswordStoreFactory::get).thenReturn(store);
            new Terminal(preferences, TerminalOptionsBuilder.options(), input).configure(input);
            final Host host = new Host(this.protocol(), "customer.files.com");
            final LoginOptions options = new LoginOptions(host.getProtocol());
            assertFalse(options.keychain);
            new TerminalLoginService(input).validate(host, new DefaultX509KeyManager(), LoginCallback.noop, options);
            verifyNoInteractions(store);
        }
    }

    private Profile protocol() throws Exception {
        return new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new BrickProtocol())))
                .read(this.getClass().getResourceAsStream("/Files.com.cyberduckprofile"));
    }
}
