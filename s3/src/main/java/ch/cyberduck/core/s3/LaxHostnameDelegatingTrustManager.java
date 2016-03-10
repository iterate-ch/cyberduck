package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class LaxHostnameDelegatingTrustManager extends ThreadLocalHostnameDelegatingTrustManager {
    private static final Logger log = Logger.getLogger(LaxHostnameDelegatingTrustManager.class);

    public LaxHostnameDelegatingTrustManager(final X509TrustManager delegate, final String hostname) {
        super(delegate, hostname);
    }

    @Override
    public void setTarget(final String hostname) {
        final String simple;
        final String[] parts = StringUtils.split(hostname, '.');
        if(parts.length > 4) {
            ArrayUtils.reverse(parts);
            // Rewrite c.cyberduck.s3.amazonaws.com which does not match wildcard certificate *.s3.amazonaws.com
            simple = StringUtils.join(parts[3], ".", parts[2], ".", parts[1], ".", parts[0]);
            log.warn(String.format("Rewrite hostname target to %s", simple));
        }
        else {
            simple = hostname;
        }
        super.setTarget(simple);
    }
}
