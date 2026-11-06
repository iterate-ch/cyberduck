package ch.cyberduck.core.cryptomator.impl.v8;

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
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryV8Provider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.CredentialsVaultMetadataProvider;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultException;
import ch.cyberduck.core.vault.VaultMetadataProvider;
import ch.cyberduck.core.vault.VaultUnlockException;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.CipherCombo;
import org.cryptomator.cryptolib.api.Cvf8LockedVault;
import org.cryptomator.cryptolib.api.Cvf8UnlockedVault;
import org.cryptomator.cryptolib.api.CvfMasterkey;
import org.cryptomator.cryptolib.api.InvalidPassphraseException;
import org.cryptomator.cryptolib.api.LockedVault;
import org.cryptomator.cryptolib.api.UnlockedVault;
import org.cryptomator.cryptolib.api.UnsupportedVaultFormatException;
import org.cryptomator.cryptolib.api.VaultConfigLoadException;
import org.cryptomator.cryptolib.api.VaultDirectory;
import org.cryptomator.cryptolib.api.VaultKeyInvalidException;
import org.cryptomator.cryptolib.common.MasterkeyFile;
import org.cryptomator.cryptolib.common.MasterkeyFileAccess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.auto.service.AutoService;
import com.google.gson.JsonParseException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@AutoService(Vault.class)
public class CryptomatorVault extends AbstractVault {
    private static final Logger log = LogManager.getLogger(CryptomatorVault.class);

    public static final String REGULAR_FILE_EXTENSION = ".c9r";
    public static final int VAULT_VERSION = 8;
    /**
     * Vault format without vault.cryptomator configuration file
     */
    private static final int VAULT_VERSION_WITHOUT_CONFIG = 7;

    private static final String FILENAME_DIRECTORYID = "dir";
    private static final String DIRECTORY_METADATA_FILENAME = String.format("%s%s", FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);
    private static final String BACKUP_FILENAME_DIRECTORYID = "dirid";
    private static final String BACKUP_DIRECTORY_METADATA_FILENAME = String.format("%s%s", BACKUP_FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^([A-Za-z0-9_=-]+)" + REGULAR_FILE_EXTENSION);

    private static final String JSON_KEY_VAULTVERSION = "format";
    private static final String JSON_KEY_CIPHERCONFIG = "cipherCombo";
    private static final String JSON_KEY_SHORTENING_THRESHOLD = "shorteningThreshold";

    private final Path home;
    private final Path masterkeyPath;
    private final byte[] pepper;

    private final Path config;

    private Cvf8UnlockedVault vault;

    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    public CryptomatorVault(final Path home) {
        this(home,
                new Path(home, PreferencesFactory.get().getProperty("cryptomator.vault.masterkey.filename"), EnumSet.of(Path.Type.file, Path.Type.vaultmetadata)),
                new Path(home, PreferencesFactory.get().getProperty("cryptomator.vault.config.filename"), EnumSet.of(Path.Type.file, Path.Type.vaultmetadata)),
                PreferencesFactory.get().getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8)
        );
    }

    public CryptomatorVault(final Path home, final Path masterkeyPath, final Path config, final byte[] pepper) {
        this.home = home;
        this.masterkeyPath = masterkeyPath;
        this.pepper = pepper;
        this.config = config;
    }

    public Path getHome() {
        return home;
    }

    @Override
    public Path getMasterkeyPath() {
        return masterkeyPath;
    }

    @Override
    public Path getConfig() {
        return config;
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
        return vault.cipherCombo() == CipherCombo.SIV_CTRMAC ? 16 : 12;
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
    public Pattern getFilenamePattern() {
        return FILENAME_PATTERN;
    }

    @Override
    public void create(final Session<?> session, final String region, final VaultMetadataProvider metadata) throws BackgroundException {
        try(final CredentialsVaultMetadataProvider provider = CredentialsVaultMetadataProvider.cast(metadata)) {
            final VaultCredentials credentials = provider.getCredentials();
            final String passphrase = credentials.getPassword();
            final ByteArrayOutputStream masterkeyFile = new ByteArrayOutputStream();
            final Cvf8UnlockedVault created;
            try(CvfMasterkey masterkey = CvfMasterkey.generate(FastSecureRandomProvider.get().provide())) {
                // Master key file with legacy version 999 as required for vault format 8
                new MasterkeyFileAccess(pepper, FastSecureRandomProvider.get().provide()).persist(masterkey, masterkeyFile, passphrase);
                // New vaults use SIV_GCM with key id masterkeyfile:masterkey.cryptomator
                created = Cvf8UnlockedVault.create(masterkey, CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD, FastSecureRandomProvider.get().provide());
            }
            catch(IOException e) {
                throw new VaultException("Failure creating master key", e);
            }
            try {
                // Obtain non encrypted directory writer
                final Directory<?> directory = session._getFeature(Directory.class);
                final TransferStatus status = new TransferStatus().setRegion(region);
                final Encryption encryption = session._getFeature(Encryption.class);
                if(encryption != null) {
                    status.setEncryption(encryption.getDefault(home));
                }
                final Write writer = session._getFeature(Write.class);
                directory.mkdir(writer, home, status);
                log.debug("Write master key to {}", masterkeyPath);
                new ContentWriter(session).write(masterkeyPath, masterkeyFile.toByteArray());
                log.debug("Write vault configuration to {}", config);
                new ContentWriter(session).write(config, created.serializeConfig());
                this.init(created);
                final Path secondLevel = directoryProvider.toEncrypted(session, home);
                final Path firstLevel = secondLevel.getParent();
                final Path dataDir = firstLevel.getParent();
                log.debug("Create vault root directory at {}", secondLevel);
                directory.mkdir(writer, dataDir, status);
                directory.mkdir(writer, firstLevel, status);
                directory.mkdir(writer, secondLevel, status);
            }
            catch(BackgroundException e) {
                this.close();
                created.destroy();
                throw e;
            }
        }
    }

    @Override
    public void load(final Session<?> session, final VaultMetadataProvider metadata) throws BackgroundException {
        final byte[] masterkeyFile = readMasterkeyFile(session, masterkeyPath);
        byte[] vaultConfig;
        try {
            vaultConfig = new ContentReader(session).readBytes(config);
        }
        catch(NotfoundException e) {
            log.debug("Missing vault configuration {}", config);
            vaultConfig = null;
        }
        try(CvfMasterkey masterkey = loadMasterkey(masterkeyFile, CredentialsVaultMetadataProvider.cast(metadata).getCredentials().getPassword())) {
            if(null == vaultConfig) {
                vaultConfig = this.toVaultConfig(masterkeyFile, masterkey);
            }
            this.open(vaultConfig, masterkey);
        }
    }

    /**
     * Create configuration in memory for vault format 7 that has no vault.cryptomator file. Format 7 is identical
     * to format 8 using AES-SIV with AES-CTR and HMAC-SHA256 and the default name shortening threshold. The
     * configuration is signed with the master key to pass verification and must never be written to the vault.
     *
     * @param masterkeyFile Contents of master key file
     * @param masterkey     Decrypted master key
     * @return Contents of vault.cryptomator
     */
    private byte[] toVaultConfig(final byte[] masterkeyFile, final CvfMasterkey masterkey) throws BackgroundException {
        final int version;
        try {
            version = MasterkeyFile.read(new StringReader(new String(masterkeyFile, StandardCharsets.UTF_8))).version;
        }
        catch(IOException | JsonParseException e) {
            throw new VaultException("Failure reading key file", e).withFile(masterkeyPath);
        }
        if(version != VAULT_VERSION_WITHOUT_CONFIG) {
            throw new VaultException(String.format("Missing vault configuration %s for vault version %d", config.getName(), version)).withFile(config);
        }
        log.info("Use configuration for vault version {} without {}", version, config.getName());
        final JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .keyID(String.format("masterkeyfile:%s", masterkeyPath.getName()))
                .build();
        final JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .jwtID(new UUIDRandomStringService().random())
                .claim(JSON_KEY_VAULTVERSION, VAULT_VERSION)
                .claim(JSON_KEY_CIPHERCONFIG, CipherCombo.SIV_CTRMAC.name())
                .claim(JSON_KEY_SHORTENING_THRESHOLD, CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD)
                .build();
        final SignedJWT token = new SignedJWT(header, claims);
        // Returns the live key array
        final byte[] rawKey = masterkey.getEncoded().clone();
        try {
            token.sign(new MACSigner(rawKey));
            return token.serialize().getBytes(StandardCharsets.US_ASCII);
        }
        catch(JOSEException e) {
            throw new VaultException("Failure creating vault configuration", e);
        }
        finally {
            Arrays.fill(rawKey, (byte) 0x00);
        }
    }

    private CvfMasterkey loadMasterkey(final byte[] masterkeyFile, final CharSequence passphrase) throws BackgroundException {
        try {
            return new MasterkeyFileAccess(pepper, FastSecureRandomProvider.get().provide()).load(new ByteArrayInputStream(masterkeyFile), passphrase);
        }
        catch(InvalidPassphraseException e) {
            throw new VaultUnlockException("Failure to decrypt master key file", e);
        }
        catch(IllegalArgumentException | IOException | JsonParseException e) {
            throw new VaultException("Failure reading key file", e).withFile(masterkeyPath);
        }
    }

    /**
     * Verify vault configuration with master key and initialize vault
     *
     * @param vaultConfig Contents of vault.cryptomator
     * @param masterkey   Key to verify configuration with. Not retained, the unlocked vault keeps its own copy.
     */
    protected void open(final byte[] vaultConfig, final CvfMasterkey masterkey) throws BackgroundException {
        final Cvf8UnlockedVault unlocked;
        try {
            final LockedVault locked = LockedVault.parse(vaultConfig);
            if(!(locked instanceof Cvf8LockedVault)) {
                throw new VaultException(String.format("Unsupported vault configuration in %s", config.getName())).withFile(config);
            }
            unlocked = ((Cvf8LockedVault) locked).unlock(masterkey, FastSecureRandomProvider.get().provide());
        }
        catch(VaultKeyInvalidException e) {
            throw new VaultException("Invalid JWT signature", e).withFile(config);
        }
        catch(UnsupportedVaultFormatException e) {
            throw new VaultException(String.format("Unsupported vault version. %s", e.getMessage()), e).withFile(config);
        }
        catch(VaultConfigLoadException e) {
            throw new VaultException(String.format("Failed to verify vault config. %s", e.getMessage()), e).withFile(config);
        }
        this.init(unlocked);
    }

    private void init(final Cvf8UnlockedVault unlocked) {
        log.debug("Unlocked vault {} with cipher combo {}", home, unlocked.cipherCombo());
        this.vault = unlocked;
        this.filenameProvider = new CryptoFilenameV7Provider(unlocked.shorteningThreshold());
        this.directoryProvider = new CryptoDirectoryV8Provider(this, this.filenameProvider);
    }

    private static byte[] readMasterkeyFile(final Session<?> session, final Path file) throws BackgroundException {
        log.debug("Read master key {}", file);
        return new ContentReader(session).readBytes(file);
    }

    @Override
    public VaultVersion getVersion() {
        return new VaultVersion(VaultVersion.Type.V8);
    }

    @Override
    public synchronized void close() {
        super.close();
        vault = null;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof CryptomatorVault)) {
            return false;
        }
        final CryptomatorVault that = (CryptomatorVault) o;
        return new SimplePathPredicate(home).test(that.home);
    }

    @Override
    public int hashCode() {
        return Objects.hash(new SimplePathPredicate(home));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoVault{");
        sb.append("home=").append(home);
        sb.append(", vault=").append(vault);
        sb.append('}');
        return sb.toString();
    }
}
