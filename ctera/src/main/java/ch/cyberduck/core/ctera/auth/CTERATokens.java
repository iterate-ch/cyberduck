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
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.log4j.Logger;

public class CTERATokens {

    private static final Logger log = Logger.getLogger(CTERATokens.class);

    private String deviceId;
    private String sharedSecret;

    public static CTERATokens parse(final String token) throws BackgroundException {
        final String[] t = token.split(":");
        if(t.length < 2) {
            log.error("Unable to parse token");
            throw new LoginCanceledException();
        }
        return new CTERATokens().
            setDeviceId(t[0]).
            setSharedSecret(t[1]);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public CTERATokens setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public CTERATokens setSharedSecret(final String sharedSecret) {
        this.sharedSecret = sharedSecret;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", deviceId, sharedSecret);
    }
}
