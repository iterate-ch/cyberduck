package ch.cyberduck.core.cryptomator.impl.uvf;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryUVFProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultMetadataProvider;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.DirectoryMetadata;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;
import org.cryptomator.cryptolib.api.RevolvingMasterkey;
import org.cryptomator.cryptolib.api.UVFMasterkey;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.auto.service.AutoService;

@AutoService(Vault.class)
public class UVFVault extends AbstractVault {
    private static final Logger log = LogManager.getLogger(UVFVault.class);

    private static final String REGULAR_FILE_EXTENSION = ".uvf";
    private static final String FILENAME_DIRECTORYID = "dir";
    private static final String DIRECTORY_METADATA_FILENAME = String.format("%s%s", FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);
    private static final String BACKUP_FILENAME_DIRECTORYID = FILENAME_DIRECTORYID;
    private static final String BACKUP_DIRECTORY_METADATA_FILENAME = String.format("%s%s", BACKUP_FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^([A-Za-z0-9_=-]+)" + REGULAR_FILE_EXTENSION);

    /**
     * Root of vault directory
     */
    private final Path home;
    private RevolvingMasterkey masterKey;
    private final Path masterkeyPath;

    private Cryptor cryptor;
    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    private int nonceSize;

    public UVFVault(final Path home) {
        this.home = home;
        this.masterkeyPath = new Path(home, PreferencesFactory.get().getProperty("cryptomator.vault.config.filename.uvf"),
                EnumSet.of(Path.Type.file, Path.Type.vaultmetadata));
    }

    @Override
    public void create(final Session<?> session, final String region, final VaultMetadataProvider metadata) throws BackgroundException {
        // Generic vault creation with provided metadata
        try(final UVFVaultMetadataProvider provider = UVFVaultMetadataProvider.cast(metadata)) {
            final byte[] rootDirectoryMetadata = provider.computeRootDirUvf();
            final String rootDirectoryIdHash = provider.computeRootDirIdHash();
            this.uploadTemplate(session, region, provider.encrypt(), rootDirectoryMetadata, rootDirectoryIdHash);
        }
    }

    /**
     * Uploads a template to create the vault's root directory with associated metadata and directory structure.
     *
     * @param session               The session used for the interaction with the backend.
     * @param region                The region identifier used to associate the uploaded data.
     * @param vaultMetadata         Metadata as JWE of the vault to be uploaded
     * @param rootDirectoryMetadata The encrypted directory metadata file, represented as a byte array.
     * @param rootDirectoryIdHash   The hash of the root directory ID used to create the directory structure.
     * @throws BackgroundException If an error occurs during the upload process.
     */
    private void uploadTemplate(final Session<?> session, final String region,
                                final String vaultMetadata, final byte[] rootDirectoryMetadata, final String rootDirectoryIdHash) throws BackgroundException {

        log.debug("Create vault root directory at {}", home);
        // Obtain non encrypted directory writer
        final Directory<?> directory = session._getFeature(Directory.class);
        final TransferStatus status = new TransferStatus().setRegion(region);
        final Encryption encryption = session._getFeature(Encryption.class);
        if(encryption != null) {
            status.setEncryption(encryption.getDefault(home));
        }
        final Path vault = directory.mkdir(session._getFeature(Write.class), home, status);

        final Path dataDir = new Path(vault, "d", EnumSet.of(Path.Type.directory));
        final Path firstLevel = new Path(dataDir, rootDirectoryIdHash.substring(0, 2), EnumSet.of(Path.Type.directory));
        final Path secondLevel = new Path(firstLevel, rootDirectoryIdHash.substring(2), EnumSet.of(Path.Type.directory));

        directory.mkdir(session._getFeature(Write.class), dataDir, status);
        directory.mkdir(session._getFeature(Write.class), firstLevel, status);
        directory.mkdir(session._getFeature(Write.class), secondLevel, status);

        // vault.uvf
        new ContentWriter(session).write(this.getConfig(), vaultMetadata.getBytes(StandardCharsets.US_ASCII));
        // dir.uvf
        new ContentWriter(session).write(new Path(secondLevel, "dir.uvf", EnumSet.of(Path.Type.file)), rootDirectoryMetadata);
    }

    @Override
    public void load(final Session<?> session, final VaultMetadataProvider metadata) throws BackgroundException {
        try(final UVFVaultMetadataProvider provider = UVFVaultMetadataProvider.cast(metadata)) {
            this.masterKey = UVFMasterkey.fromDecryptedPayload(provider.decrypt());
            final CryptorProvider cryptorProvider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
            log.debug("Initialized crypto provider {}", cryptorProvider);
            this.cryptor = cryptorProvider.provide(masterKey, FastSecureRandomProvider.get().provide());
            this.filenameProvider = new CryptoFilenameV7Provider(Integer.MAX_VALUE);
            this.directoryProvider = new CryptoDirectoryUVFProvider(this, filenameProvider);
            this.nonceSize = 12;
        }
    }

    @Override
    public synchronized void close() {
        super.close();
        cryptor = null;
    }

    @Override
    public Path getMasterkeyPath() {
        return masterkeyPath;
    }

    @Override
    public RevolvingMasterkey getMasterkey() {
        return masterKey;
    }

    @Override
    public Path getConfig() {
        return masterkeyPath;
    }

    @Override
    public Path getHome() {
        return home;
    }

    @Override
    public FileHeaderCryptor getFileHeaderCryptor() {
        return cryptor.fileHeaderCryptor();
    }

    @Override
    public FileContentCryptor getFileContentCryptor() {
        return cryptor.fileContentCryptor();
    }

    @Override
    public CryptoFilename getFilenameProvider() {
        return filenameProvider;
    }

    @Override
    public CryptoDirectory getDirectoryProvider() {
        return directoryProvider;
    }

    @Override
    public Cryptor getCryptor() {
        return cryptor;
    }

    @Override
    public int getNonceSize() {
        return nonceSize;
    }

    @Override
    public Pattern getFilenamePattern() {
        return FILENAME_PATTERN;
    }

    @Override
    public String getDirectoryMetadataFilename() {
        return DIRECTORY_METADATA_FILENAME;
    }

    @Override
    public String getBackupDirectoryMetadataFilename() {
        return BACKUP_DIRECTORY_METADATA_FILENAME;
    }

    public DirectoryMetadata getRootDirId() {
        return this.cryptor.directoryContentCryptor().rootDirectoryMetadata();
    }

    @Override
    public VaultVersion getVersion() {
        return new VaultVersion(VaultVersion.Type.UVF);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof UVFVault)) {
            return false;
        }
        final UVFVault that = (UVFVault) o;
        return new SimplePathPredicate(home).test(that.home);
    }

    @Override
    public int hashCode() {
        return Objects.hash(new SimplePathPredicate(home));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UVFVault{");
        sb.append("home=").append(home);
        sb.append(", cryptor=").append(cryptor);
        sb.append('}');
        return sb.toString();
    }
}
