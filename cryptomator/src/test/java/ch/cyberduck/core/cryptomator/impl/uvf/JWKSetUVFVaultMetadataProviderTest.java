package ch.cyberduck.core.cryptomator.impl.uvf;

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

import ch.cyberduck.core.vault.VaultException;

import org.cryptomator.cryptolib.api.LockedVault;
import org.cryptomator.cryptolib.api.UvfLockedVault;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;
import org.cryptomator.cryptolib.api.VaultKeyInvalidException;
import org.junit.Test;

import java.util.Arrays;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;

import static org.junit.Assert.*;

public class JWKSetUVFVaultMetadataProviderTest {

    private static OctetSequenceKey memberKey() throws Exception {
        return new OctetSequenceKeyGenerator(256).keyID("org.cryptomator.hub.memberkey").generate();
    }

    private static ECKey recoveryKey() throws Exception {
        return new ECKeyGenerator(Curve.P_384).keyID("org.cryptomator.hub.recoverykey").generate();
    }

    @Test
    public void testCreateAndUnlock() throws Exception {
        final OctetSequenceKey memberKey = memberKey();
        final UvfUnlockedVault created = new JWKSetUVFVaultMetadataProvider(new JWKSet(Arrays.asList(memberKey, recoveryKey()))).create();
        final UvfLockedVault locked = (UvfLockedVault) LockedVault.parse(created.serializeConfig());
        // Unlock with member key only
        final UvfUnlockedVault unlocked = new JWKSetUVFVaultMetadataProvider(new JWKSet(memberKey)).unlock(locked);
        assertEquals(created.seedIds(), unlocked.seedIds());
        created.destroy();
        unlocked.destroy();
    }

    @Test
    public void testCreateWithPublicRecoveryKey() throws Exception {
        // Private part of recovery key held by key management service
        final UvfUnlockedVault created = new JWKSetUVFVaultMetadataProvider(new JWKSet(Arrays.asList(memberKey(), recoveryKey().toPublicJWK()))).create();
        assertNotNull(created.serializeConfig());
        created.destroy();
    }

    @Test(expected = VaultKeyInvalidException.class)
    public void testUnlockWrongMemberKey() throws Exception {
        final UvfUnlockedVault created = new JWKSetUVFVaultMetadataProvider(new JWKSet(Arrays.asList(memberKey(), recoveryKey()))).create();
        final UvfLockedVault locked = (UvfLockedVault) LockedVault.parse(created.serializeConfig());
        created.destroy();
        new JWKSetUVFVaultMetadataProvider(new JWKSet(memberKey())).unlock(locked);
    }

    @Test(expected = VaultException.class)
    public void testCreateMissingRecoveryKey() throws Exception {
        new JWKSetUVFVaultMetadataProvider(new JWKSet(memberKey())).create();
    }

    @Test(expected = VaultException.class)
    public void testCreateMissingMemberKey() throws Exception {
        new JWKSetUVFVaultMetadataProvider(new JWKSet(recoveryKey())).create();
    }

    @Test(expected = VaultException.class)
    public void testCreateInvalidMemberKey() throws Exception {
        new JWKSetUVFVaultMetadataProvider(new JWKSet(Arrays.asList(new OctetSequenceKeyGenerator(128).generate(), recoveryKey()))).create();
    }

    @Test(expected = VaultException.class)
    public void testUnlockInvalidMemberKey() throws Exception {
        final UvfUnlockedVault created = new JWKSetUVFVaultMetadataProvider(new JWKSet(Arrays.asList(memberKey(), recoveryKey()))).create();
        final UvfLockedVault locked = (UvfLockedVault) LockedVault.parse(created.serializeConfig());
        created.destroy();
        new JWKSetUVFVaultMetadataProvider(new JWKSet(new OctetSequenceKeyGenerator(128).generate())).unlock(locked);
    }

    @Test(expected = VaultException.class)
    public void testCreateInvalidRecoveryKeyCurve() throws Exception {
        new JWKSetUVFVaultMetadataProvider(new JWKSet(Arrays.asList(memberKey(), new ECKeyGenerator(Curve.P_256).generate()))).create();
    }
}
