package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;

public final class OAuthTokens {
    public static final OAuthTokens EMPTY = new OAuthTokens(null, null, Long.MAX_VALUE, null);

    private final String accessToken;
    private final String refreshToken;
    private final Long expiryInMilliseconds;
    private final String idToken;

    public OAuthTokens(final String accessToken, final String refreshToken, final Long expiryInMilliseconds) {
        this(accessToken, refreshToken, expiryInMilliseconds, null);
    }

    public OAuthTokens(final String accessToken, final String refreshToken, final Long expiryInMilliseconds, final String idToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiryInMilliseconds = expiryInMilliseconds;
        this.idToken = idToken;
    }

    public boolean validate() {
        return StringUtils.isNotEmpty(accessToken);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getExpiryInMilliseconds() {
        return expiryInMilliseconds;
    }

    public String getIdToken() { return idToken; }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiryInMilliseconds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OAuthTokens{");
        sb.append("accessToken='").append(StringUtils.repeat("*", Integer.min(8, StringUtils.length(accessToken)))).append('\'');
        sb.append(", refreshToken='").append(StringUtils.repeat("*", Integer.min(8, StringUtils.length(refreshToken)))).append('\'');
        sb.append(", expiryInMilliseconds=").append(expiryInMilliseconds);
        sb.append('}');
        return sb.toString();
    }
}
