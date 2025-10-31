package ch.cyberduck.core.azure;/*
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

public class AbstractAzureTest extends VaultTest {

    protected AzureSession session;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultMetadata.Type.V8, VaultMetadata.Type.UVF};
    }

    @Parameterized.Parameter
    public VaultMetadata.Type vaultVersion;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                PROPERTIES.get("azure.user"), PROPERTIES.get("azure.password")
        ));
        session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
    }
}
