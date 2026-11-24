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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.features.CryptoChecksumCompute;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.impl.uvf.DefaultUVFVaultMetadataProvider;
import ch.cyberduck.core.cryptomator.impl.uvf.UVFVault;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.cryptomator.cryptolib.api.Cvf8UnlockedVault;
import org.cryptomator.cryptolib.api.CvfMasterkey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Checksum and upload of segments with the same features and in the same order as segmented upload services. The
 * checksum calculated before the upload must match the uploaded content.
 */
@RunWith(value = Parameterized.class)
public class CryptoSegmentUploadTest extends AbstractCryptoTests {

    private AbstractVault getVault() throws Exception {
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

    @Test
    public void testSegments() throws Exception {
        final AbstractVault vault = this.getVault();
        final long segmentLength = new TransferStatus().setSegmentAlignment(vault.getCleartextChunkSize()).alignSegment(100000L);
        final long length = 2 * segmentLength + 1000L;
        final byte[] cleartext = RandomUtils.nextBytes((int) length);
        final Path file = new Path("/vault/f", EnumSet.of(Path.Type.file, Path.Type.encrypted));
        // Overall status with header shared by all segments as in S3MultipartUploadService
        final TransferStatus overall = new TransferStatus().setLength(length).setSegmentAlignment(vault.getCleartextChunkSize());
        overall.setHeader(vault.createHeader());
        final List<String> failures = new ArrayList<>();
        long offset = 0L;
        long remaining = length;
        for(int part = 1; remaining > 0; part++) {
            final long partLength = Math.min(overall.alignSegment(segmentLength), remaining);
            // New status for each segment without nonces
            final TransferStatus segment = new TransferStatus().setParent(overall).setSegment(true).setLength(partLength).setOffset(offset);
            segment.setHeader(overall.getHeader());
            // Checksum before upload such as Content-MD5
            final Checksum checksum = new CryptoChecksumCompute(new MD5ChecksumCompute(), vault).compute(new ByteArrayInputStream(cleartext), segment);
            // Upload
            final ByteArrayOutputStream sent = new ByteArrayOutputStream();
            final Write<Void> proxy = new Write<Void>() {
                @Override
                public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
                    return new StatusOutputStream<Void>(sent) {
                        @Override
                        public Void getStatus() {
                            return null;
                        }
                    };
                }
            };
            try(StatusOutputStream<Void> out = new CryptoWriteFeature<>(new NullSession(new Host(new TestProtocol())), proxy, vault)
                    .write(file, segment, new DisabledConnectionCallback())) {
                out.write(cleartext, (int) offset, (int) partLength);
            }
            final Checksum uploaded = new MD5ChecksumCompute().compute(new ByteArrayInputStream(sent.toByteArray()), new TransferStatus());
            if(!checksum.equals(uploaded)) {
                failures.add(String.format("Part %d: checksum %s does not match uploaded content %s", part, checksum.hash, uploaded.hash));
            }
            offset += partLength;
            remaining -= partLength;
        }
        vault.close();
        assertTrue(String.join("\n", failures), failures.isEmpty());
    }
}
