package ch.cyberduck.core;

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

import java.util.Objects;

/**
 * Temporary access credentials
 */
public final class STSTokens {

    public static final STSTokens EMPTY
            = new STSTokens(null, null, null, Long.MAX_VALUE);

    private final String accessKeyId;
    private final String secretAccessKey;
    private final String sessionToken;
    private final Long expiryInMilliseconds;

    public STSTokens(final String sessionToken) {
        this(null, null, sessionToken, -1L);
    }

    public STSTokens(final String accessKeyId, final String secretAccessKey, final String sessionToken, final Long expiryInMilliseconds) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.expiryInMilliseconds = expiryInMilliseconds;
    }

    public boolean validate() {
        return StringUtils.isNotEmpty(sessionToken);
    }

    public String getAccessKeyId() {
        return accessKeyId;
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
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final STSTokens stsTokens = (STSTokens) o;
        if(!Objects.equals(accessKeyId, stsTokens.accessKeyId)) {
            return false;
        }
        if(!Objects.equals(secretAccessKey, stsTokens.secretAccessKey)) {
            return false;
        }
        if(!Objects.equals(sessionToken, stsTokens.sessionToken)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = accessKeyId != null ? accessKeyId.hashCode() : 0;
        result = 31 * result + (secretAccessKey != null ? secretAccessKey.hashCode() : 0);
        result = 31 * result + (sessionToken != null ? sessionToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("STSTokens{");
        sb.append("accessKeyId='").append(StringUtils.repeat("*", Integer.min(8, StringUtils.length(accessKeyId)))).append('\'');
        sb.append(", secretAccessKey='").append(StringUtils.repeat("*", Integer.min(8, StringUtils.length(secretAccessKey)))).append('\'');
        sb.append(", sessionToken='").append(StringUtils.repeat("*", Integer.min(8, StringUtils.length(sessionToken)))).append('\'');
        sb.append(", expiryInMilliseconds=").append(expiryInMilliseconds);
        sb.append('}');
        return sb.toString();
    }
}
