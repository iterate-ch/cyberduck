package ch.cyberduck.core.cryptomator.legacy.features;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.legacy.CryptomatorVault;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CryptoUploadFeatureTest {

    @Test
    public void testSegmentAlignment() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Directory.class) {
                    return (T) new Directory() {

                        @Override
                        public Path mkdir(final Write writer, final Path folder, final TransferStatus status) {
                            assertTrue(folder.equals(home) || folder.isChild(home));
                            return folder;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptomatorVault vault = new CryptomatorVault(home);
        vault.create(session, null, new VaultCredentials("test"), CryptomatorVault.VAULT_VERSION_DEPRECATED);
        final int chunk = vault.getFileContentCryptor().cleartextChunkSize();
        final Upload<Void> proxy = new Upload<Void>() {
            @Override
            public Void upload(final Write<Void> write, final Path file, final Local local, final BandwidthThrottle throttle, final ProgressListener progress,
                               final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) {
                // Recommended part size of B2 not a multiple of the chunk size
                assertEquals(0L, status.alignSegment(100000000L) % chunk);
                assertEquals(100007936L, status.alignSegment(100000000L));
                return null;
            }

            @Override
            public Write.Append append(final Path file, final TransferStatus status) {
                return new Write.Append(false).withStatus(status);
            }
        };
        new CryptoUploadFeature<>(session, proxy, vault).upload(null, new Path(home, "f", EnumSet.of(Path.Type.file)), null, null, null, null,
                new TransferStatus().setLength(200000000L), null);
    }
}
