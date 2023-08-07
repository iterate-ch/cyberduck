package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

public final class STSTokens {

    public static final STSTokens EMPTY
            = new STSTokens(null, null, null, Long.MAX_VALUE);

    private final String accessKey;
    private final String secretAccessKey;
    private final Long expiryInMilliseconds;
    private final String sessionToken;

    public STSTokens(final String accessKey, final String secretAccessKey, final String sessionToken, final Long expiryInMilliseconds) {
        this.accessKey = accessKey;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.expiryInMilliseconds = expiryInMilliseconds;
    }

    public boolean validate() {
        return StringUtils.isNotEmpty(sessionToken);
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public Long getExpiryInMilliseconds() {
        return expiryInMilliseconds;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiryInMilliseconds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OAuthTokens{");
        sb.append("accessKey='").append(StringUtils.repeat("*", Integer.min(8, StringUtils.length(accessKey)))).append('\'');
        sb.append(", secretAccessKey='").append(StringUtils.repeat("*", Integer.min(8, StringUtils.length(secretAccessKey)))).append('\'');
        sb.append(", sessionToken='").append(StringUtils.repeat("*", Integer.min(8, StringUtils.length(secretAccessKey)))).append('\'');
        sb.append(", expiryInMilliseconds=").append(expiryInMilliseconds);
        sb.append('}');
        return sb.toString();
    }
}
