package ch.cyberduck.core.sso;

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

import org.junit.Test;

import java.lang.reflect.Field;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.json.MockJsonFactory;

import static org.junit.Assert.assertNotNull;

public class RegisterClientOAuth2RequestInterceptorTest {

    @Test
    public void testExchangeToken() throws Exception {
        final AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                new MockHttpTransport(),
                new MockJsonFactory(),
                new GenericUrl("https://localhost/oauth2/token"),
                null,
                "clientid",
                "https://localhost/oauth2/auth"
        ).enablePKCE().build();
        final Field pkceField = AuthorizationCodeFlow.class.getDeclaredField("pkce");
        pkceField.setAccessible(true);
        final Object pkce = pkceField.get(flow);
        assertNotNull(pkce);
        // Get the code verifier using reflection
        final Field codeVerifierField = pkce.getClass().getDeclaredField("verifier");
        codeVerifierField.setAccessible(true);
        final String codeVerifier = (String) codeVerifierField.get(pkce);
        assertNotNull(codeVerifier);
    }
}