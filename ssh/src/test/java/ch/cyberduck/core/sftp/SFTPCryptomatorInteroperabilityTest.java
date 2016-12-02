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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.pool.SingleSessionPool;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.cryptomator.cryptofs.CryptoFileSystem;
import org.cryptomator.cryptofs.CryptoFileSystemProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

import static org.cryptomator.cryptofs.CryptoFileSystemProperties.cryptoFileSystemProperties;
import static org.cryptomator.cryptofs.CryptoFileSystemUris.createUri;
import static org.junit.Assert.assertArrayEquals;

public class SFTPCryptomatorInteroperabilityTest {

    private static int PORT_NUMBER = ThreadLocalRandom.current().nextInt(2000, 3000);

    private static SshServer server;
    private CryptoFileSystem cryptoFileSystem;
    private String passphrase;
    private Path vault;

    @Before
    public void start() throws Exception {
        createCryptoFileSystem();

        server = SshServer.setUpDefaultServer();
        server.setPort(PORT_NUMBER);
        server.setPasswordAuthenticator((username, password, session) -> true);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystem.Factory()));
        server.setFileSystemFactory(new VirtualFileSystemFactory(cryptoFileSystem.getPathToVault().getParent().toAbsolutePath().toString()));
        server.start();
    }

    @After
    public void stop() throws Exception {
        server.stop();
        cryptoFileSystem.close();
        FileUtils.deleteDirectory(cryptoFileSystem.getPathToVault().getParent().toFile());
    }

    private void createCryptoFileSystem() throws Exception {
        final java.nio.file.Path tempDir = Files.createTempDirectory("RealFileSystemIntegrationTest");
        final java.nio.file.Path vault = tempDir.resolve("vault");
        passphrase = RandomStringUtils.randomAlphanumeric(25);
        cryptoFileSystem = new CryptoFileSystemProvider().newFileSystem(createUri(vault), cryptoFileSystemProperties().withPassphrase(passphrase).build());
    }

    private Session<?> loadRemoteVault() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "localhost", PORT_NUMBER, new Credentials("empty", "empty"));
        final SingleSessionPool pool = new SingleSessionPool(new LoginConnectionService(
                new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()
        ), new SFTPSession(host), PathCache.empty());
        final Session<?> session = pool.borrow(BackgroundActionState.running);
        final Path home = session.getFeature(Home.class).find();
        vault = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore(), new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword(passphrase);
            }
        }).load(session);
        session.withVault(cryptomator);
        return session;
    }

    /**
     *   Create file/folder with Cryptomator, read with Cyberduck
     */
    @Test
    public void testCryptomatorInteroperabilityTests() throws Exception {
        // create folder
        final java.nio.file.Path targetFolder = cryptoFileSystem.getPath("/" + RandomStringUtils.random(100));
        Files.createDirectory(targetFolder);
        // create file and write some random content
        java.nio.file.Path targetFile = targetFolder.resolve(RandomStringUtils.random(100));
        final byte[] content = RandomUtils.nextBytes(48768);
        Files.write(targetFile, content);

        // read with Cyberduck and compare
        Session<?> session = this.loadRemoteVault();
        Path p = new Path(new Path(vault, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = session.getFeature(Read.class).read(p, new TransferStatus());
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);
        session.close();
    }
}
