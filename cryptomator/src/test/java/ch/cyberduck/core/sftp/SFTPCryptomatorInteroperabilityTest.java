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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.cryptomator.cryptofs.CryptoFileSystem;
import org.cryptomator.cryptofs.CryptoFileSystemProperties;
import org.cryptomator.cryptofs.CryptoFileSystemProvider;
import org.cryptomator.cryptofs.CryptoFileSystemUris;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertArrayEquals;

public class SFTPCryptomatorInteroperabilityTest {

    private static int PORT_NUMBER = ThreadLocalRandom.current().nextInt(2000, 3000);

    private static SshServer server;
    private CryptoFileSystem cryptoFileSystem;
    private String passphrase;

    @Before
    public void startSerer() throws Exception {
        server = SshServer.setUpDefaultServer();
        server.setPort(PORT_NUMBER);
        server.setPasswordAuthenticator((username, password, session) -> true);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystem.Factory()));
        final java.nio.file.Path tempDir = Files.createTempDirectory(String.format("%s-", this.getClass().getName()));
        final java.nio.file.Path vault = tempDir.resolve("vault");
        passphrase = RandomStringUtils.randomAlphanumeric(25);
        cryptoFileSystem = new CryptoFileSystemProvider().newFileSystem(CryptoFileSystemUris.createUri(vault), CryptoFileSystemProperties.cryptoFileSystemProperties().withPassphrase(passphrase).build());
        server.setFileSystemFactory(new VirtualFileSystemFactory(cryptoFileSystem.getPathToVault().getParent().toAbsolutePath().toString()));
        server.start();
    }

    @After
    public void stop() throws Exception {
        server.stop();
        cryptoFileSystem.close();
        FileUtils.deleteDirectory(cryptoFileSystem.getPathToVault().getParent().toFile());
    }

    /**
     * Create file/folder with Cryptomator, read with Cyberduck
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
        final Host host = new Host(new SFTPProtocol(), "localhost", PORT_NUMBER, new Credentials("empty", "empty"));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).load(session, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword(passphrase);
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        Path p = new Path(new Path(vault, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = new CryptoReadFeature(session, new SFTPReadFeature(session), cryptomator).read(p, new TransferStatus(), new DisabledConnectionCallback());
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);
        session.close();
    }

    /**
     * Create long file/folder with Cryptomator, read with Cyberduck
     */
    @Test
    public void testCryptomatorInteroperability_longNames_Tests() throws Exception {
        // create folder
        final java.nio.file.Path targetFolder = cryptoFileSystem.getPath("/" + RandomStringUtils.random(180));
        Files.createDirectory(targetFolder);
        // create file and write some random content
        java.nio.file.Path targetFile = targetFolder.resolve(RandomStringUtils.random(180));
        final byte[] content = RandomUtils.nextBytes(20);
        Files.write(targetFile, content);

        // read with Cyberduck and compare
        final Host host = new Host(new SFTPProtocol(), "localhost", PORT_NUMBER, new Credentials("empty", "empty"));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).load(session, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword(passphrase);
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        Path p = new Path(new Path(vault, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = new CryptoReadFeature(session, new SFTPReadFeature(session), cryptomator).read(p, new TransferStatus(), new DisabledConnectionCallback());
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);
        session.close();
    }
}
