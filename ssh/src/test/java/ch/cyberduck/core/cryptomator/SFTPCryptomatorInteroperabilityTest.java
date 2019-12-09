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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPReadFeature;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.cryptomator.cryptofs.CryptoFileSystem;
import org.cryptomator.cryptofs.CryptoFileSystemProperties;
import org.cryptomator.cryptofs.CryptoFileSystemProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertArrayEquals;

@Category(IntegrationTest.class)
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
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        final java.nio.file.Path tempDir = Files.createTempDirectory(String.format("%s-", this.getClass().getName()));
        final java.nio.file.Path vault = tempDir.resolve("vault");
        Files.createDirectory(vault);
        passphrase = new AlphanumericRandomStringService().random();
        cryptoFileSystem = CryptoFileSystemProvider.newFileSystem(vault, CryptoFileSystemProperties.cryptoFileSystemProperties().withPassphrase(passphrase).build());
        server.setFileSystemFactory(new VirtualFileSystemFactory(cryptoFileSystem.getPathToVault().getParent().toAbsolutePath()));
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
        final java.nio.file.Path targetFolder = cryptoFileSystem.getPath("/", new AlphanumericRandomStringService().random());
        Files.createDirectory(targetFolder);
        // create file and write some random content
        java.nio.file.Path targetFile = targetFolder.resolve(new RandomStringGenerator.Builder().build().generate(100));
        final byte[] content = RandomUtils.nextBytes(48768);
        Files.write(targetFile, content);

        // read with Cyberduck and compare
        final Host host = new Host(new SFTPProtocol(), "localhost", PORT_NUMBER, new Credentials("empty", "empty"));
        final SFTPSession session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault).load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(passphrase);
            }
        }, new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        Path p = new Path(new Path(vault, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = new CryptoReadFeature(session, new SFTPReadFeature(session), cryptomator).read(p, new TransferStatus(), new DisabledConnectionCallback());
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);
    }

    /**
     * Create long file/folder with Cryptomator, read with Cyberduck
     */
    @Test
    public void testCryptomatorInteroperability_longNames_Tests() throws Exception {
        // create folder
        final java.nio.file.Path targetFolder = cryptoFileSystem.getPath("/", new AlphanumericRandomStringService().random());
        Files.createDirectory(targetFolder);
        // create file and write some random content
        java.nio.file.Path targetFile = targetFolder.resolve(new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(20);
        Files.write(targetFile, content);

        // read with Cyberduck and compare
        final Host host = new Host(new SFTPProtocol(), "localhost", PORT_NUMBER, new Credentials("empty", "empty"));
        final SFTPSession session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault).load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(passphrase);
            }
        }, new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        Path p = new Path(new Path(vault, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = new CryptoReadFeature(session, new SFTPReadFeature(session), cryptomator).read(p, new TransferStatus(), new DisabledConnectionCallback());
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);
    }
}
