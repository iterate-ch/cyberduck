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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.brick.BrickProtocol;
import ch.cyberduck.core.brick.BrickSession;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

public class TerminalLoginServiceTest {
    @Test
    public void testManualApiKeyDoesNotRequirePasswordField() throws Exception {
        final CommandLine input = new DefaultParser().parse(TerminalOptionsBuilder.options(),
                new String[]{"--username", "jane", "--password", "manual-api-key", "--list", "brick://customer.files.com/"});
        final Host host = new Host(new BrickProtocol(), "customer.files.com");
        final LoginOptions options = new LoginOptions(host.getProtocol());
        assertFalse(options.password);
        new TerminalLoginService(input).validate(host, new DefaultX509KeyManager(), LoginCallback.noop, options);
        assertEquals("manual-api-key", host.getCredentials().getPassword());
        assertEquals("jane", host.getCredentials().getUsername());
    }

    @Test
    public void testNoKeychainSkipsSavedKeyAndAuthorizesInBrowser() throws Exception {
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
            final Host host = new Host(new BrickProtocol(), "customer.files.com");
            final LoginOptions options = new LoginOptions(host.getProtocol());
            assertFalse(options.keychain);
            new TerminalLoginService(input).validate(host, new DefaultX509KeyManager(), LoginCallback.noop, options);
            final AtomicInteger authorizations = new AtomicInteger();
            final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
                @Override
                public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) {
                    authorizations.incrementAndGet();
                    return host.getCredentials().setPassword("browser-issued-key");
                }
            };
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertEquals(1, authorizations.get());
            assertEquals("browser-issued-key", host.getCredentials().getPassword());
            verifyNoInteractions(store);
        }
    }
}
