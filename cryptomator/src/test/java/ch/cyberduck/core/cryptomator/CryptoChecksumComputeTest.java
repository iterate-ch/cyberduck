package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.features.CryptoChecksumCompute;
import ch.cyberduck.core.cryptomator.impl.v8.CryptoVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class CryptoChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        final Path vault = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Directory.class) {
                    return (T) new Directory() {

                        @Override
                        public Path mkdir(final Write writer, final Path folder, final TransferStatus status) {
                            assertTrue(folder.equals(vault) || folder.isChild(vault));
                            return folder;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"));
        final ByteBuffer header = cryptomator.getFileHeaderCryptor().encryptHeader(cryptomator.getFileHeaderCryptor().create());
        // DEFAULT_PIPE_SIZE=1024
        final SHA256ChecksumCompute sha = new SHA256ChecksumCompute();
        final CryptoChecksumCompute compute = new CryptoChecksumCompute(sha, cryptomator);
        final RandomNonceGenerator nonces = new RandomNonceGenerator(cryptomator.getNonceSize());
        assertNotNull(compute.compute(new NullInputStream(1025L), new TransferStatus().setLength(1025L).setHeader(header).setNonces(nonces)).hash);
        assertNotEquals(compute.compute(new NullInputStream(1025L), new TransferStatus().setLength(1025L).setHeader(header).setNonces(nonces)),
                compute.compute(new NullInputStream(1025L), new TransferStatus().setLength(1025L).setHeader(header).setNonces(nonces)));
        assertNotNull(compute.compute(new NullInputStream(0L), new TransferStatus().setLength(0L).setHeader(header).setNonces(nonces)).hash);
        assertEquals(compute.compute(new NullInputStream(0L), new TransferStatus().setHeader(header).setNonces(nonces)),
                compute.compute(new NullInputStream(0L), new TransferStatus().setHeader(header).setNonces(nonces)));
        assertNotEquals(compute.compute(new NullInputStream(0L), new TransferStatus().setHeader(header).setNonces(nonces)),
                sha.compute(new NullInputStream(0L), new TransferStatus()));
    }
}
