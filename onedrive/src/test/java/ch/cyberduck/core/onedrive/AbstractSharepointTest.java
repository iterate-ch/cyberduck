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
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.test.VaultTest;

import java.io.InputStream;

public abstract class AbstractSharepointTest extends AbstractGraphTest {
    protected SharepointSession session;

    @Override
    protected Protocol protocol() {
        return new SharepointProtocol();
    }

    @Override
    protected InputStream profile() {
        return this.getClass().getResourceAsStream("/Microsoft SharePoint.cyberduckprofile");
    }

    @Override
    protected GraphSession session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        return (session = new SharepointSession(host, trust, key));
    }

    @Override
    protected String user() {
        return PROPERTIES.get("sharepoint.user");
    }

    @Override
    protected HostPasswordStore passwordStore() {
        return new DisabledPasswordStore() {

            @Override
            public String getPassword(final String serviceName, final String accountName) {
                if(accountName.equals("Microsoft SharePoint (sharepoint@iterategmbh.onmicrosoft.com) OAuth2 Token Expiry")) {
                    return PROPERTIES.get("sharepoint.tokenexpiry");
                }
                return null;
            }

            @Override
            public String getPassword(Scheme scheme, int port, String hostname, String user) {
                if(user.endsWith("Microsoft SharePoint (sharepoint@iterategmbh.onmicrosoft.com) OAuth2 Access Token")) {
                    return PROPERTIES.get("sharepoint.accesstoken");
                }
                if(user.endsWith("Microsoft SharePoint (sharepoint@iterategmbh.onmicrosoft.com) OAuth2 Refresh Token")) {
                    return PROPERTIES.get("sharepoint.refreshtoken");
                }
                return null;
            }

            @Override
            public void addPassword(final String serviceName, final String accountName, final String password) {
                if(accountName.equals("Microsoft SharePoint (sharepoint@iterategmbh.onmicrosoft.com) OAuth2 Token Expiry")) {
                    VaultTest.add("sharepoint.tokenexpiry", password);
                }
            }

            @Override
            public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
                if(user.equals("Microsoft SharePoint (sharepoint@iterategmbh.onmicrosoft.com) OAuth2 Access Token")) {
                    VaultTest.add("sharepoint.accesstoken", password);
                }
                if(user.equals("Microsoft SharePoint (sharepoint@iterategmbh.onmicrosoft.com) OAuth2 Refresh Token")) {
                    VaultTest.add("sharepoint.refreshtoken", password);
                }
            }
        };
    }
}
