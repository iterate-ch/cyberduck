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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3BucketHostnameTrustManager extends ThreadLocalHostnameDelegatingTrustManager {
    private static final Logger log = LogManager.getLogger(S3BucketHostnameTrustManager.class);

    /**
     * Test for pattern of bucket name containing dot
     */
    private final Pattern pattern = Pattern.compile("([a-z0-9.-]+\\.)([a-z0-9.-]+\\.s3(\\.dualstack)?(\\.[a-z0-9-]+)?.amazonaws.com)");

    public S3BucketHostnameTrustManager(final X509TrustManager delegate, final String hostname) {
        super(delegate, hostname);
    }

    @Override
    public String getTarget() {
        final String hostname = super.getTarget();
        final Matcher matcher = pattern.matcher(hostname);
        if(matcher.matches()) {
            final String simple = matcher.group(2);
            log.warn("Rewrite hostname target to {}", simple);
            return simple;
        }
        return hostname;
    }
}
