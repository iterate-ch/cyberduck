package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.Resolver;
import ch.cyberduck.core.exception.ResolveCanceledException;
import ch.cyberduck.core.exception.ResolveFailedException;
import org.apache.http.conn.DnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CustomDnsResolver implements DnsResolver {

    private final Resolver resolver = new Resolver();

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        try {
            return new InetAddress[]{resolver.resolve(host, new DisabledCancelCallback())};
        }
        catch(ResolveFailedException | ResolveCanceledException e) {
            throw new UnknownHostException(e.getDetail(false));
        }
    }
}
