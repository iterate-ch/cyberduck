package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.preferences.PreferencesReader;

public interface CredentialsHolder extends PreferencesReader {
    String getUsername();

    Credentials setUsername(String user);

    String getPassword();

    Credentials setPassword(String password);

    String getToken();

    Credentials setToken(String token);

    TemporaryAccessTokens getTokens();

    Credentials setTokens(TemporaryAccessTokens tokens);

    OAuthTokens getOauth();

    Credentials setOauth(OAuthTokens oauth);

    boolean isSaved();

    Credentials setSaved(boolean saved);

    boolean isAnonymousLogin();

    boolean isPasswordAuthentication();

    boolean isPasswordAuthentication(boolean allowblank);

    boolean isTokenAuthentication();

    boolean isOAuthAuthentication();

    boolean isPublicKeyAuthentication();

    Local getIdentity();

    Credentials setIdentity(Local file);

    String getIdentityPassphrase();

    Credentials setIdentityPassphrase(String identityPassphrase);

    String getCertificate();

    Credentials setCertificate(String certificate);

    boolean isCertificateAuthentication();

    Credentials setProperty(String key, String value);

    String getProperty(String key);

    boolean validate(Protocol protocol, LoginOptions options);

    void reset();
}
