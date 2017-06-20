package ch.cyberduck.core.spectra;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Header;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.udt.UDTProtocol;
import ch.cyberduck.core.udt.UDTProxyProvider;
import ch.cyberduck.core.udt.UDTTLSProtocol;

import java.util.Collections;
import java.util.List;

public class SpectraQloudsonicProxyProvider implements UDTProxyProvider {

    @Override
    public Host find(final Location.Name region, final boolean tls) {
        final Protocol protocol;
        if(tls) {
            protocol = new UDTTLSProtocol();
        }
        else {
            protocol = new UDTProtocol();
        }
        return new Host(protocol, "localhost", protocol.getScheme().getPort());
    }

    @Override
    public List<Header> headers() {
        return Collections.singletonList(new Header("X-Qloudsonic-Voucher", "demo"));
    }
}
