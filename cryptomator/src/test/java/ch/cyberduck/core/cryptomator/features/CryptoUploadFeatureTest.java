package ch.cyberduck.core.cryptomator.features;

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
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class CryptoUploadFeatureTest {

    @Test
    public void testAppendNotSupported() throws Exception {
        final Upload<Void> proxy = new Upload<Void>() {
            @Override
            public Void upload(final Write<Void> write, final Path file, final Local local, final BandwidthThrottle throttle, final ProgressListener progress,
                               final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) {
                fail();
                return null;
            }

            @Override
            public Write.Append append(final Path file, final TransferStatus status) {
                // Existing content of incomplete upload with offset in ciphertext
                fail();
                return new Write.Append(true).withStatus(status).withOffset(10494788L);
            }
        };
        final CryptoUploadFeature<Void> feature = new CryptoUploadFeature<>(new NullSession(new Host(new TestProtocol())), proxy,
                new CryptomatorVault(new Path("/vault", EnumSet.of(Path.Type.directory))));
        final Write.Append append = feature.append(new Path("/vault/f", EnumSet.of(Path.Type.file)), new TransferStatus().setLength(20971520L));
        assertFalse(append.append);
    }
}
