package ch.cyberduck.core.azure;

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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import java.util.Collections;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.HttpResponseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class AzureOAuthTokenCredentialTest {

    @Test
    public void testGetToken() {
        final Credentials credentials = new Credentials();
        final OAuthTokens refreshed = new OAuthTokens("access", "refresh", 1893456000000L);
        final OAuth2AuthorizationService service = new OAuth2AuthorizationService(HttpClientBuilder.create().build(), new Host(new AzureProtocol()),
                "https://login.example.com/token", "https://login.example.com/authorize", "id", "", Collections.emptyList(), false, LoginCallback.noop) {
            @Override
            public OAuthTokens validate(final OAuthTokens saved, final CancelCallback cancel) throws BackgroundException {
                return refreshed;
            }
        };
        final AccessToken token = new AzureOAuthTokenCredential(service, credentials, CancelCallback.noop)
                .getTokenSync(new TokenRequestContext());
        assertEquals("access", token.getToken());
        assertEquals(1893456000L, token.getExpiresAt().toEpochSecond());
        assertEquals(refreshed, credentials.getOauth());
    }

    @Test
    public void testGetTokenFailureMappedToBackgroundException() {
        final LoginCanceledException failure = new LoginCanceledException();
        final OAuth2AuthorizationService service = new OAuth2AuthorizationService(HttpClientBuilder.create().build(), new Host(new AzureProtocol()),
                "https://login.example.com/token", "https://login.example.com/authorize", "id", "", Collections.emptyList(), false, LoginCallback.noop) {
            @Override
            public OAuthTokens validate(final OAuthTokens saved, final CancelCallback cancel) throws BackgroundException {
                throw failure;
            }
        };
        try {
            new AzureOAuthTokenCredential(service, new Credentials(), CancelCallback.noop).getTokenSync(new TokenRequestContext());
            fail();
        }
        catch(HttpResponseException e) {
            assertSame(failure, new AzureExceptionMappingService().map(e));
        }
    }
}
