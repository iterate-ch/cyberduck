package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.vault.VaultMetadata;

import org.junit.runners.Parameterized;

public abstract class AbstractCryptoTests {

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultMetadata.Type.V8, VaultMetadata.Type.UVF};
    }

    @Parameterized.Parameter
    public VaultMetadata.Type vaultVersion;

}
