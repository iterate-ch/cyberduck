package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

public abstract class AbstractSharepointTest extends AbstractGraphTest {
    protected SharepointSession session;

    @Override
    protected Protocol protocol() {
        return new OneDriveProtocol();
    }

    @Override
    protected Local profile() {
        return new Local("../profiles/default/Microsoft SharePoint.cyberduckprofile");
    }

    @Override
    protected GraphSession session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        return (session = new SharepointSession(host, trust, key));
    }

    @Override
    protected HostPasswordStore passwordStore() {
        return new DisabledPasswordStore() {
            @Override
            public String getPassword(Scheme scheme, int port, String hostname, String user) {
                if(user.endsWith("Microsoft SharePoint (cyberduck) OAuth2 Access Token")) {
                    return System.getProperties().getProperty("sharepoint.accesstoken");
                }
                if(user.endsWith("Microsoft SharePoint (cyberduck) OAuth2 Refresh Token")) {
                    return System.getProperties().getProperty("sharepoint.refreshtoken");
                }
                return null;
            }
        };
    }
}
