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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoAuthenticationException;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryV8Provider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultException;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.core.vault.VaultMetadataProvider;

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
import java.text.MessageFormat;
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
public class CryptoVault extends AbstractVault {

    private static final Logger log = LogManager.getLogger(CryptoVault.class);


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

    private final PasswordStore keychain = PasswordStoreFactory.get();
    private final Preferences preferences = PreferencesFactory.get();

    private final Path config;

    private int nonceSize;
    private Cryptor cryptor;

    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    public CryptoVault(final Path home) {
        this.home = home;
        this.masterkeyPath = new Path(home, preferences.getProperty("cryptomator.vault.masterkey.filename"), EnumSet.of(Path.Type.file, Path.Type.vault));
        this.config = new Path(home, preferences.getProperty("cryptomator.vault.config.filename"), EnumSet.of(Path.Type.file, Path.Type.vault));
        this.pepper = preferences.getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8);

        //TODO nötig?
        // New vault home with vault flag set for internal use
        final EnumSet<Path.Type> type = EnumSet.copyOf(home.getType());
        type.add(Path.Type.vault);
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
    public String getRegularFileExtension() {
        return REGULAR_FILE_EXTENSION;
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

    public AbstractVault create(final Session<?> session, final String region, final VaultCredentials credentials) throws BackgroundException {
        return this.create(session, region, new DefaultVaultMetadataV8Provider(credentials));
    }

    @Override
    public AbstractVault create(final Session<?> session, final String region, final VaultMetadataProvider metadata) throws BackgroundException {
        final VaultMetadataV8Provider provider = VaultMetadataV8Provider.cast(metadata);
        final VaultCredentials credentials = provider.getCredentials();
        final Host bookmark = session.getHost();
        if(credentials.isSaved()) {
            try {
                keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                        new DefaultUrlProvider(bookmark).toUrl(masterkeyPath, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
            }
            catch(LocalAccessDeniedException e) {
                log.error("Failure {} saving credentials for {} in password store", e, bookmark);
            }
        }
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
        final Path vault = directory.mkdir(session._getFeature(Write.class), home, status);
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
        this.open(parseVaultConfigFromJWT(conf).withMasterkeyFile(masterkeyFile), passphrase);
        final Path secondLevel = directoryProvider.toEncrypted(session, home);
        final Path firstLevel = secondLevel.getParent();
        final Path dataDir = firstLevel.getParent();
        log.debug("Create vault root directory at {}", secondLevel);
        directory.mkdir(session._getFeature(Write.class), dataDir, status);
        directory.mkdir(session._getFeature(Write.class), firstLevel, status);
        directory.mkdir(session._getFeature(Write.class), secondLevel, status);
        return this;
    }

    private static VaultConfig parseVaultConfigFromMasterKey(final MasterkeyFile masterkeyFile) {
        return new VaultConfig(masterkeyFile.version, CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD,
                CryptorProvider.Scheme.SIV_CTRMAC, null, null);
    }

    @Override
    public Vault load(final Session<?> session, final PasswordCallback prompt) throws BackgroundException {
        final Host bookmark = session.getHost();
        String passphrase = keychain.getPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                new DefaultUrlProvider(bookmark).toUrl(masterkeyPath, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl());
        if(null == passphrase) {
            // Legacy
            passphrase = keychain.getPassword(String.format("Cryptomator Passphrase %s", bookmark.getHostname()),
                    new DefaultUrlProvider(bookmark).toUrl(masterkeyPath, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl());
        }
        return this.unlock(session, prompt, bookmark, passphrase);
    }

    public Vault unlock(final Session<?> session, final PasswordCallback prompt, final Host bookmark, final String passphrase) throws BackgroundException {
        final ch.cyberduck.core.cryptomator.impl.v8.CryptoVault.VaultConfig vaultConfig = this.readVaultConfig(session);
        this.unlock(vaultConfig, passphrase, bookmark, prompt,
                MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), home.getName())
        );
        return this;
    }

    public void unlock(final ch.cyberduck.core.cryptomator.impl.v8.CryptoVault.VaultConfig vaultConfig, final String passphrase, final Host bookmark, final PasswordCallback prompt,
                       final String message) throws BackgroundException {
        final Credentials credentials;
        if(null == passphrase) {
            credentials = prompt.prompt(
                    bookmark, LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                    message,
                    new LoginOptions()
                            .save(preferences.getBoolean("cryptomator.vault.keychain"))
                            .user(false)
                            .anonymous(false)
                            .icon("cryptomator.tiff")
                            .passwordPlaceholder(LocaleFactory.localizedString("Passphrase", "Cryptomator")));
            if(null == credentials.getPassword()) {
                throw new LoginCanceledException();
            }
        }
        else {
            credentials = new VaultCredentials(passphrase).setSaved(false);
        }
        try {
            this.open(vaultConfig, credentials.getPassword());
            if(credentials.isSaved()) {
                log.info("Save passphrase for {}", masterkeyPath);
                // Save password with hostname and path to masterkey.cryptomator in keychain
                keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                        new DefaultUrlProvider(bookmark).toUrl(masterkeyPath, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
            }
        }
        catch(CryptoAuthenticationException e) {
            this.unlock(vaultConfig, null, bookmark, prompt, String.format("%s %s.", e.getDetail(),
                    MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), home.getName())));
        }
    }

    private ch.cyberduck.core.cryptomator.impl.v8.CryptoVault.VaultConfig readVaultConfig(final Session<?> session) throws BackgroundException {
        final MasterkeyFile masterkeyFile = this.readMasterkeyFile(session, masterkeyPath);
        try {
            return parseVaultConfigFromJWT(new ContentReader(session).read(config)).withMasterkeyFile(masterkeyFile);
        }
        catch(NotfoundException e) {
            log.debug("Ignore failure reading vault configuration {}", config);
            return parseVaultConfigFromMasterKey(masterkeyFile).withMasterkeyFile(masterkeyFile);
        }
    }

    public static ch.cyberduck.core.cryptomator.impl.v8.CryptoVault.VaultConfig parseVaultConfigFromJWT(final String token) {
        final DecodedJWT decoded = JWT.decode(token);
        return new ch.cyberduck.core.cryptomator.impl.v8.CryptoVault.VaultConfig(
                decoded.getClaim(JSON_KEY_VAULTVERSION).asInt(),
                decoded.getClaim(JSON_KEY_SHORTENING_THRESHOLD).asInt(),
                CryptorProvider.Scheme.valueOf(decoded.getClaim(JSON_KEY_CIPHERCONFIG).asString()),
                decoded.getAlgorithm(), decoded);
    }

    private MasterkeyFile readMasterkeyFile(final Session<?> session, final Path file) throws BackgroundException {
        log.debug("Read master key {}", file);
        try(Reader reader = new ContentReader(session).getReader(file)) {
            return MasterkeyFile.read(reader);
        }
        catch(JsonParseException | IllegalArgumentException | IllegalStateException | IOException e) {
            throw new VaultException(String.format("Failure reading vault master key file %s", file.getName()), e);
        }
    }

    protected void open(final ch.cyberduck.core.cryptomator.impl.v8.CryptoVault.VaultConfig vaultConfig, final CharSequence passphrase) throws BackgroundException {
        try {
            final PerpetualMasterkey masterKey = this.getMasterKey(vaultConfig.getMkfile(), passphrase);
            this.open(vaultConfig, masterKey);
        }
        catch(IllegalArgumentException | IOException e) {
            throw new VaultException("Failure reading key file", e);
        }
        catch(InvalidPassphraseException e) {
            throw new CryptoAuthenticationException("Failure to decrypt master key file", e);
        }
    }

    protected void open(final ch.cyberduck.core.cryptomator.impl.v8.CryptoVault.VaultConfig vaultConfig, final PerpetualMasterkey masterKey) throws BackgroundException {
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

    private PerpetualMasterkey getMasterKey(final MasterkeyFile mkFile, final CharSequence passphrase) throws IOException {
        final StringWriter writer = new StringWriter();
        mkFile.write(writer);
        return new MasterkeyFileAccess(pepper, FastSecureRandomProvider.get().provide()).load(
                new ByteArrayInputStream(writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8)), passphrase);
    }

    @Override
    public VaultMetadata getMetadata() {
        return new VaultMetadata(this.getHome(), VaultMetadata.Type.V8);
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
        if(!(o instanceof CryptoVault)) {
            return false;
        }
        final CryptoVault that = (CryptoVault) o;
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
