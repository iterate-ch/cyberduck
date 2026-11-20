package ch.cyberduck.core.brick;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.KeychainLoginService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.UnsecureHostPasswordStore;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.HostDictionary;
import ch.cyberduck.core.serializer.impl.dd.PlistSerializer;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

public class BrickOAuth2AuthorizationServiceTest {
    @Test
    public void testAuthorizationWithDedicatedClientAndPKCE() throws Exception {
        final Host host = this.host();
        host.setProperty(BrickOAuth2AuthorizationService.VERIFIED, "true");
        final MockHttpTransport transport = this.transport(this.key());
        final OAuth2AuthorizationCodeProvider provider = this.provider();
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final BrickOAuth2AuthorizationService service = this.service(host, transport, store, provider);
        service.setParameter("platform", "Cyberduck");
        final OAuthTokens tokens = service.validate(OAuthTokens.EMPTY, CancelCallback.noop);
        final ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(provider).prompt(eq(host), any(), url.capture(), anyString(), anyString());
        final GenericUrl authorize = new GenericUrl(url.getValue());
        assertEquals("cyberduck", authorize.getFirst("client_id"));
        assertEquals("desktop", authorize.getFirst("scope"));
        assertEquals("S256", authorize.getFirst("code_challenge_method"));
        assertEquals(InetAddress.getLocalHost().getHostName(), authorize.getFirst("computer"));
        assertEquals("Cyberduck", authorize.getFirst("platform"));
        assertNotNull(authorize.getFirst("state"));
        final GenericUrl token = new GenericUrl("http://localhost/?" + transport.getLowLevelHttpRequest().getContentAsString());
        assertEquals("cyberduck", token.getFirst("client_id"));
        assertNull(token.getFirst("client_secret"));
        assertEquals(authorize.getFirst("redirect_uri"), token.getFirst("redirect_uri"));
        final String verifier = (String) token.getFirst("code_verifier");
        assertEquals(authorize.getFirst("code_challenge"), Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII))));
        assertEquals("api-key", tokens.getAccessToken());
        assertNull(tokens.getRefreshToken());
        assertNull(tokens.getIdToken());
        assertEquals(Long.valueOf(Long.MAX_VALUE), tokens.getExpiryInMilliseconds());
        assertFalse(host.getCredentials().isPasswordAuthentication());
        assertSame(tokens, host.getCredentials().getOauth());
        verify(store).save(host);
    }

    @Test
    public void testFreshAuthorizationAdoptsAuthorizedAccountAndCanonicalHostname() throws Exception {
        final Host host = new Host(this.protocol(), "custom.example.com", new Credentials("os-user"));
        final OAuth2AuthorizationCodeProvider provider = this.provider();
        this.service(host, this.transport(this.key()), mock(HostPasswordStore.class), provider).authorize(CancelCallback.noop);
        assertEquals("customer.files.com", host.getHostname());
        assertEquals("jane", host.getCredentials().getUsername());
    }

    @Test
    public void testReauthorizationAdoptsBrowserAccountAndSavesKey() throws Exception {
        final Host host = this.host();
        host.setProperty(BrickOAuth2AuthorizationService.VERIFIED, "true");
        final Map<String, Object> key = this.key();
        key.put("user_username", "other-user");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final OAuth2AuthorizationCodeProvider provider = this.provider();
        final OAuthTokens tokens = this.service(host, this.transport(key), store, provider).validate(OAuthTokens.EMPTY, CancelCallback.noop);
        assertEquals("other-user", host.getCredentials().getUsername());
        assertEquals("api-key", tokens.getAccessToken());
        verify(store).save(host);
        final ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(provider).prompt(eq(host), any(), url.capture(), anyString(), anyString());
        assertNull(new GenericUrl(url.getValue()).getFirst("login_hint"));
    }

    @Test
    public void testVerifiedSiteSurvivesBookmarkSerializationAndTokenReset() throws Exception {
        final Host host = this.host();
        host.setProperty(BrickOAuth2AuthorizationService.VERIFIED, "true");
        final ProtocolFactory factory = new ProtocolFactory(Collections.singleton(new BrickProtocol()));
        factory.register(host.getProtocol());
        final Host restored = new HostDictionary<>(factory).deserialize(host.serialize(new PlistSerializer()));
        assertEquals("true", restored.getProperty(BrickOAuth2AuthorizationService.VERIFIED));
        assertFalse(restored.getCredentials().getOauth().validate());
        for(String server : new String[]{"https://other.files.com", "https://customer.files.com:8443"}) {
            final Map<String, Object> key = this.key();
            key.put("server", server);
            assertThrows(LoginFailureException.class, () -> this.service(restored, this.transport(key), mock(HostPasswordStore.class), this.provider())
                    .authorize(CancelCallback.noop));
            assertEquals("customer.files.com", restored.getHostname());
            assertEquals("jane", restored.getCredentials().getUsername());
        }
    }

    @Test
    public void testDefaultHostnameComesFromProfile() throws Exception {
        final Profile protocol = mock(Profile.class, delegatesTo(this.protocol()));
        doReturn("bootstrap.example.com").when(protocol).getDefaultHostname();
        final Host host = new Host(protocol, "BOOTSTRAP.example.com", new Credentials("jane"));
        host.setProperty(BrickOAuth2AuthorizationService.VERIFIED, "true");
        this.service(host, this.transport(this.key()), mock(HostPasswordStore.class), this.provider()).authorize(CancelCallback.noop);
        assertEquals("customer.files.com", host.getHostname());
    }

    @Test
    public void testMissingOrInvalidRequiredFields() throws Exception {
        for(String field : new String[]{"password", "user_username", "server"}) {
            final Map<String, Object> key = this.key();
            key.remove(field);
            assertThrows(InteroperabilityException.class, () -> this.service(this.host(), this.transport(key), mock(HostPasswordStore.class), this.provider())
                    .authorize(CancelCallback.noop));
        }
        for(String server : new String[]{"http://customer.files.com", "not a URI", "https:///missing-host"}) {
            final Map<String, Object> key = this.key();
            key.put("server", server);
            assertThrows(InteroperabilityException.class, () -> this.service(this.host(), this.transport(key), mock(HostPasswordStore.class), this.provider())
                    .authorize(CancelCallback.noop));
        }
        for(Object payload : new Object[]{null, "invalid"}) {
            assertThrows(InteroperabilityException.class, () -> this.service(this.host(), this.transport(payload), mock(HostPasswordStore.class), this.provider())
                    .authorize(CancelCallback.noop));
        }
    }

    @Test
    public void testMigrationSavesTokenWithoutRemovingLegacyKey() throws Exception {
        final Host host = this.host();
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(host)).thenReturn("legacy-key");
        final OAuth2AuthorizationCodeProvider provider = this.provider();
        final BrickOAuth2AuthorizationService service = this.service(host, this.transport(this.key()), store, provider);
        final OAuthTokens tokens = service.validate(OAuthTokens.EMPTY, CancelCallback.noop);
        assertEquals("legacy-key", tokens.getAccessToken());
        assertFalse(tokens.isExpired());
        assertSame(tokens, service.validate(tokens, CancelCallback.noop));
        final InOrder order = inOrder(store);
        order.verify(store).findLoginPassword(host);
        order.verify(store).save(host);
        verifyNoInteractions(provider);
        verifyNoMoreInteractions(store);
    }

    @Test
    public void testRejectedMigratedKeyIsNotLoadedAgain() throws Exception {
        final Host host = this.host();
        final Properties passwords = new Properties();
        final HostPasswordStore store = new UnsecureHostPasswordStore(null) {
            @Override
            protected Properties load() {
                return passwords;
            }

            @Override
            protected void save(final Properties values) {
            }
        };
        store.addPassword(host.getProtocol().getScheme(), host.getPort(), host.getHostname(), "jane", "legacy-key");
        final OAuth2AuthorizationCodeProvider provider = this.provider();
        final BrickOAuth2AuthorizationService service = this.service(host, this.transport(this.key()), store, provider);
        service.validate(OAuthTokens.EMPTY, CancelCallback.noop);
        assertEquals("legacy-key", store.findLoginPassword(host));
        assertEquals("legacy-key", store.findOAuthTokens(host).getAccessToken());
        new KeychainLoginService(store).prompt(host, "Rejected key", LoginCallback.noop, new LoginOptions(host.getProtocol()));
        assertEquals("api-key", service.validate(host.getCredentials().getOauth(), CancelCallback.noop).getAccessToken());
        assertEquals("api-key", store.findOAuthTokens(host).getAccessToken());
        verify(provider).prompt(eq(host), any(), anyString(), anyString(), anyString());
    }

    @Test
    public void testSavedOAuthKeyUsesStandardKeychainLookup() throws Exception {
        final Host host = this.host();
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final OAuthTokens tokens = new OAuthTokens("saved-key", null, Long.MAX_VALUE);
        when(store.findOAuthTokens(host)).thenReturn(tokens);
        new KeychainLoginService(store).validate(host, null, LoginCallback.noop, new LoginOptions(host.getProtocol()));
        final OAuth2AuthorizationCodeProvider provider = this.provider();
        assertSame(tokens, this.service(host, this.transport(this.key()), store, provider).validate(host.getCredentials().getOauth(), CancelCallback.noop));
        verify(store).findOAuthTokens(host);
        verifyNoMoreInteractions(store);
        verifyNoInteractions(provider);
    }

    @Test
    public void testFailedMigrationSaveDoesNotStartAuthorization() throws Exception {
        final Host host = this.host();
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(host)).thenReturn("legacy-key");
        final AccessDeniedException failure = new AccessDeniedException("Cannot save");
        doThrow(failure).when(store).save(host);
        final OAuth2AuthorizationCodeProvider provider = this.provider();
        final BrickOAuth2AuthorizationService service = this.service(host, this.transport(this.key()), store, provider);
        assertSame(failure, assertThrows(AccessDeniedException.class, () -> service.validate(OAuthTokens.EMPTY, CancelCallback.noop)));
        verifyNoInteractions(provider);
    }

    @Test
    public void testKeychainOptOutSkipsMigrationAndSaving() throws Exception {
        final Preferences preferences = mock(Preferences.class, delegatesTo(PreferencesFactory.get()));
        doReturn(false).when(preferences).getBoolean("connection.login.keychain");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        try(MockedStatic<PreferencesFactory> factory = mockStatic(PreferencesFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
            factory.when(PreferencesFactory::get).thenReturn(preferences);
            final Host host = this.host();
            assertEquals("api-key", this.service(host, this.transport(this.key()), store, this.provider()).validate(OAuthTokens.EMPTY, CancelCallback.noop).getAccessToken());
            assertFalse(host.getCredentials().isSaved());
            verifyNoInteractions(store);
        }
    }

    private Host host() throws Exception {
        return new Host(this.protocol(), "customer.files.com", new Credentials("jane"));
    }

    private Profile protocol() throws Exception {
        return new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new BrickProtocol())))
                .read(this.getClass().getResourceAsStream("/Files.com.cyberduckprofile"));
    }

    private OAuth2AuthorizationCodeProvider provider() throws Exception {
        final OAuth2AuthorizationCodeProvider provider = mock(OAuth2AuthorizationCodeProvider.class);
        when(provider.prompt(any(), any(), anyString(), anyString(), anyString())).thenReturn("code");
        return provider;
    }

    private BrickOAuth2AuthorizationService service(final Host host, final MockHttpTransport transport, final HostPasswordStore store,
                                                    final OAuth2AuthorizationCodeProvider provider) {
        try(MockedStatic<PasswordStoreFactory> factory = mockStatic(PasswordStoreFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
            factory.when(PasswordStoreFactory::get).thenReturn(store);
            return new BrickOAuth2AuthorizationService(transport, host, LoginCallback.noop, CancelCallback.noop, provider);
        }
    }

    private Map<String, Object> key() {
        final Map<String, Object> key = new LinkedHashMap<>();
        key.put("password", "api-key");
        key.put("user_username", "jane");
        key.put("server", "https://customer.files.com");
        return key;
    }

    private MockHttpTransport transport(final Object key) throws Exception {
        final GenericJson response = new GenericJson().set("access_token", "oauth-token").set("refresh_token", "refresh-token")
                .set("id_token", "unused-id-token").set("token_type", "Bearer").set("expires_in", 300);
        if(key != null) {
            response.set("files_api_key", key);
        }
        return new MockHttpTransport.Builder().setLowLevelHttpResponse(new MockLowLevelHttpResponse()
                .setContentType("application/json").setContent(new GsonFactory().toString(response))).build();
    }
}
