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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.junit.runners.Parameterized;

import java.io.InputStream;

public abstract class AbstractOneDriveTest extends AbstractGraphTest {
    protected OneDriveSession session;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{CryptoVault.VAULT_VERSION_DEPRECATED, CryptoVault.VAULT_VERSION};
    }

    @Parameterized.Parameter
    public int vaultVersion;

    @Override
    protected Protocol protocol() {
        return new OneDriveProtocol();
    }

    @Override
    protected InputStream profile() {
        return this.getClass().getResourceAsStream("/Microsoft OneDrive.cyberduckprofile");
    }

    @Override
    protected String user() {
        return PROPERTIES.get("onedrive.user");
    }

    @Override
    protected GraphSession session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        return (session = new OneDriveSession(host, trust, key));
    }
}
