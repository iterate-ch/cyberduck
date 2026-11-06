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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.UnsecureHostPasswordStore;
import ch.cyberduck.core.brick.BrickOAuth2AuthorizationService;
import ch.cyberduck.core.brick.BrickProtocol;
import ch.cyberduck.core.brick.BrickSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import static org.junit.Assert.*;

public class TerminalLoginServiceTest {
    @Test
    public void testFilesComOAuthKeyReusedWithExplicitUsername() throws Exception {
        final String previousStore = PreferencesFactory.get().getProperty("factory.passwordstore.class");
        final String previousProvider = PreferencesFactory.get().getProperty("factory.authorizationcodeprovider.class");
        final String previousKeychain = PreferencesFactory.get().getProperty("connection.login.keychain");
        final String previousUsername = PreferencesFactory.get().getProperty("connection.login.name");
        PreferencesFactory.get().setProperty("factory.passwordstore.class", MemoryPasswordStore.class.getName());
        PreferencesFactory.get().setProperty("factory.authorizationcodeprovider.class", CodeProvider.class.getName());
        PreferencesFactory.get().setProperty("connection.login.keychain", "true");
        PreferencesFactory.get().setProperty("connection.login.name", "os-user");
        MemoryPasswordStore.properties.clear();
        CodeProvider.prompts = 0;
        try {
            final ProtocolFactory factory = new ProtocolFactory(Collections.singleton(new BrickProtocol()));
            factory.register(new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/Files.com.cyberduckprofile")));
            for(int invocation = 0; invocation < 2; invocation++) {
                final CommandLine input = new DefaultParser().parse(TerminalOptionsBuilder.options(),
                        new String[]{"--username", "jane", "--list", "brick://customer.files.com/"});
                final Host host = new CommandLineUriParser(input, factory).parse("brick://customer.files.com/");
                final TerminalLoginService login = new TerminalLoginService(input);
                login.validate(host, new DefaultX509KeyManager(), LoginCallback.noop, new LoginOptions(host.getProtocol()));
                assertEquals("jane", host.getCredentials().getUsername());
                assertEquals(invocation == 0 ? null : "api-key", host.getCredentials().getPassword());
                final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
                    @Override
                    public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                        final MockHttpTransport transport = new MockHttpTransport.Builder().setLowLevelHttpResponse(new MockLowLevelHttpResponse()
                                .setContentType("application/json")
                                .setContent("{\"access_token\":\"oauth-token\",\"token_type\":\"Bearer\",\"expires_in\":300,"
                                        + "\"files_api_key\":{\"password\":\"api-key\",\"user_username\":\"jane\","
                                        + "\"server\":\"https://customer.files.com\",\"nickname\":\"jane @ customer.files.com\"}}"))
                                .build();
                        new BrickOAuth2AuthorizationService(transport, host, host.getProtocol().getOAuthClientId(), prompt).authorize(cancel);
                        return host.getCredentials();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T _getFeature(final Class<T> type) {
                        if(type == AttributesFinder.class) {
                            return (T) (AttributesFinder) (file, listener) -> PathAttributes.EMPTY;
                        }
                        return super._getFeature(type);
                    }
                };
                try {
                    session.open(target -> Proxy.DIRECT, HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
                    assertTrue(login.authenticate(session, ProgressListener.noop, LoginCallback.noop, CancelCallback.noop));
                    assertEquals("api-key", host.getCredentials().getPassword());
                    assertEquals(1, CodeProvider.prompts);
                }
                finally {
                    session.close();
                }
            }
        }
        finally {
            PreferencesFactory.get().setProperty("factory.passwordstore.class", previousStore);
            PreferencesFactory.get().setProperty("factory.authorizationcodeprovider.class", previousProvider);
            PreferencesFactory.get().setProperty("connection.login.keychain", previousKeychain);
            PreferencesFactory.get().setProperty("connection.login.name", previousUsername);
            MemoryPasswordStore.properties.clear();
        }
    }

    public static class MemoryPasswordStore extends UnsecureHostPasswordStore {
        private static final Properties properties = new Properties();

        public MemoryPasswordStore() {
            super(null);
        }

        @Override
        protected Properties load() {
            return properties;
        }

        @Override
        protected void save(final Properties properties) {
            // Keep credentials in memory across fresh login services.
        }
    }

    public static class CodeProvider implements OAuth2AuthorizationCodeProvider {
        private static int prompts;

        @Override
        public String prompt(final Host host, final LoginCallback prompt, final String authorizationCodeUrl, final String redirectUri, final String state) {
            prompts++;
            return "authorization-code";
        }
    }
}
