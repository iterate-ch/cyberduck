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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.impl.v8.MasterkeyVaultMetadataProvider;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPReadFeature;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertArrayEquals;

public class SFTPCryptomatorInteroperabilityTest {

    private final int PORT_NUMBER = ThreadLocalRandom.current().nextInt(2000, 3000);

    private static SshServer server;
    private CryptoFileSystem cryptoFileSystem;
    private java.nio.file.Path tempDir;
    private String passphrase;

    @Before
    public void startSerer() throws Exception {
        server = SshServer.setUpDefaultServer();
        server.setPort(PORT_NUMBER);
        server.setPasswordAuthenticator((username, password, session) -> true);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        tempDir = Files.createTempDirectory(String.format("%s-", this.getClass().getName()));

        unzipTestVault("/testvault.zip", tempDir.toString());

        //TODO switch back to cryptofs based testing as soon as cryptofs is based on new cryptolib
        /*
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
        final PerpetualMasterkey mk = Masterkey.generate(csprng);
        final MasterkeyFileAccess mkAccess = new MasterkeyFileAccess(PreferencesFactory.get().getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8), csprng);
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
        */
        server.setFileSystemFactory(new VirtualFileSystemFactory(tempDir.toAbsolutePath()));
        server.start();
    }

    @After
    public void stop() throws Exception {
        server.stop();
        FileUtils.deleteDirectory(tempDir.toFile());
        /*
        cryptoFileSystem.close();
        FileUtils.deleteDirectory(cryptoFileSystem.getPathToVault().getParent().toFile());
         */
    }

    private void unzipTestVault(final String zip, final String target) throws Exception {
        try(InputStream is = this.getClass().getResourceAsStream(zip);
            ZipInputStream zipIn = new ZipInputStream(is)) {

            java.nio.file.Path targetDir = Paths.get(target);
            Files.createDirectories(targetDir);

            ZipEntry entry;
            while((entry = zipIn.getNextEntry()) != null) {
                java.nio.file.Path filePath = targetDir.resolve(entry.getName());
                System.out.println(filePath.toString());

                if(entry.isDirectory()) {
                    Files.createDirectories(filePath);
                }
                else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipIn.closeEntry();
            }
        }
    }

    /**
     * Create file/folder with Cryptomator, read with Cyberduck
     */
    @Ignore(value = "Need a Cryptofs version that is based on the new Cryptolib")
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
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        session.login(LoginCallback.noop, CancelCallback.noop);
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vaultPath = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final AbstractVault cryptomator = new CryptomatorVault(vaultPath);
        cryptomator.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials("12341234")));
        session.withRegistry(new DefaultVaultRegistry(PasswordCallback.noop, cryptomator));
        Path p = new Path(new Path(vaultPath, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = new CryptoReadFeature(session, new SFTPReadFeature(session), cryptomator).read(p, new TransferStatus(), ConnectionCallback.noop);
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);
    }

    /**
     * Read Cryptomator generated vault with long file and folder names
     */
    @Test
    public void testCryptomatorInteroperabilityLongFileAndFoldername() throws Exception {


        // read with Cyberduck and compare
        final Host host = new Host(new SFTPProtocol(), "localhost", PORT_NUMBER, new Credentials("empty", "empty"));
        final SFTPSession session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        session.login(LoginCallback.noop, CancelCallback.noop);
        final Path vaultPath = new SFTPHomeDirectoryService(session).find();
        final AbstractVault cryptomator = new CryptomatorVault(vaultPath);
        cryptomator.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials("12341234")));
        session.withRegistry(new DefaultVaultRegistry(PasswordCallback.noop, cryptomator));

/*
        Path p = new Path(new Path(vaultPath, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = new CryptoReadFeature(session, new SFTPReadFeature(session), cryptomator).read(p, new TransferStatus(), ConnectionCallback.noop);
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);*/
    }


    /**
     * Create long file/folder with Cryptomator, read with Cyberduck
     */
    @Ignore(value = "Need a Cryptofs version that is based on the new Cryptolib")
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
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        session.login(LoginCallback.noop, CancelCallback.noop);
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vaultPath = new Path(home, "vault", EnumSet.of(Path.Type.directory));
        final AbstractVault cryptomator = new CryptomatorVault(vaultPath);
        cryptomator.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials("12341234")));
        session.withRegistry(new DefaultVaultRegistry(PasswordCallback.noop, cryptomator));
        Path p = new Path(new Path(vaultPath, targetFolder.getFileName().toString(), EnumSet.of(Path.Type.directory)), targetFile.getFileName().toString(), EnumSet.of(Path.Type.file));
        final InputStream read = new CryptoReadFeature(session, new SFTPReadFeature(session), cryptomator).read(p, new TransferStatus(), ConnectionCallback.noop);
        final byte[] readContent = new byte[content.length];
        IOUtils.readFully(read, readContent);
        assertArrayEquals(content, readContent);
    }
}
