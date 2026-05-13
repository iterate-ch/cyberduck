package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.openstack.AbstractSwiftTest;
import ch.cyberduck.core.openstack.SwiftDeleteFeature;
import ch.cyberduck.core.openstack.SwiftFindFeature;
import ch.cyberduck.core.openstack.SwiftRegionService;
import ch.cyberduck.core.openstack.SwiftTouchFeature;
import ch.cyberduck.core.openstack.SwiftWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultVersion;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class SwiftTouchFeatureTest extends AbstractSwiftTest {

    @Test
    public void testTouchEncrypted() throws Exception {
        final Path home = new Path("/test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final DefaultVaultProvider provider = new DefaultVaultProvider(session);
        provider.create(session, null, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        final AbstractVault cryptomator = provider.load(session, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final TransferStatus status = new TransferStatus();
        final Path test = new CryptoTouchFeature<>(session, new SwiftTouchFeature(session, regionService), cryptomator).touch(
                new CryptoWriteFeature<>(session, new SwiftWriteFeature(session, new SwiftRegionService(session)), cryptomator), new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file)), status);
        assertEquals(0L, test.attributes().getSize());
        assertEquals(0L, status.getResponse().getSize());
        assertTrue(cryptomator.getFeature(session, Find.class, new SwiftFindFeature(session)).find(test));
        cryptomator.getFeature(session, Delete.class, new SwiftDeleteFeature(session)).delete(Arrays.asList(test, vault), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
