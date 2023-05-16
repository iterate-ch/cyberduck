package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.cryptomator.features.*;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryV6Provider;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryV7Provider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV6Provider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;
import org.cryptomator.cryptolib.api.InvalidPassphraseException;
import org.cryptomator.cryptolib.api.Masterkey;
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
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonParseException;

import static ch.cyberduck.core.vault.DefaultVaultRegistry.DEFAULT_VAULTCONFIG_FILE_NAME;

/**
 * Cryptomator vault implementation
 */
public class CryptoVault implements Vault {
    private static final Logger log = LogManager.getLogger(CryptoVault.class);

    public static final int VAULT_VERSION_DEPRECATED = 6;
    public static final int VAULT_VERSION = PreferencesFactory.get().getInteger("cryptomator.vault.version");
    public static final byte[] VAULT_PEPPER = PreferencesFactory.get().getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8);

    public static final String DIR_PREFIX = "0";

    private static final Pattern BASE32_PATTERN = Pattern.compile("^0?(([A-Z2-7]{8})*[A-Z2-7=]{8})");
    private static final Pattern BASE64URL_PATTERN = Pattern.compile("^([A-Za-z0-9_=-]+).c9r");

    private static final String JSON_KEY_VAULTVERSION = "format";
    private static final String JSON_KEY_CIPHERCONFIG = "cipherCombo";
    private static final String JSON_KEY_SHORTENING_THRESHOLD = "shorteningThreshold";

    /**
     * Root of vault directory
     */
    private final Path home;
    private final Path masterkey;
    private final Path config;
    private final Path vault;
    private int vaultVersion;
    private int nonceSize;

    private final PasswordStore keychain = PasswordStoreFactory.get();
    private final Preferences preferences = PreferencesFactory.get();

    private Cryptor cryptor;
    private CryptorCache fileNameCryptor;

    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    private final byte[] pepper;

    public CryptoVault(final Path home) {
        this(home, DefaultVaultRegistry.DEFAULT_MASTERKEY_FILE_NAME, DEFAULT_VAULTCONFIG_FILE_NAME, VAULT_PEPPER);
    }

    public CryptoVault(final Path home, final String masterkey, final String config, final byte[] pepper) {
        this.home = home;
        this.masterkey = new Path(home, masterkey, EnumSet.of(Path.Type.file, Path.Type.vault));
        this.config = new Path(home, config, EnumSet.of(Path.Type.file, Path.Type.vault));
        this.pepper = pepper;
        // New vault home with vault flag set for internal use
        final EnumSet<Path.Type> type = EnumSet.copyOf(home.getType());
        type.add(Path.Type.vault);
        if(home.isRoot()) {
            this.vault = new Path(home.getAbsolute(), type, new PathAttributes(home.attributes()));
        }
        else {
            this.vault = new Path(home.getParent(), home.getName(), type, new PathAttributes(home.attributes()));
        }
    }

    public synchronized Path create(final Session<?> session, final VaultCredentials credentials, final int version) throws BackgroundException {
        return this.create(session, null, credentials, version);
    }

    public synchronized Path create(final Session<?> session, final String region, final VaultCredentials credentials, final int version) throws BackgroundException {
        final Host bookmark = session.getHost();
        if(credentials.isSaved()) {
            try {
                keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                        new DefaultUrlProvider(bookmark).toUrl(masterkey).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
            }
            catch(LocalAccessDeniedException e) {
                log.error(String.format("Failure %s saving credentials for %s in password store", e, bookmark));
            }
        }
        final String passphrase = credentials.getPassword();
        final ByteArrayOutputStream mkArray = new ByteArrayOutputStream();
        final Masterkey mk = Masterkey.generate(FastSecureRandomProvider.get().provide());
        final MasterkeyFileAccess access = new MasterkeyFileAccess(pepper, FastSecureRandomProvider.get().provide());
        final MasterkeyFile masterkeyFile;
        try {
            access.persist(mk, mkArray, passphrase, version);
            masterkeyFile = MasterkeyFile.read(new StringReader(new String(mkArray.toByteArray(), StandardCharsets.UTF_8)));
        }
        catch(IOException e) {
            throw new VaultException("Failure creating master key", e);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write master key to %s", masterkey));
        }
        // Obtain non encrypted directory writer
        final Directory<?> directory = session._getFeature(Directory.class);
        final TransferStatus status = new TransferStatus().withRegion(region);
        final Encryption encryption = session.getFeature(Encryption.class);
        if(encryption != null) {
            status.setEncryption(encryption.getDefault(home));
        }
        final Path vault = directory.mkdir(home, status);
        new ContentWriter(session).write(masterkey, mkArray.toByteArray());
        if(VAULT_VERSION == version) {
            // Create vaultconfig.cryptomator
            final Algorithm algorithm = Algorithm.HMAC256(mk.getEncoded());
            final String conf = JWT.create()
                    .withJWTId(new UUIDRandomStringService().random())
                    .withKeyId(String.format("masterkeyfile:%s", masterkey.getName()))
                    .withClaim(JSON_KEY_VAULTVERSION, version)
                    .withClaim(JSON_KEY_CIPHERCONFIG, CryptorProvider.Scheme.SIV_GCM.toString())
                    .withClaim(JSON_KEY_SHORTENING_THRESHOLD, CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD)
                    .sign(algorithm);
            new ContentWriter(session).write(config, conf.getBytes(StandardCharsets.US_ASCII));
            this.open(this.getVaultConfig(conf).withMasterkeyFile(masterkeyFile), passphrase);
        }
        else {
            this.open(new VaultConfig(version, CryptoFilenameV6Provider.DEFAULT_NAME_SHORTENING_THRESHOLD,
                    CryptorProvider.Scheme.SIV_CTRMAC, null, null).withMasterkeyFile(masterkeyFile), passphrase);
        }
        final Path secondLevel = directoryProvider.toEncrypted(session, home.attributes().getDirectoryId(), home);
        final Path firstLevel = secondLevel.getParent();
        final Path dataDir = firstLevel.getParent();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create vault root directory at %s", secondLevel));
        }
        directory.mkdir(dataDir, status);
        directory.mkdir(firstLevel, status);
        directory.mkdir(secondLevel, status);
        return vault;
    }

    @Override
    public synchronized Path create(final Session<?> session, final String region, final VaultCredentials credentials) throws BackgroundException {
        return this.create(session, region, credentials, VAULT_VERSION);
    }

    @Override
    public synchronized CryptoVault load(final Session<?> session, final PasswordCallback prompt) throws BackgroundException {
        if(this.isUnlocked()) {
            log.warn(String.format("Skip unlock of open vault %s", this));
            return this;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Attempt to read master key from %s", masterkey));
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Read master key %s", masterkey));
        }
        final Host bookmark = session.getHost();
        String passphrase = keychain.getPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                new DefaultUrlProvider(bookmark).toUrl(masterkey).find(DescriptiveUrl.Type.provider).getUrl());
        if(null == passphrase) {
            // Legacy
            passphrase = keychain.getPassword(String.format("Cryptomator Passphrase %s", bookmark.getHostname()),
                    new DefaultUrlProvider(bookmark).toUrl(masterkey).find(DescriptiveUrl.Type.provider).getUrl());
        }
        final VaultConfig vaultConfig = this.getVaultConfig(session);
        this.unlock(vaultConfig, passphrase, bookmark, prompt,
                MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), home.getName()),
                keychain);
        return this;
    }

    private VaultConfig getVaultConfig(final Session<?> session) throws BackgroundException {
        try {
            final String token = new ContentReader(session).read(config);
            return this.getVaultConfig(token).withMasterkeyFile(this.readMasterkeyFile(session, masterkey));
        }
        catch(NotfoundException e) {
            final MasterkeyFile mkfile = this.readMasterkeyFile(session, masterkey);
            return new VaultConfig(mkfile.version,
                    mkfile.version == VAULT_VERSION_DEPRECATED ?
                            CryptoFilenameV6Provider.DEFAULT_NAME_SHORTENING_THRESHOLD :
                            CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD,
                    CryptorProvider.Scheme.SIV_CTRMAC, null, null).withMasterkeyFile(mkfile);
        }
    }

    private VaultConfig getVaultConfig(final String token) {
        final DecodedJWT decoded = JWT.decode(token);
        return new VaultConfig(
                decoded.getClaim(JSON_KEY_VAULTVERSION).asInt(),
                decoded.getClaim(JSON_KEY_SHORTENING_THRESHOLD).asInt(),
                CryptorProvider.Scheme.valueOf(decoded.getClaim(JSON_KEY_CIPHERCONFIG).asString()),
                decoded.getAlgorithm(), decoded);
    }

    private MasterkeyFile readMasterkeyFile(final Session<?> session, final Path masterkey) throws BackgroundException {
        try (Reader reader = new ContentReader(session).getReader(masterkey)) {
            return MasterkeyFile.read(reader);
        }
        catch(JsonParseException | IllegalArgumentException | IllegalStateException | IOException e) {
            throw new VaultException(String.format("Failure reading vault master key file %s", masterkey.getName()), e);
        }
    }

    private void unlock(final VaultConfig vaultConfig, final String passphrase, final Host bookmark, final PasswordCallback prompt,
                        final String message, final PasswordStore keychain) throws BackgroundException {
        final Credentials credentials;
        if(null == passphrase) {
            credentials = prompt.prompt(
                    bookmark, LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                    message,
                    new LoginOptions()
                            .user(false)
                            .anonymous(false)
                            .icon("cryptomator.tiff")
                            .passwordPlaceholder(LocaleFactory.localizedString("Passphrase", "Cryptomator")));
            if(null == credentials.getPassword()) {
                throw new LoginCanceledException();
            }
        }
        else {
            credentials = new VaultCredentials(passphrase).withSaved(preferences.getBoolean("vault.keychain"));
        }
        try {
            this.open(vaultConfig, credentials.getPassword());
            if(credentials.isSaved()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Save passphrase for %s", masterkey));
                }
                // Save password with hostname and path to masterkey.cryptomator in keychain
                keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                        new DefaultUrlProvider(bookmark).toUrl(masterkey).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
            }
        }
        catch(CryptoAuthenticationException e) {
            this.unlock(vaultConfig, null, bookmark, prompt, String.format("%s %s.", e.getDetail(),
                    MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), home.getName())), keychain);
        }
    }

    @Override
    public synchronized void close() {
        if(this.isUnlocked()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Close vault with cryptor %s", cryptor));
            }
            if(cryptor != null) {
                cryptor.destroy();
            }
            if(directoryProvider != null) {
                directoryProvider.destroy();
            }
            if(filenameProvider != null) {
                filenameProvider.destroy();
            }
        }
        cryptor = null;
        fileNameCryptor = null;
    }

    protected void open(final VaultConfig vaultConfig, final CharSequence passphrase) throws BackgroundException {
        switch(vaultConfig.version) {
            case VAULT_VERSION_DEPRECATED:
                this.open(vaultConfig, passphrase, new CryptoFilenameV6Provider(vault), new CryptoDirectoryV6Provider(vault, this));
                break;
            default:
                this.open(vaultConfig, passphrase, new CryptoFilenameV7Provider(vaultConfig.getShorteningThreshold()),
                        new CryptoDirectoryV7Provider(vault, this));
                break;
        }
    }

    protected void open(final VaultConfig vaultConfig, final CharSequence passphrase, final CryptoFilename filenameProvider,
                        final CryptoDirectory directoryProvider) throws BackgroundException {
        this.vaultVersion = vaultConfig.version;
        final CryptorProvider provider = CryptorProvider.forScheme(vaultConfig.getCipherCombo());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Initialized crypto provider %s", provider));
        }
        try {
            final Masterkey masterKey = this.getMasterKey(vaultConfig.getMkfile(), passphrase);
            vaultConfig.verify(masterKey.getEncoded(), VAULT_VERSION);
            this.cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
            this.fileNameCryptor = new CryptorCache(cryptor.fileNameCryptor());
            this.filenameProvider = filenameProvider;
            this.directoryProvider = directoryProvider;
            this.nonceSize = vaultConfig.getNonceSize();
        }
        catch(IllegalArgumentException | IOException e) {
            throw new VaultException("Failure reading key file", e);
        }
        catch(InvalidPassphraseException e) {
            throw new CryptoAuthenticationException("Failure to decrypt master key file", e);
        }
    }

    private Masterkey getMasterKey(final MasterkeyFile mkFile, final CharSequence passphrase) throws IOException {
        final StringWriter writer = new StringWriter();
        mkFile.write(writer);
        return new MasterkeyFileAccess(pepper, FastSecureRandomProvider.get().provide()).load(
                new ByteArrayInputStream(writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8)), passphrase);
    }

    public synchronized boolean isUnlocked() {
        return cryptor != null;
    }

    @Override
    public State getState() {
        return this.isUnlocked() ? State.open : State.closed;
    }

    @Override
    public boolean contains(final Path file) {
        if(this.isUnlocked()) {
            return new SimplePathPredicate(file).test(home) || file.isChild(home);
        }
        return false;
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), false);
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file, boolean metadata) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), metadata);
    }

    public Path encrypt(final Session<?> session, final Path file, final String directoryId, boolean metadata) throws BackgroundException {
        final Path encrypted;
        if(file.isFile() || metadata) {
            if(file.getType().contains(Path.Type.vault)) {
                log.warn(String.format("Skip file %s because it is marked as an internal vault path", file));
                return file;
            }
            if(new SimplePathPredicate(file).test(home)) {
                log.warn(String.format("Skip vault home %s because the root has no metadata file", file));
                return file;
            }
            final Path parent;
            final String filename;
            if(file.getType().contains(Path.Type.encrypted)) {
                final Path decrypted = file.attributes().getDecrypted();
                parent = directoryProvider.toEncrypted(session, decrypted.getParent().attributes().getDirectoryId(), decrypted.getParent());
                filename = directoryProvider.toEncrypted(session, parent.attributes().getDirectoryId(), decrypted.getName(), decrypted.getType());
            }
            else {
                parent = directoryProvider.toEncrypted(session, file.getParent().attributes().getDirectoryId(), file.getParent());
                filename = directoryProvider.toEncrypted(session, parent.attributes().getDirectoryId(), file.getName(), file.getType());
            }
            final PathAttributes attributes = new PathAttributes(file.attributes());
            attributes.setDirectoryId(null);
            if(!file.isFile() && !metadata) {
                // The directory is different from the metadata file used to resolve the actual folder
                attributes.setVersionId(null);
                attributes.setFileId(null);
            }
            // Translate file size
            attributes.setSize(this.toCiphertextSize(0L, file.attributes().getSize()));
            final EnumSet<Path.Type> type = EnumSet.copyOf(file.getType());
            if(metadata && vaultVersion == VAULT_VERSION_DEPRECATED) {
                type.remove(Path.Type.directory);
                type.add(Path.Type.file);
            }
            type.remove(Path.Type.decrypted);
            type.add(Path.Type.encrypted);
            encrypted = new Path(parent, filename, type, attributes);
        }
        else {
            if(file.getType().contains(Path.Type.encrypted)) {
                log.warn(String.format("Skip file %s because it is already marked as an encrypted path", file));
                return file;
            }
            if(file.getType().contains(Path.Type.vault)) {
                return directoryProvider.toEncrypted(session, home.attributes().getDirectoryId(), home);
            }
            encrypted = directoryProvider.toEncrypted(session, directoryId, file);
        }
        // Add reference to decrypted file
        if(!file.getType().contains(Path.Type.encrypted)) {
            encrypted.attributes().setDecrypted(file);
        }
        // Add reference for vault
        file.attributes().setVault(home);
        encrypted.attributes().setVault(home);
        return encrypted;
    }

    @Override
    public Path decrypt(final Session<?> session, final Path file) throws BackgroundException {
        if(file.getType().contains(Path.Type.decrypted)) {
            log.warn(String.format("Skip file %s because it is already marked as an decrypted path", file));
            return file;
        }
        if(file.getType().contains(Path.Type.vault)) {
            log.warn(String.format("Skip file %s because it is marked as an internal vault path", file));
            return file;
        }
        final Path inflated = this.inflate(session, file);
        final Pattern pattern = vaultVersion == VAULT_VERSION_DEPRECATED ? BASE32_PATTERN : BASE64URL_PATTERN;
        final Matcher m = pattern.matcher(inflated.getName());
        if(m.matches()) {
            final String ciphertext = m.group(1);
            try {
                final String cleartextFilename = fileNameCryptor.decryptFilename(
                        vaultVersion == VAULT_VERSION_DEPRECATED ? BaseEncoding.base32() : BaseEncoding.base64Url(),
                        ciphertext, file.getParent().attributes().getDirectoryId().getBytes(StandardCharsets.UTF_8));
                final PathAttributes attributes = new PathAttributes(file.attributes());
                if(this.isDirectory(inflated)) {
                    final Permission permission = attributes.getPermission();
                    permission.setUser(permission.getUser().or(Permission.Action.execute));
                    permission.setGroup(permission.getGroup().or(Permission.Action.execute));
                    permission.setOther(permission.getOther().or(Permission.Action.execute));
                    // Reset size for folders
                    attributes.setSize(-1L);
                    attributes.setVersionId(null);
                    attributes.setFileId(null);
                }
                else {
                    // Translate file size
                    attributes.setSize(this.toCleartextSize(0L, file.attributes().getSize()));
                }
                // Add reference to encrypted file
                attributes.setEncrypted(file);
                // Add reference for vault
                attributes.setVault(home);
                final EnumSet<Path.Type> type = EnumSet.copyOf(file.getType());
                type.remove(this.isDirectory(inflated) ? Path.Type.file : Path.Type.directory);
                type.add(this.isDirectory(inflated) ? Path.Type.directory : Path.Type.file);
                type.remove(Path.Type.encrypted);
                type.add(Path.Type.decrypted);
                final Path decrypted = new Path(file.getParent().attributes().getDecrypted(), cleartextFilename, type, attributes);
                if(type.contains(Path.Type.symboliclink)) {
                    decrypted.setSymlinkTarget(file.getSymlinkTarget());
                }
                return decrypted;
            }
            catch(AuthenticationFailedException e) {
                throw new CryptoAuthenticationException(
                        "Failure to decrypt due to an unauthentic ciphertext", e);
            }
        }
        else {
            throw new CryptoFilenameMismatchException(
                    String.format("Failure to decrypt %s due to missing pattern match for %s", inflated.getName(), pattern));
        }
    }

    private boolean isDirectory(final Path p) {
        if(vaultVersion == VAULT_VERSION_DEPRECATED) {
            return p.getName().startsWith(DIR_PREFIX);
        }
        return p.isDirectory();
    }

    @Override
    public long toCiphertextSize(final long cleartextFileOffset, final long cleartextFileSize) {
        if(TransferStatus.UNKNOWN_LENGTH == cleartextFileSize) {
            return TransferStatus.UNKNOWN_LENGTH;
        }
        final int headerSize;
        if(0L == cleartextFileOffset) {
            headerSize = cryptor.fileHeaderCryptor().headerSize();
        }
        else {
            headerSize = 0;
        }
        return headerSize + cryptor.fileContentCryptor().ciphertextSize(cleartextFileSize);
    }

    @Override
    public long toCleartextSize(final long cleartextFileOffset, final long ciphertextFileSize) throws CryptoInvalidFilesizeException {
        if(TransferStatus.UNKNOWN_LENGTH == ciphertextFileSize) {
            return TransferStatus.UNKNOWN_LENGTH;
        }
        final int headerSize;
        if(0L == cleartextFileOffset) {
            headerSize = cryptor.fileHeaderCryptor().headerSize();
        }
        else {
            headerSize = 0;
        }
        try {
            return cryptor.fileContentCryptor().cleartextSize(ciphertextFileSize - headerSize);
        }
        catch(AssertionError e) {
            throw new CryptoInvalidFilesizeException(String.format("Encrypted file size must be at least %d bytes", headerSize));
        }
        catch(IllegalArgumentException e) {
            throw new CryptoInvalidFilesizeException(String.format("Invalid file size. %s", e.getMessage()));
        }
    }

    private Path inflate(final Session<?> session, final Path file) throws BackgroundException {
        final String fileName = file.getName();
        if(filenameProvider.isDeflated(fileName)) {
            final String filename = filenameProvider.inflate(session, fileName);
            return new Path(file.getParent(), filename, EnumSet.of(Path.Type.file), file.attributes());
        }
        return file;
    }

    public Path getHome() {
        return home;
    }

    public Path getMasterkey() {
        return masterkey;
    }

    public Path getConfig() {
        return config;
    }

    public FileHeaderCryptor getFileHeaderCryptor() {
        return cryptor.fileHeaderCryptor();
    }

    public FileContentCryptor getFileContentCryptor() {
        return cryptor.fileContentCryptor();
    }

    public CryptorCache getFileNameCryptor() {
        return fileNameCryptor;
    }

    public CryptoFilename getFilenameProvider() {
        return filenameProvider;
    }

    public CryptoDirectory getDirectoryProvider() {
        return directoryProvider;
    }

    public int getNonceSize() {
        return nonceSize;
    }

    public int numberOfChunks(final long cleartextFileSize) {
        return (int) (cleartextFileSize / cryptor.fileContentCryptor().cleartextChunkSize() +
                ((cleartextFileSize % cryptor.fileContentCryptor().cleartextChunkSize() > 0) ? 1 : 0));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T delegate) {
        if(this.isUnlocked()) {
            if(type == ListService.class) {
                return (T) new CryptoListService(session, (ListService) delegate, this);
            }
            if(type == Touch.class) {
                // Use default touch feature because touch with remote implementation will not add encrypted file header
                return (T) new CryptoTouchFeature(session, new DefaultTouchFeature(session._getFeature(Write.class)), session._getFeature(Write.class), this);
            }
            if(type == Directory.class) {
                return (T) (vaultVersion == VAULT_VERSION_DEPRECATED ?
                        new CryptoDirectoryV6Feature(session, (Directory) delegate, session._getFeature(Write.class),
                                session._getFeature(Find.class), this) :
                        new CryptoDirectoryV7Feature(session, (Directory) delegate, session._getFeature(Write.class),
                                session._getFeature(Find.class), this));
            }
            if(type == Upload.class) {
                return (T) new CryptoUploadFeature(session, (Upload) delegate, session._getFeature(Write.class), this);
            }
            if(type == Download.class) {
                return (T) new CryptoDownloadFeature(session, (Download) delegate, session._getFeature(Read.class), this);
            }
            if(type == Read.class) {
                return (T) new CryptoReadFeature(session, (Read) delegate, this);
            }
            if(type == Write.class) {
                return (T) new CryptoWriteFeature(session, (Write) delegate, this);
            }
            if(type == MultipartWrite.class) {
                return (T) new CryptoMultipartWriteFeature(session, (Write) delegate, this);
            }
            if(type == Move.class) {
                return (T) (vaultVersion == VAULT_VERSION_DEPRECATED ?
                        new CryptoMoveV6Feature(session, (Move) delegate, this) :
                        new CryptoMoveV7Feature(session, (Move) delegate, this));
            }
            if(type == AttributesFinder.class) {
                return (T) new CryptoAttributesFeature(session, (AttributesFinder) delegate, this);
            }
            if(type == Find.class) {
                return (T) (vaultVersion == VAULT_VERSION_DEPRECATED ?
                        new CryptoFindV6Feature(session, (Find) delegate, this) :
                        new CryptoFindV7Feature(session, (Find) delegate, this));
            }
            if(type == UrlProvider.class) {
                return (T) new CryptoUrlProvider(session, (UrlProvider) delegate, this);
            }
            if(type == FileIdProvider.class) {
                return (T) new CryptoFileIdProvider(session, (FileIdProvider) delegate, this);
            }
            if(type == VersionIdProvider.class) {
                return (T) new CryptoVersionIdProvider(session, (VersionIdProvider) delegate, this);
            }
            if(type == Delete.class) {
                return (T) (vaultVersion == VAULT_VERSION_DEPRECATED ?
                        new CryptoDeleteV6Feature(session, (Delete) delegate, this) :
                        new CryptoDeleteV7Feature(session, (Delete) delegate, this));
            }
            if(type == Trash.class) {
                return (T) (vaultVersion == VAULT_VERSION_DEPRECATED ?
                        new CryptoDeleteV6Feature(session, (Delete) delegate, this) :
                        new CryptoDeleteV7Feature(session, (Delete) delegate, this));
            }
            if(type == Symlink.class) {
                return (T) new CryptoSymlinkFeature(session, (Symlink) delegate, this);
            }
            if(type == Headers.class) {
                return (T) new CryptoHeadersFeature(session, (Headers) delegate, this);
            }
            if(type == Compress.class) {
                return (T) new CryptoCompressFeature(session, (Compress) delegate, this);
            }
            if(type == Bulk.class) {
                return (T) new CryptoBulkFeature(session, (Bulk) delegate, session._getFeature(Delete.class), this);
            }
            if(type == UnixPermission.class) {
                return (T) new CryptoUnixPermission(session, (UnixPermission) delegate, this);
            }
            if(type == AclPermission.class) {
                return (T) new CryptoAclPermission(session, (AclPermission) delegate, this);
            }
            if(type == Copy.class) {
                return (T) new CryptoCopyFeature(session, (Copy) delegate, this);
            }
            if(type == Timestamp.class) {
                return (T) new CryptoTimestampFeature(session, (Timestamp) delegate, this);
            }
            if(type == Encryption.class) {
                return (T) new CryptoEncryptionFeature(session, (Encryption) delegate, this);
            }
            if(type == Lifecycle.class) {
                return (T) new CryptoLifecycleFeature(session, (Lifecycle) delegate, this);
            }
            if(type == Location.class) {
                return (T) new CryptoLocationFeature(session, (Location) delegate, this);
            }
            if(type == Lock.class) {
                return (T) new CryptoLockFeature(session, (Lock) delegate, this);
            }
            if(type == Logging.class) {
                return (T) new CryptoLoggingFeature(session, (Logging) delegate, this);
            }
            if(type == Redundancy.class) {
                return (T) new CryptoRedundancyFeature(session, (Redundancy) delegate, this);
            }
            if(type == Search.class) {
                return (T) new CryptoSearchFeature(session, (Search) delegate, this);
            }
            if(type == TransferAcceleration.class) {
                return (T) new CryptoTransferAccelerationFeature<>(session, (TransferAcceleration) delegate, this);
            }
            if(type == Versioning.class) {
                return (T) new CryptoVersioningFeature(session, (Versioning) delegate, this);
            }
        }
        return delegate;
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

    public static class VaultConfig {

        private final int version;
        private final int shorteningThreshold;
        private final CryptorProvider.Scheme cipherCombo;
        private final String algorithm;
        private final DecodedJWT token;
        private MasterkeyFile mkfile;

        private VaultConfig(int version, int shorteningThreshold, CryptorProvider.Scheme cipherCombo, String algorithm, DecodedJWT token) {
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
}