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
    public static final OAuthTokens EMPTY = new OAuthTokens(null, null, Long.MAX_VALUE);

    private String accessToken;
    private String refreshToken;
    private Long expiryInMilliseconds;

    public OAuthTokens(final String accessToken, final String refreshToken, final Long expiryInMilliseconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiryInMilliseconds = expiryInMilliseconds;
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

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiryInMilliseconds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tokens{");
        sb.append("accessToken='").append(accessToken).append('\'');
        sb.append(", refreshToken='").append(refreshToken).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
