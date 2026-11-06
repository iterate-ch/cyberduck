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
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryUVFProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultException;
import ch.cyberduck.core.vault.VaultMetadataProvider;
import ch.cyberduck.core.vault.VaultUnlockException;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.LockedVault;
import org.cryptomator.cryptolib.api.UnlockedVault;
import org.cryptomator.cryptolib.api.UnsupportedVaultFormatException;
import org.cryptomator.cryptolib.api.UvfLockedVault;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;
import org.cryptomator.cryptolib.api.VaultConfigLoadException;
import org.cryptomator.cryptolib.api.VaultDirectory;
import org.cryptomator.cryptolib.api.VaultKeyInvalidException;
import org.cryptomator.cryptolib.api.VaultRollbackException;

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
     * Length of AES-GCM chunk nonces
     */
    private static final int NONCE_SIZE = 12;

    /**
     * Root of vault directory
     */
    private final Path home;
    private final Path masterkeyPath;

    private UvfUnlockedVault vault;
    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    public UVFVault(final Path home) {
        this.home = home;
        this.masterkeyPath = new Path(home, PreferencesFactory.get().getProperty("cryptomator.vault.config.filename.uvf"),
                EnumSet.of(Path.Type.file, Path.Type.vaultmetadata));
    }

    @Override
    public void create(final Session<?> session, final String region, final VaultMetadataProvider metadata) throws BackgroundException {
        try(final UVFVaultMetadataProvider provider = UVFVaultMetadataProvider.cast(metadata)) {
            final UvfUnlockedVault created = provider.create();
            try {
                this.uploadTemplate(session, region, created);
                this.init(session, created);
            }
            catch(BackgroundException e) {
                created.destroy();
                throw e;
            }
        }
    }

    private void uploadTemplate(final Session<?> session, final String region, final UvfUnlockedVault created) throws BackgroundException {
        log.debug("Create vault root directory at {}", home);
        final Directory<?> directory = session._getFeature(Directory.class);
        final TransferStatus status = new TransferStatus().setRegion(region);
        final Encryption encryption = session._getFeature(Encryption.class);
        if(encryption != null) {
            status.setEncryption(encryption.getDefault(home));
        }
        final Path vault = directory.mkdir(session._getFeature(Write.class), home, status);
        final VaultDirectory root = created.rootDirectory();
        // d/XX/YYYY
        final String[] segments = StringUtils.split(root.dirPath(), '/');
        final Path dataDir = new Path(vault, segments[0], EnumSet.of(Path.Type.directory));
        final Path firstLevel = new Path(dataDir, segments[1], EnumSet.of(Path.Type.directory));
        final Path secondLevel = new Path(firstLevel, segments[2], EnumSet.of(Path.Type.directory));
        directory.mkdir(session._getFeature(Write.class), dataDir, status);
        directory.mkdir(session._getFeature(Write.class), firstLevel, status);
        directory.mkdir(session._getFeature(Write.class), secondLevel, status);
        new ContentWriter(session).write(this.getConfig(), created.serializeConfig());
        new ContentWriter(session).write(new Path(secondLevel, BACKUP_DIRECTORY_METADATA_FILENAME, EnumSet.of(Path.Type.file)), root.encryptedMetadataBackup());
    }

    @Override
    public void load(final Session<?> session, final VaultMetadataProvider metadata) throws BackgroundException {
        try(final UVFVaultMetadataProvider provider = UVFVaultMetadataProvider.cast(metadata)) {
            final byte[] config = new ContentReader(session).readBytes(masterkeyPath);
            try {
                final LockedVault locked = LockedVault.parse(config);
                if(!(locked instanceof UvfLockedVault)) {
                    throw new VaultException(String.format("Unsupported vault metadata in %s", masterkeyPath.getName())).withFile(masterkeyPath);
                }
                // Reject outdated or forged vault metadata missing seeds seen before
                this.init(session, provider.unlock(((UvfLockedVault) locked).requireSeeds(UVFSeedPins.get(this.getPinIdentifier(session)))));
            }
            catch(VaultKeyInvalidException e) {
                throw new VaultUnlockException("Failure to decrypt vault metadata", e);
            }
            catch(VaultRollbackException e) {
                throw new VaultException(String.format("Vault metadata %s is outdated or forged. %s", masterkeyPath.getName(), e.getMessage()), e).withFile(masterkeyPath);
            }
            catch(VaultConfigLoadException | UnsupportedVaultFormatException e) {
                throw new VaultException(String.format("Failure reading vault metadata %s. %s", masterkeyPath.getName(), e.getMessage()), e).withFile(masterkeyPath);
            }
        }
    }

    private void init(final Session<?> session, final UvfUnlockedVault unlocked) {
        log.debug("Unlocked vault {} with seeds {}", home, unlocked.seedIds());
        UVFSeedPins.put(this.getPinIdentifier(session), unlocked.seedDigests().values());
        this.vault = unlocked;
        this.filenameProvider = new CryptoFilenameV7Provider(Integer.MAX_VALUE);
        this.directoryProvider = new CryptoDirectoryUVFProvider(this, filenameProvider);
    }

    /**
     * @return Identifier of vault unique for connection and path
     */
    private String getPinIdentifier(final Session<?> session) {
        return String.format("%s%s", session.getHost().getUuid(), home.getAbsolute());
    }

    @Override
    public synchronized void close() {
        super.close();
        vault = null;
    }

    @Override
    public Path getMasterkeyPath() {
        return masterkeyPath;
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
    public UnlockedVault getUnlockedVault() {
        return vault;
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
    public int getNonceSize() {
        return NONCE_SIZE;
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

    @Override
    public VaultDirectory getRootDirId() {
        return vault.rootDirectory();
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
        sb.append(", vault=").append(vault);
        sb.append('}');
        return sb.toString();
    }
}
