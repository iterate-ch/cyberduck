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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;

import org.junit.After;
import org.junit.Before;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.fail;

public abstract class AbstractOneDriveTest extends AbstractGraphTest {
    @Override
    protected OneDriveSession session() {
        return (OneDriveSession)super.session();
    }

    @Override
    protected Protocol protocol() {
        return new OneDriveProtocol();
    }

    @Override
    protected Local profile() {
        return new Local("../profiles/default/Microsoft OneDrive.cyberduckprofile");
    }

    @Override
    protected HostPasswordStore passwordStore() {
        return new DisabledPasswordStore() {
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
        };
    }
}
