package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.CryptoPathMapper;
import ch.cyberduck.core.cryptomator.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.CryptoSessionListWorker;
import ch.cyberduck.core.cryptomator.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.DirectoryIdProvider;
import ch.cyberduck.core.cryptomator.LongFileNameProvider;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.SessionListWorker;
import ch.cyberduck.test.IntegrationTest;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.KeyFile;
import org.cryptomator.cryptolib.v1.Version1CryptorModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SFTPCryptomatorListServiceTest {

    private CryptorProvider cryptorProvider;
    private Cryptor cryptor;

    @Before
    public void setup() {
        cryptorProvider = new Version1CryptorModule().provideCryptorProvider(new SecureRandom());
        final String masterKey = "{\"version\":5,\"scryptSalt\":\"JdjFoskbyIE=\",\"scryptCostParam\":16384,\"scryptBlockSize\":8,"
                + "\"primaryMasterKey\":\"h+5DIMCFiMTa1lBbd/i4jsORzQXe5YcqUME5Cmza4raqBpFQ+lkqaQ==\","
                + "\"hmacMasterKey\":\"qSdfm+JwGLfapvNrqmqo32WVS8idB76nPLxo611DIfdgCFxGbrAlZQ==\","
                + "\"versionMac\":\"ALE/39EGv6oLi5/LPtTVVTxPuzrmtRqUJGzMZJ5zyIc=\"}";
        final KeyFile keyFile = KeyFile.parse(masterKey.getBytes());
        cryptor = cryptorProvider.createFromKeyFile(keyFile, "coke4you", 5);
        Assert.assertNotNull(cryptor);
    }

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final PathCache cache = new PathCache(1);
        final Path vaultRoot = new Path(home, "/cryptomator-vault/test", EnumSet.of(Path.Type.directory));
        final LongFileNameProvider longFileNameProvider = new LongFileNameProvider(vaultRoot, session);
        final DirectoryIdProvider directoryIdProvider = new DirectoryIdProvider(session);
        final CryptoPathMapper cryptoPathMapper = new CryptoPathMapper(vaultRoot, cryptor, longFileNameProvider, directoryIdProvider);
        final SessionListWorker worker = new CryptoSessionListWorker(cache,
                new Path(home, "/cryptomator-vault/test", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener(),
                cryptor, cryptoPathMapper, longFileNameProvider);
        final AttributedList<Path> list = worker.run(session);
        for(Path path : list) {
            System.out.println(path.getAbsolute());
        }
        session.close();
    }

    @Test
    public void testRead() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vaultRoot = new Path(home, "/cryptomator-vault/test", EnumSet.of(Path.Type.directory));
        final LongFileNameProvider longFileNameProvider = new LongFileNameProvider(vaultRoot, session);
        final DirectoryIdProvider directoryIdProvider = new DirectoryIdProvider(session);
        final CryptoPathMapper cryptoPathMapper = new CryptoPathMapper(vaultRoot, cryptor, longFileNameProvider, directoryIdProvider);

        final Path test = new Path(vaultRoot, "BinScope-DESKTOP-752QTEV-16_11_21_18_02_40.html", EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final SFTPReadFeature readFeature = new SFTPReadFeature(session);
        final InputStream stream = new CryptoReadFeature(readFeature, cryptor, cryptoPathMapper).read(test, status);

        OutputStream os = new FileOutputStream("/Users/yla/Downloads/" + test.getName());
        new StreamCopier(status, status).transfer(stream, os);
        os.close();

        session.close();
    }

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vaultRoot = new Path(home, "/cryptomator-vault/test", EnumSet.of(Path.Type.directory));
        final LongFileNameProvider longFileNameProvider = new LongFileNameProvider(vaultRoot, session);
        final DirectoryIdProvider directoryIdProvider = new DirectoryIdProvider(session);
        final CryptoPathMapper cryptoPathMapper = new CryptoPathMapper(vaultRoot, cryptor, longFileNameProvider, directoryIdProvider);


        final Path test = new Path(vaultRoot, "BinScope-" + UUID.randomUUID().toString() + ".html", EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final SFTPWriteFeature writeFeature = new SFTPWriteFeature(session);
        final OutputStream out = new CryptoWriteFeature(writeFeature, cryptor, cryptoPathMapper).write(test, status);
        final FileInputStream content = new FileInputStream("/Users/yla/Downloads/BinScope-DESKTOP-752QTEV-16_11_21_18_02_40.html");
        new StreamCopier(status, status).transfer(content, out);

        session.close();
    }
}