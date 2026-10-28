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
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoUploadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.impl.v8.MasterkeyVaultMetadataProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class CryptoWriteFeatureTest extends AbstractCryptoTests {

    @Test
    public void testCiphertextSize_GCM() throws Exception {
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
        final AbstractVault vault = new CryptomatorVault(home);
        vault.create(session, null, new MasterkeyVaultMetadataProvider(new VaultCredentials("test")));
        int headerSize = vault.getFileHeaderCryptor().headerSize();
        // zero file size
        assertEquals(headerSize, vault.toCiphertextSize(0L, 0));
        // one-byte file
        assertEquals(headerSize + 28 + 1, vault.toCiphertextSize(0L, 1));
        // file with chunk size length
        assertEquals(headerSize + vault.getFileContentCryptor().ciphertextChunkSize(), vault.toCiphertextSize(0L, vault.getFileContentCryptor().cleartextChunkSize()));
        // file with chunk size length + 1
        assertEquals(headerSize + vault.getFileContentCryptor().ciphertextChunkSize() + 28 + 1, vault.toCiphertextSize(0L, vault.getFileContentCryptor().cleartextChunkSize() + 1));
        // file with 2 * chunk size length
        assertEquals(headerSize + 2 * vault.getFileContentCryptor().ciphertextChunkSize(), vault.toCiphertextSize(0L, 2 * vault.getFileContentCryptor().cleartextChunkSize()));
        // file with 2 * chunk size length + 100
        assertEquals(headerSize + 2 * vault.getFileContentCryptor().ciphertextChunkSize() + 28 + 100, vault.toCiphertextSize(0L, 2 * vault.getFileContentCryptor().cleartextChunkSize() + 100));
    }

    private final Path vault = new Path("/vault", EnumSet.of(Path.Type.directory));
    private final AtomicReference<byte[]> written = new AtomicReference<>();
    private final Map<String, byte[]> store = new HashMap<>();

    private final Write<Void> proxy = new Write<Void>() {
        @Override
        public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            return new StatusOutputStream<Void>(buffer) {
                @Override
                public void close() throws IOException {
                    super.close();
                    store.put(file.getAbsolute(), buffer.toByteArray());
                    written.set(buffer.toByteArray());
                }

                @Override
                public Void getStatus() {
                    return null;
                }
            };
        }
    };

    private final AtomicReference<Checksum> sent = new AtomicReference<>();

    private final Write<Void> md5proxy = new Write<Void>() {
        @Override
        public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
            // Checksum sent with the request
            sent.set(status.getChecksum());
            return proxy.write(file, status, callback);
        }
    };

    private CryptoUploadFeature<Void> upload(final NullSession session, final AbstractVault cryptomator, final HashAlgorithm algorithm) {
        return new CryptoUploadFeature<>(session, new DefaultUploadFeature<>(EnumSet.noneOf(Upload.Flags.class), algorithm), cryptomator);
    }

    private NullSession session() {
        return new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Directory.class) {
                    return (T) new Directory() {
                        @Override
                        public Path mkdir(final Write writer, final Path folder, final TransferStatus status) {
                            return folder;
                        }
                    };
                }
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            if(store.containsKey(file.getAbsolute())) {
                                return new ByteArrayInputStream(store.get(file.getAbsolute()));
                            }
                            throw new NotfoundException(file.getAbsolute());
                        }
                    };
                }
                if(type == Write.class) {
                    return (T) proxy;
                }
                if(type == Upload.class) {
                    return (T) new DefaultUploadFeature<>(EnumSet.noneOf(Upload.Flags.class), HashAlgorithm.md5);
                }
                return super._getFeature(type);
            }
        };
    }

    private CryptoWriteFeature<Void> feature() throws BackgroundException {
        final NullSession session = this.session();
        final DefaultVaultProvider provider = new DefaultVaultProvider(session);
        provider.create(session, null, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        final AbstractVault cryptomator = provider.load(session, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        return new CryptoWriteFeature<>(session, proxy, cryptomator);
    }

    private void write(final CryptoWriteFeature<Void> feature, final TransferStatus status, final byte[] content) throws Exception {
        final OutputStream out = feature.write(new Path(vault, "f", EnumSet.of(Path.Type.file)), status, ConnectionCallback.noop);
        out.write(content);
        out.close();
        // Test double close
        out.close();
    }

    @Test
    public void testDigestOfEncryptedContent() throws Exception {
        final byte[] content = RandomUtils.nextBytes(2500);
        final TransferStatus status = new TransferStatus().setVerify(HashAlgorithm.sha256).setLength(content.length);
        this.write(this.feature(), status, content);
        // Checksum of the content sent to the server and not of the local content
        assertEquals(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(written.get()), new TransferStatus()), status.getDigest());
        assertNotEquals(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()), status.getDigest());
    }

    @Test
    public void testNoDigestWithoutVerify() throws Exception {
        final byte[] content = RandomUtils.nextBytes(2500);
        final TransferStatus status = new TransferStatus().setLength(content.length);
        this.write(this.feature(), status, content);
        assertEquals(Checksum.NONE, status.getDigest());
    }

    @Test
    public void testDigestNotAppliedToLocalContent() throws Exception {
        final NullSession session = this.session();
        final DefaultVaultProvider provider = new DefaultVaultProvider(session);
        provider.create(session, null, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        final AbstractVault cryptomator = provider.load(session, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        final Path file = new Path(vault, "f", EnumSet.of(Path.Type.file));
        final ChecksumCompute compute = this.upload(session, cryptomator, HashAlgorithm.sha256).checksum(file, new TransferStatus());
        assertTrue(compute.isTransformed());
        assertEquals("SHA-256", compute.digest().getAlgorithm());
    }

    @Test
    public void testPrecalculatedChecksumMatchesWrittenContent() throws Exception {
        for(int length : new int[]{0, 1, 100, 32768, 32769, 100000, 5242880}) {
            final NullSession session = this.session();
            final DefaultVaultProvider provider = new DefaultVaultProvider(session);
            provider.create(session, null, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
            final AbstractVault cryptomator = provider.load(session, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
            final CryptoWriteFeature<Void> feature = new CryptoWriteFeature<>(session, md5proxy, cryptomator);
            final byte[] content = RandomUtils.nextBytes(length);
            final Path file = new Path(vault, "f", EnumSet.of(Path.Type.file));
            final TransferStatus status = new TransferStatus().setLength(content.length);
            // Calculated prior to the transfer as in the upload filter
            status.setChecksum(this.upload(session, cryptomator, HashAlgorithm.md5).checksum(file, status).compute(new ByteArrayInputStream(content), status));
            final OutputStream out = feature.write(file, status, ConnectionCallback.noop);
            out.write(content);
            out.close();
            assertEquals(String.format("Length %d", length),
                    ChecksumComputeFactory.get(HashAlgorithm.md5).compute(new ByteArrayInputStream(written.get()), new TransferStatus()).hex, status.getChecksum().hex);
        }
    }

    @Test
    public void testTouchChecksumMatchesWrittenContent() throws Exception {
        final NullSession session = this.session();
        final DefaultVaultProvider provider = new DefaultVaultProvider(session);
        provider.create(session, null, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        final AbstractVault cryptomator = provider.load(session, vault, new VaultVersion(vaultVersion), new VaultCredentials("test"));
        final CryptoWriteFeature<Void> writer = new CryptoWriteFeature<>(session, md5proxy, cryptomator);
        final Path file = new Path(vault, "t", EnumSet.of(Path.Type.file));
        // Checksum set in the status as in the touch worker
        final TransferStatus status = new TransferStatus();
        status.setChecksum(this.upload(session, cryptomator, HashAlgorithm.md5).checksum(file, status).compute(new NullInputStream(0L), status));
        sent.set(null);
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(session), cryptomator).touch(writer, file, status);
        assertEquals(ChecksumComputeFactory.get(HashAlgorithm.md5).compute(new ByteArrayInputStream(written.get()), new TransferStatus()).hex, sent.get().hex);
    }
}
