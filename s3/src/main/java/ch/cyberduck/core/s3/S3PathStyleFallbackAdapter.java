package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class S3PathStyleFallbackAdapter<R> extends BackgroundExceptionCallable<R> {
    private static final Logger log = LogManager.getLogger(S3PathStyleFallbackAdapter.class);

    private final RequestEntityRestStorageService client;
    private final BackgroundExceptionCallable<R> proxy;

    public S3PathStyleFallbackAdapter(final RequestEntityRestStorageService client, final BackgroundExceptionCallable<R> proxy) {
        this.client = client;
        this.proxy = proxy;
    }

    @Override
    public R call() throws BackgroundException {
        try {
            return proxy.call();
        }
        catch(ResolveFailedException e) {
            if(S3Session.isAwsHostname(client.getEndpoint())) {
                throw e;
            }
            log.warn(String.format("Failure %s resolving bucket name. Disable use of DNS bucket names", e));
            client.disableDnsBuckets();
            return proxy.call();
        }
    }
}
