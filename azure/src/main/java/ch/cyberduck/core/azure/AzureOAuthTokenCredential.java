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
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.threading.CancelCallback;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import reactor.core.publisher.Mono;

/**
 * Provides access tokens from Microsoft Entra ID retrieved with the OAuth 2.0 flow configured in the connection profile
 */
public final class AzureOAuthTokenCredential implements TokenCredential {

    private final OAuth2AuthorizationService authorization;
    private final Credentials credentials;
    private final CancelCallback cancel;

    public AzureOAuthTokenCredential(final OAuth2AuthorizationService authorization, final Credentials credentials, final CancelCallback cancel) {
        this.authorization = authorization;
        this.credentials = credentials;
        this.cancel = cancel;
    }

    @Override
    public Mono<AccessToken> getToken(final TokenRequestContext request) {
        return Mono.fromCallable(this::get);
    }

    @Override
    public AccessToken getTokenSync(final TokenRequestContext request) {
        return this.get();
    }

    private synchronized AccessToken get() {
        try {
            final OAuthTokens tokens = authorization.validate(credentials.getOauth(), cancel);
            credentials.setOauth(tokens);
            return new AccessToken(tokens.getAccessToken(),
                    OffsetDateTime.ofInstant(Instant.ofEpochMilli(tokens.getExpiryInMilliseconds()), ZoneOffset.UTC));
        }
        catch(BackgroundException e) {
            // Surface as failure from the SDK pipeline
            throw new ClientAuthenticationException(e.getMessage(), null, e);
        }
    }
}
