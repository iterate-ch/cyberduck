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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.impl.uvf.DefaultUVFVaultMetadataProvider;
import ch.cyberduck.core.cryptomator.impl.uvf.UVFVault;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.cryptomator.cryptolib.api.Cvf8UnlockedVault;
import org.cryptomator.cryptolib.api.CvfMasterkey;
import org.junit.runners.Parameterized;

import java.util.EnumSet;

public abstract class AbstractCryptoTests {

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultVersion.Type.V8, VaultVersion.Type.UVF};
    }

    @Parameterized.Parameter
    public VaultVersion.Type vaultVersion;

    /**
     * @return New vault of the parameterized version unlocked in memory without storage. Only file content encryption
     * and directory metadata in memory are available.
     */
    protected AbstractVault unlockedVault() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        switch(vaultVersion) {
            case UVF: {
                final AbstractVault vault = new UVFVault(home);
                FieldUtils.writeField(vault, "vault", new DefaultUVFVaultMetadataProvider(new VaultCredentials("test")).create(), true);
                return vault;
            }
            default: {
                final AbstractVault vault = new CryptomatorVault(home);
                try(CvfMasterkey masterkey = CvfMasterkey.generate(FastSecureRandomProvider.get().provide())) {
                    FieldUtils.writeField(vault, "vault", Cvf8UnlockedVault.create(masterkey,
                            CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD, FastSecureRandomProvider.get().provide()), true);
                }
                return vault;
            }
        }
    }
}
