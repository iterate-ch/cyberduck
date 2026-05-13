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
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.DirectoryMetadata;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;
import org.cryptomator.cryptolib.api.InvalidPassphraseException;
import org.cryptomator.cryptolib.api.Masterkey;
import org.cryptomator.cryptolib.api.PerpetualMasterkey;
import org.cryptomator.cryptolib.common.MasterkeyFile;
import org.cryptomator.cryptolib.common.MasterkeyFileAccess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.auto.service.AutoService;
import com.google.gson.JsonParseException;

@AutoService(Vault.class)
public class CryptomatorVault extends AbstractVault {
    private static final Logger log = LogManager.getLogger(CryptomatorVault.class);

    public static final String REGULAR_FILE_EXTENSION = ".c9r";
    public static final int VAULT_VERSION = 8;

    private static final String FILENAME_DIRECTORYID = "dir";
    private static final String DIRECTORY_METADATA_FILENAME = String.format("%s%s", FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);
    private static final String BACKUP_FILENAME_DIRECTORYID = "dirid";
    private static final String BACKUP_DIRECTORY_METADATA_FILENAME = String.format("%s%s", BACKUP_FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^([A-Za-z0-9_=-]+)" + REGULAR_FILE_EXTENSION);

    private static final List<Integer> SUPPORTED_VERSIONS = Arrays.asList(7, 8);

    private static final String JSON_KEY_VAULTVERSION = "format";
    private static final String JSON_KEY_CIPHERCONFIG = "cipherCombo";
    private static final String JSON_KEY_SHORTENING_THRESHOLD = "shorteningThreshold";

    private final Path home;
    private Masterkey masterkey;
    private final Path masterkeyPath;
    private final byte[] pepper;

    private final Path config;

    private int nonceSize;
    private Cryptor cryptor;

    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    public CryptomatorVault(final Path home) {
        this.home = home;
        this.masterkeyPath = new Path(home, PreferencesFactory.get().getProperty("cryptomator.vault.masterkey.filename"),
                EnumSet.of(Path.Type.file, Path.Type.vaultmetadata));
        this.config = new Path(home, PreferencesFactory.get().getProperty("cryptomator.vault.config.filename"),
                EnumSet.of(Path.Type.file, Path.Type.vaultmetadata));
        this.pepper = PreferencesFactory.get().getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8);
    }

    public Path getHome() {
        return home;
    }

    @Override
    public Path getMasterkeyPath() {
        return masterkeyPath;
    }

    public Masterkey getMasterkey() {
        return masterkey;
    }

    @Override
    public Path getConfig() {
        return config;
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
    public String getDirectoryMetadataFilename() {
        return DIRECTORY_METADATA_FILENAME;
    }

    @Override
    public String getBackupDirectoryMetadataFilename() {
        return BACKUP_DIRECTORY_METADATA_FILENAME;
    }

    @Override
    public DirectoryMetadata getRootDirId() {
        return this.cryptor.directoryContentCryptor().rootDirectoryMetadata();
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
            final ByteArrayOutputStream mkArray = new ByteArrayOutputStream();
            final PerpetualMasterkey mk = Masterkey.generate(FastSecureRandomProvider.get().provide());
            final MasterkeyFileAccess access = new MasterkeyFileAccess(pepper, FastSecureRandomProvider.get().provide());
            final MasterkeyFile masterkeyFile;
            try {
                access.persist(mk, mkArray, passphrase, VAULT_VERSION);
                masterkeyFile = MasterkeyFile.read(new StringReader(new String(mkArray.toByteArray(), StandardCharsets.UTF_8)));
            }
            catch(IOException e) {
                throw new VaultException("Failure creating master key", e);
            }
            log.debug("Write master key to {}", masterkeyPath);
            // Obtain non encrypted directory writer
            final Directory<?> directory = session._getFeature(Directory.class);
            final TransferStatus status = new TransferStatus().setRegion(region);
            final Encryption encryption = session._getFeature(Encryption.class);
            if(encryption != null) {
                status.setEncryption(encryption.getDefault(home));
            }
            directory.mkdir(session._getFeature(Write.class), home, status);
            new ContentWriter(session).write(masterkeyPath, mkArray.toByteArray());
            // Create vaultconfig.cryptomator
            final Algorithm algorithm = Algorithm.HMAC256(mk.getEncoded());
            final String conf = JWT.create()
                    .withJWTId(new UUIDRandomStringService().random())
                    .withKeyId(String.format("masterkeyfile:%s", masterkeyPath.getName()))
                    .withClaim(JSON_KEY_VAULTVERSION, VAULT_VERSION)
                    .withClaim(JSON_KEY_CIPHERCONFIG, CryptorProvider.Scheme.SIV_GCM.toString())
                    .withClaim(JSON_KEY_SHORTENING_THRESHOLD, CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD)
                    .sign(algorithm);
            new ContentWriter(session).write(config, conf.getBytes(StandardCharsets.US_ASCII));
            this.unlock(parseVaultConfigFromJWT(conf).withMasterkeyFile(masterkeyFile), passphrase);
            final Path secondLevel = directoryProvider.toEncrypted(session, home);
            final Path firstLevel = secondLevel.getParent();
            final Path dataDir = firstLevel.getParent();
            log.debug("Create vault root directory at {}", secondLevel);
            directory.mkdir(session._getFeature(Write.class), dataDir, status);
            directory.mkdir(session._getFeature(Write.class), firstLevel, status);
            directory.mkdir(session._getFeature(Write.class), secondLevel, status);
        }
    }

    @Override
    public void load(final Session<?> session, final VaultMetadataProvider metadata) throws BackgroundException {
        final VaultConfig vaultConfig = readVaultConfig(session, masterkeyPath, config);
        this.unlock(vaultConfig, CredentialsVaultMetadataProvider.cast(metadata).getCredentials().getPassword());
    }

    protected PerpetualMasterkey unlock(final VaultConfig vaultConfig, final CharSequence passphrase) throws BackgroundException {
        try {
            final PerpetualMasterkey masterKey = getMasterKey(vaultConfig.getMkfile(), pepper, passphrase);
            this.open(vaultConfig, masterKey);
            return masterKey;
        }
        catch(IllegalArgumentException | IOException e) {
            throw new VaultException("Failure reading key file", e);
        }
        catch(InvalidPassphraseException e) {
            throw new VaultUnlockException("Failure to decrypt master key file", e);
        }
    }

    protected void open(final CryptomatorVault.VaultConfig vaultConfig, final PerpetualMasterkey masterKey) throws BackgroundException {
        if(!SUPPORTED_VERSIONS.contains(vaultConfig.vaultVersion())) {
            throw new VaultException(String.format("Unsupported vault version %d", vaultConfig.vaultVersion()));
        }
        final CryptorProvider provider = CryptorProvider.forScheme(vaultConfig.getCipherCombo());
        log.debug("Initialized crypto provider {}", provider);
        vaultConfig.verify(masterKey.getEncoded(), VAULT_VERSION);
        this.masterkey = masterKey;
        this.cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        this.filenameProvider = new CryptoFilenameV7Provider(vaultConfig.getShorteningThreshold());
        this.directoryProvider = new CryptoDirectoryV8Provider(this, this.filenameProvider);
        this.nonceSize = vaultConfig.getNonceSize();
    }

    private static CryptomatorVault.VaultConfig readVaultConfig(final Session<?> session, final Path masterkeyPath, final Path config) throws BackgroundException {
        final MasterkeyFile masterkeyFile = readMasterkeyFile(session, masterkeyPath);
        try {
            return parseVaultConfigFromJWT(new ContentReader(session).read(config)).withMasterkeyFile(masterkeyFile);
        }
        catch(NotfoundException e) {
            log.debug("Ignore failure reading vault configuration {}", config);
            return parseVaultConfigFromMasterKey(masterkeyFile).withMasterkeyFile(masterkeyFile);
        }
    }

    private static CryptomatorVault.VaultConfig parseVaultConfigFromJWT(final String token) {
        final DecodedJWT decoded = JWT.decode(token);
        return new CryptomatorVault.VaultConfig(
                decoded.getClaim(JSON_KEY_VAULTVERSION).asInt(),
                decoded.getClaim(JSON_KEY_SHORTENING_THRESHOLD).asInt(),
                CryptorProvider.Scheme.valueOf(decoded.getClaim(JSON_KEY_CIPHERCONFIG).asString()),
                decoded.getAlgorithm(), decoded);
    }

    private static VaultConfig parseVaultConfigFromMasterKey(final MasterkeyFile masterkeyFile) {
        return new VaultConfig(masterkeyFile.version, CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD,
                CryptorProvider.Scheme.SIV_CTRMAC, null, null);
    }

    private static MasterkeyFile readMasterkeyFile(final Session<?> session, final Path file) throws BackgroundException {
        log.debug("Read master key {}", file);
        try(Reader reader = new ContentReader(session).getReader(file)) {
            return MasterkeyFile.read(reader);
        }
        catch(JsonParseException | IllegalArgumentException | IllegalStateException | IOException e) {
            throw new VaultException(String.format("Failure reading vault master key file %s", file.getName()), e).withFile(file);
        }
    }

    private static PerpetualMasterkey getMasterKey(final MasterkeyFile masterkeyFile, final byte[] pepper, final CharSequence passphrase) throws IOException {
        final StringWriter writer = new StringWriter();
        masterkeyFile.write(writer);
        return new MasterkeyFileAccess(pepper, FastSecureRandomProvider.get().provide()).load(
                new ByteArrayInputStream(writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8)), passphrase);
    }

    @Override
    public VaultVersion getVersion() {
        return new VaultVersion(VaultVersion.Type.V8);
    }

    @Override
    public synchronized void close() {
        super.close();
        cryptor = null;
    }

    public static class VaultConfig {
        private final int version;
        private final int shorteningThreshold;
        private final CryptorProvider.Scheme cipherCombo;
        private final String algorithm;
        private final DecodedJWT token;
        private MasterkeyFile mkfile;

        public VaultConfig(int version, int shorteningThreshold, CryptorProvider.Scheme cipherCombo, String algorithm, DecodedJWT token) {
            this.version = version;
            this.shorteningThreshold = shorteningThreshold;
            this.cipherCombo = cipherCombo;
            this.algorithm = algorithm;
            this.token = token;
        }

        public int vaultVersion() {
            return version;
        }

        public VaultConfig withMasterkeyFile(final MasterkeyFile mkfile) {
            this.mkfile = mkfile;
            return this;
        }

        public MasterkeyFile getMkfile() {
            return mkfile;
        }

        public int getShorteningThreshold() {
            return shorteningThreshold;
        }

        public CryptorProvider.Scheme getCipherCombo() {
            return cipherCombo;
        }

        public int getNonceSize() throws VaultException {
            switch(cipherCombo) {
                case SIV_CTRMAC:
                    return 16;
                case SIV_GCM:
                    return 12;
                default:
                    throw new VaultException(String.format("Unsupported cipher scheme %s", cipherCombo));
            }
        }

        private Algorithm initAlgorithm(byte[] rawKey) throws VaultException {
            switch(algorithm) {
                case "HS256":
                    return Algorithm.HMAC256(rawKey);
                case "HS384":
                    return Algorithm.HMAC384(rawKey);
                case "HS512":
                    return Algorithm.HMAC512(rawKey);
                default:
                    throw new VaultException(String.format("Unsupported signature algorithm %s", algorithm));
            }
        }

        public void verify(byte[] rawKey, int expectedVaultVersion) throws VaultException {
            try {
                if(token == null) {
                    return;
                }
                JWTVerifier verifier = JWT.require(initAlgorithm(rawKey))
                        .withClaim(JSON_KEY_VAULTVERSION, expectedVaultVersion)
                        .build();
                verifier.verify(token);
            }
            catch(SignatureVerificationException e) {
                throw new VaultException("Invalid JWT signature", e);
            }
            catch(InvalidClaimException e) {
                throw new VaultException(String.format("Expected vault config for version %d", expectedVaultVersion), e);
            }
            catch(JWTVerificationException e) {
                throw new VaultException(String.format("Failed to verify vault config %s", token), e);
            }
        }
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
        sb.append(", cryptor=").append(cryptor);
        sb.append('}');
        return sb.toString();
    }
}
