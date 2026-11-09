package ch.cyberduck.core.vault.registry;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultUnlockCancelException;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class VaultRegistryUploadFeatureTest {

    private static final Upload<Void> proxy = new Upload<Void>() {
        @Override
        public Void upload(final Write<Void> write, final Path file, final Local local, final BandwidthThrottle throttle, final ProgressListener progress, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) {
            return null;
        }

        @Override
        public ChecksumCompute checksum(final Path file, final TransferStatus status) {
            return ChecksumComputeFactory.get(HashAlgorithm.md5);
        }

        @Override
        public EnumSet<Flags> features(final Path file, final TransferStatus status) {
            return EnumSet.of(Flags.checksumRequired);
        }
    };

    @Test
    public void testChecksumDisabledVault() {
        final Session<?> session = new NullSession(new Host(new TestProtocol()));
        final VaultRegistryUploadFeature<Void> feature = new VaultRegistryUploadFeature<>(session, proxy, new VaultRegistry.DisabledVaultRegistry());
        final Path file = new Path("/f", EnumSet.of(Path.Type.file));
        assertEquals(HashAlgorithm.md5, feature.checksum(file, new TransferStatus()).getAlgorithm());
    }

    @Test
    public void testChecksumVaultLocked() {
        final Session<?> session = new NullSession(new Host(new TestProtocol()));
        final VaultRegistry registry = new VaultRegistry.DisabledVaultRegistry() {
            @Override
            public Vault find(final Session<?> session, final Path file, final boolean autoload) throws VaultUnlockCancelException {
                throw new VaultUnlockCancelException(file);
            }
        };
        final VaultRegistryUploadFeature<Void> feature = new VaultRegistryUploadFeature<>(session, proxy, registry);
        assertEquals(HashAlgorithm.md5, feature.checksum(new Path("/f", EnumSet.of(Path.Type.file)), new TransferStatus()).getAlgorithm());
    }
}
