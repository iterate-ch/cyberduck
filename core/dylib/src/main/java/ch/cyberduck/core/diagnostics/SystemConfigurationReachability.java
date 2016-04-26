package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.idna.PunycodeConverter;
import ch.cyberduck.core.library.Native;

public final class SystemConfigurationReachability extends DefaultInetAddressReachability {

    static {
        Native.load("core");
    }

    public SystemConfigurationReachability() {
        //
    }

    @Override
    public boolean isReachable(final Host host) {
        if(super.isReachable(host)) {
            return this.isReachable(this.toURL(host));
        }
        return false;
    }

    private String toURL(final Host host) {
        StringBuilder url = new StringBuilder(host.getProtocol().getScheme().toString());
        url.append("://");
        url.append(new PunycodeConverter().convert(host.getHostname()));
        url.append(":").append(host.getPort());
        return url.toString();
    }

    private native boolean isReachable(String url);

    /**
     * Opens the network configuration assistant for the URL denoting this host
     */
    @Override
    public void diagnose(final Host host) {
        this.diagnose(new HostUrlProvider().get(host));
    }

    private native void diagnose(String url);
}
