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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVListService;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVReadFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DAVReadFeatureTest {

    @Test
    public void testReadRange() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final TransferStatus status = new TransferStatus();
        final int length = 140000;
        final byte[] content = RandomUtils.nextBytes(length);
        status.setLength(content.length);
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final CryptoWriteFeature<String> writer = new CryptoWriteFeature<>(session, new DAVWriteFeature(session), cryptomator);
        final Cryptor cryptor = cryptomator.getCryptor();
        final FileHeader header = cryptor.fileHeaderCryptor().create();
        status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RandomNonceGenerator());
        status.setChecksum(writer.checksum(test).compute(new ByteArrayInputStream(content), status));
        final OutputStream out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(new CryptoFindFeature(session, new DAVFindFeature(session), cryptomator).find(test));
        Assert.assertEquals(content.length, new CryptoListService(session, new DAVListService(session), cryptomator).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        Assert.assertEquals(content.length, writer.append(test, status.getLength(), PathCache.empty()).size, 0L);
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(40000);
            final TransferStatus read = new TransferStatus();
            read.setOffset(23); // offset within chunk
            read.setAppend(true);
            read.length(40000); // ensure to read at least two chunks
            final InputStream in = new CryptoReadFeature(session, new DAVReadFeature(session), cryptomator).read(test, read, new DisabledConnectionCallback());
            new StreamCopier(read, read).withLimit(40000L).transfer(in, buffer);
            final byte[] reference = new byte[40000];
            System.arraycopy(content, 23, reference, 0, reference.length);
            assertArrayEquals(reference, buffer.toByteArray());
        }
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(40000);
            final TransferStatus read = new TransferStatus();
            read.setOffset(65536); // offset at the beginning of a new chunk
            read.setAppend(true);
            read.length(40000); // ensure to read at least two chunks
            final InputStream in = new CryptoReadFeature(session, new DAVReadFeature(session), cryptomator).read(test, read, new DisabledConnectionCallback());
            new StreamCopier(read, read).withLimit(40000L).transfer(in, buffer);
            final byte[] reference = new byte[40000];
            System.arraycopy(content, 65536, reference, 0, reference.length);
            assertArrayEquals(reference, buffer.toByteArray());
        }
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(40000);
            final TransferStatus read = new TransferStatus();
            read.setOffset(65537); // offset at the beginning+1 of a new chunk
            read.setAppend(true);
            read.length(40000); // ensure to read at least two chunks
            final InputStream in = new CryptoReadFeature(session, new DAVReadFeature(session), cryptomator).read(test, read, new DisabledConnectionCallback());
            new StreamCopier(read, read).withLimit(40000L).transfer(in, buffer);
            final byte[] reference = new byte[40000];
            System.arraycopy(content, 65537, reference, 0, reference.length);
            assertArrayEquals(reference, buffer.toByteArray());
        }
        new CryptoDeleteFeature(session, new DAVDeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
