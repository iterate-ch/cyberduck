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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.udt.UDTProxyConfigurator;
import ch.cyberduck.core.udt.UDTProxyProvider;
import ch.cyberduck.core.udt.UDTTransferAcceleration;

public class SpectraTransferAcceleration implements UDTTransferAcceleration {

    private final HttpSession<?> session;

    public SpectraTransferAcceleration(final HttpSession<?> session) {
        this.session = session;
    }

    @Override
    public UDTProxyProvider provider() {
        return new SpectraQloudsonicProxyProvider();
    }

    @Override
    public boolean getStatus(final Path file) {
        return true;
    }

    @Override
    public void setStatus(final Path file, final boolean enabled) {
        //
    }

    @Override
    public boolean prompt(final Host bookmark, final Path file, final ConnectionCallback prompt)
            throws BackgroundException {
        return true;
    }

    @Override
    public void configure(final boolean enable, final Path file) throws BackgroundException {
        final UDTProxyConfigurator configurator = new UDTProxyConfigurator(Location.unknown, this.provider(),
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(session.getHost())), new KeychainX509KeyManager(session.getHost()));
        configurator.configure(session);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpectraTransferAcceleration{");
        sb.append("session=").append(session);
        sb.append('}');
        return sb.toString();
    }
}
