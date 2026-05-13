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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.features.CryptoChecksumCompute;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class CryptoChecksumComputeTest extends AbstractCryptoTests {

    @Test
    public void testCompute() throws Exception {
        final Path vault = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {

            private final Map<String, byte[]> fileStore = new HashMap<>();

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
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            if(fileStore.containsKey(file.getAbsolute())) {
                                return new ByteArrayInputStream(fileStore.get(file.getAbsolute()));
                            }
                            throw new NotfoundException(file.getAbsolute());
                        }
                    };
                }
                if(type == Write.class) {
                    return (T) new Write<Void>() {

                        @Override
                        public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                            return new StatusOutputStream<Void>(buffer) {
                                @Override
                                public void close() throws IOException {
                                    super.close();
                                    fileStore.put(file.getAbsolute(), buffer.toByteArray());
                                }

                                @Override
                                public Void getStatus() {
                                    return null;
                                }
                            };
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final DefaultVaultProvider provider = new DefaultVaultProvider(session);
        provider.create(session, null, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        final AbstractVault cryptomator = provider.load(session, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
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
