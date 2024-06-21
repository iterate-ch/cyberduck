package ch.cyberduck.core.ctera.auth;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CteraTokens {
    private static final Logger log = LogManager.getLogger(CteraTokens.class);

    public static CteraTokens EMPTY = new CteraTokens(StringUtils.EMPTY, StringUtils.EMPTY);

    private final String deviceId;
    private final String sharedSecret;

    public CteraTokens(final String deviceId, final String sharedSecret) {
        this.deviceId = deviceId;
        this.sharedSecret = sharedSecret;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public boolean validate() {
        return StringUtils.isNotEmpty(deviceId) && StringUtils.isNotEmpty(sharedSecret);
    }

    public static CteraTokens parse(final String token) throws BackgroundException {
        if(null == token) {
            return EMPTY;
        }
        return new CteraTokens(StringUtils.substringBefore(token, ':'), StringUtils.substringAfter(token, ':'));
    }

    @Override
    public String toString() {
        return String.format("%s:%s", deviceId, StringUtils.repeat("*", Integer.min(8, StringUtils.length(sharedSecret))));
    }
}
