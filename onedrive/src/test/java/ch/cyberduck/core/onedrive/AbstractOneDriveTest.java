package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import java.io.InputStream;

public abstract class AbstractOneDriveTest extends AbstractGraphTest {
    protected OneDriveSession session;

    @Override
    protected Protocol protocol() {
        return new OneDriveProtocol();
    }

    @Override
    protected InputStream profile() {
        return this.getClass().getResourceAsStream("/Microsoft OneDrive.cyberduckprofile");
    }

    @Override
    protected HostPasswordStore passwordStore() {
        return new TestPasswordStore();
    }

    @Override
    protected GraphSession session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        return (session = new OneDriveSession(host, trust, key));
    }

    public final static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            if(user.endsWith("Microsoft OneDrive (cyberduck) OAuth2 Access Token")) {
                return System.getProperties().getProperty("onedrive.accesstoken");
            }
            if(user.endsWith("Microsoft OneDrive (cyberduck) OAuth2 Refresh Token")) {
                return System.getProperties().getProperty("onedrive.refreshtoken");
            }
            return null;
        }
    }
}
