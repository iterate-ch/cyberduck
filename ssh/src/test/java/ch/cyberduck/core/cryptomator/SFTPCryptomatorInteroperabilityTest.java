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
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.cryptomator.cryptofs.CryptoFileSystem;
import org.cryptomator.cryptofs.CryptoFileSystemProperties;
import org.cryptomator.cryptofs.CryptoFileSystemProvider;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.Masterkey;
import org.cryptomator.cryptolib.api.MasterkeyLoader;
import org.cryptomator.cryptolib.api.MasterkeyLoadingFailedException;
import org.cryptomator.cryptolib.common.MasterkeyFileAccess;
import org.cryptomator.cryptolib.common.ReseedingSecureRandom;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

import static org.cryptomator.cryptofs.CryptoFileSystemProperties.cryptoFileSystemProperties;
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
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        final java.nio.file.Path tempDir = Files.createTempDirectory(String.format("%s-", this.getClass().getName()));
        final java.nio.file.Path vault = tempDir.resolve("vault");
        Files.createDirectory(vault);
        passphrase = new AlphanumericRandomStringService().random();
        final SecureRandom csprng;
        switch(Factory.Platform.getDefault()) {
            case windows:
                csprng = ReseedingSecureRandom.create(SecureRandom.getInstanceStrong());
                break;
            default:
                csprng = FastSecureRandomProvider.get().provide();
        }
        final Masterkey mk = Masterkey.generate(csprng);
        final MasterkeyFileAccess mkAccess = new MasterkeyFileAccess(CryptoVault.VAULT_PEPPER, csprng);
        final java.nio.file.Path mkPath = Paths.get(vault.toString(), DefaultVaultRegistry.DEFAULT_MASTERKEY_FILE_NAME);
        mkAccess.persist(mk, mkPath, passphrase);
        CryptoFileSystemProperties properties = cryptoFileSystemProperties().withKeyLoader(new MasterkeyLoader() {
                    @Override
                    public Masterkey loadKey(final URI keyId) throws MasterkeyLoadingFailedException {
                        return mkAccess.load(mkPath, passphrase);
                    }
                })
                .withCipherCombo(CryptorProvider.Scheme.SIV_CTRMAC)
                .build();
        CryptoFileSystemProvider.initialize(vault, properties, URI.create("test:key"));
        cryptoFileSystem = CryptoFileSystemProvider.newFileSystem(vault, properties);
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
    @Test(expected = CryptoInvalidFilenameException.class)
    public void testCryptomatorInteroperabilityLongFilename() throws Exception {
        // create folder
        final java.nio.file.Path targetFolder = cryptoFileSystem.getPath("/", new AlphanumericRandomStringService().random());
        Files.createDirectory(targetFolder);
        // create file and write some random content
        java.nio.file.Path targetFile = targetFolder.resolve(new RandomStringGenerator.Builder().build().generate(220));
        final byte[] content = RandomUtils.nextBytes(48768);
        Files.write(targetFile, content);

        // read with Cyberduck and compare
        final Host host = new Host(new SFTPProtocol(), "localhost", PORT_NUMBER, new Credentials("empty", "empty"));
        final SFTPSession session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault).load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(passphrase);
            }
        });
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
    public void testCryptomatorInteroperability() throws Exception {
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
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault).load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(passphrase);
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        Path p = new Path(new Path(vault, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = new CryptoReadFeature(session, new SFTPReadFeature(session), cryptomator).read(p, new TransferStatus(), new DisabledConnectionCallback());
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);
    }
}
