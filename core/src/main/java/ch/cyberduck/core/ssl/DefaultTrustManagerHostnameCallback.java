package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.idna.PunycodeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultTrustManagerHostnameCallback implements TrustManagerHostnameCallback {
    private static final Logger log = LogManager.getLogger(DefaultTrustManagerHostnameCallback.class);

    private final Host host;

    public DefaultTrustManagerHostnameCallback(final Host host) {
        this.host = host;
    }

    @Override
    public String getTarget() {
        if(StringUtils.isBlank(host.getHostname())) {
            log.error("Missing hostname to validate in {}", host);
        }
        return new PunycodeConverter().convert(host.getHostname());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultTrustManagerHostnameCallback{");
        sb.append("host=").append(host);
        sb.append('}');
        return sb.toString();
    }
}
