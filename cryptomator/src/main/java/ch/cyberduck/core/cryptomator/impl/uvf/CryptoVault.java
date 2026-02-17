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

import ch.cyberduck.core.AlphanumericRandomStringService;
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
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryUVFFeature;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryUVFProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.impl.VaultMetadataCredentialsProvider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.JWKCallback;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultException;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.core.vault.VaultMetadataProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.DirectoryContentCryptor;
import org.cryptomator.cryptolib.api.DirectoryMetadata;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;
import org.cryptomator.cryptolib.api.RevolvingMasterkey;
import org.cryptomator.cryptolib.api.UVFMasterkey;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.auto.service.AutoService;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWEObjectJSON;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MultiDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jose.jwk.JWK;

@AutoService(Vault.class)
public class CryptoVault extends AbstractVault {
    private static final Logger log = LogManager.getLogger(CryptoVault.class);

    private static final String REGULAR_FILE_EXTENSION = ".uvf";
    private static final String FILENAME_DIRECTORYID = "dir";
    private static final String DIRECTORY_METADATA_FILENAME = String.format("%s%s", FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);
    private static final String BACKUP_FILENAME_DIRECTORYID = "dirid";
    private static final String BACKUP_DIRECTORY_METADATA_FILENAME = String.format("%s%s", BACKUP_FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^([A-Za-z0-9_=-]+)" + REGULAR_FILE_EXTENSION);

    private static final String UVF_SPEC_VERSION_KEY_PARAM = "uvf.spec.version";
    private static final String UVF_FILEFORMAT = "AES-256-GCM-32k";
    private static final String UVF_NAME_FORMAT = "AES-SIV-512-B64URL";

    private static final int PBKDF2_SALT_LENGTH = PasswordBasedEncrypter.MIN_SALT_LENGTH;
    private static final int PBKDF2_ITERATION_COUNT = PasswordBasedEncrypter.MIN_RECOMMENDED_ITERATION_COUNT;

    private final PasswordStore keychain = PasswordStoreFactory.get();
    private final Preferences preferences = PreferencesFactory.get();

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

    public CryptoVault(final Path home) {
        this.home = home;
        this.masterkeyPath = new Path(home, preferences.getProperty("cryptomator.vault.config.filename.uvf"), EnumSet.of(Path.Type.file, Path.Type.vault));
    }

    @Override
    public AbstractVault create(final Session<?> session, final String region, final VaultMetadataProvider metadata) throws BackgroundException {
        // Passphrase based vault creation
        if(metadata instanceof VaultMetadataCredentialsProvider) {
            final VaultMetadataCredentialsProvider credentialsProvider = VaultMetadataCredentialsProvider.cast(metadata);
            final VaultCredentials credentials = credentialsProvider.getCredentials();
            try {
                final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256GCM)
                        .jwkURL(URI.create("jwks.json"))
                        .contentType("json")
                        // Probably a bug - refer to https://bitbucket.org/connect2id/nimbus-jose-jwt/issues/610/critical-headers-and
                        //.criticalParams(Collections.singleton(UVF_SPEC_VERSION_KEY_PARAM))
                        //.customParam(UVF_SPEC_VERSION_KEY_PARAM, 1)
                        .keyID("org.cryptomator.uvf.vaultpassword")
                        .build();
                final String kid = Base64.getUrlEncoder().encodeToString(new AlphanumericRandomStringService(4).random().getBytes(StandardCharsets.UTF_8));
                final byte[] rawSeed = new byte[32];
                FastSecureRandomProvider.get().provide().nextBytes(rawSeed);
                final byte[] kdfSalt = new byte[32];
                FastSecureRandomProvider.get().provide().nextBytes(kdfSalt);
                final Payload payload = new Payload(new HashMap<String, Object>() {{
                    put("fileFormat", UVF_FILEFORMAT);
                    put("nameFormat", UVF_NAME_FORMAT);
                    put("seeds", new HashMap<String, String>() {{
                        put(kid, Base64.getUrlEncoder().encodeToString(rawSeed));
                    }});
                    put("initialSeed", kid);
                    put("latestSeed", kid);
                    put("kdf", "HKDF-SHA512");
                    put("kdfSalt", Base64.getUrlEncoder().encodeToString(kdfSalt));
                }});
                final JWEObject jwe = new JWEObject(header, payload);
                jwe.encrypt(new PasswordBasedEncrypter(credentials.getPassword(), PBKDF2_SALT_LENGTH, PBKDF2_ITERATION_COUNT));
                final byte[] encryptedMetadata = jwe.serialize().getBytes(StandardCharsets.US_ASCII);
                this.uploadTemplate(session, region, this.computeRootDirIdHash(payload.toString()), this.computeRootDirUvf(payload.toString()), encryptedMetadata);
            }
            catch(JOSEException | JsonProcessingException e) {
                throw new VaultException("Failure creating vault ", e);
            }
            return this;
        }

        // Generic vault creation with provided metadata
        if(metadata instanceof VaultMetadataUVFProvider) {
            final VaultMetadataUVFProvider provider = VaultMetadataUVFProvider.cast(metadata);
            this.uploadTemplate(session, region, provider.getDirPath(), provider.getMetadata(), provider.getRootDirectoryMetadata());
            return this;
        }

        throw new VaultException("Unsupported metadata provider: " + metadata.getClass().getName());
    }

    private void uploadTemplate(final Session<?> session, final String region, final String dirPath, final byte[] vaultUvf, final byte[] dirUvf) throws BackgroundException {
        final Path home = this.getHome();
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
        final Path firstLevel = new Path(dataDir, dirPath.substring(0, 2), EnumSet.of(Path.Type.directory));
        final Path secondLevel = new Path(firstLevel, dirPath.substring(2), EnumSet.of(Path.Type.directory));

        directory.mkdir(session._getFeature(Write.class), dataDir, status);
        directory.mkdir(session._getFeature(Write.class), firstLevel, status);
        directory.mkdir(session._getFeature(Write.class), secondLevel, status);

        // vault.uvf
        new ContentWriter(session).write(new Path(home, PreferencesFactory.get().getProperty("cryptomator.vault.config.filename"),
                EnumSet.of(Path.Type.file, Path.Type.vault)), vaultUvf);
        // dir.uvf
        new ContentWriter(session).write(new Path(secondLevel, "dir.uvf", EnumSet.of(Path.Type.file)), dirUvf);
    }

    private String computeRootDirIdHash(final String payloadJSON) throws JsonProcessingException {
        final UVFMasterkey masterKey = UVFMasterkey.fromDecryptedPayload(payloadJSON);
        final CryptorProvider provider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
        final Cryptor cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        final byte[] rootDirId = masterKey.rootDirId();
        return cryptor.fileNameCryptor(masterKey.firstRevision()).hashDirectoryId(rootDirId);
    }

    private byte[] computeRootDirUvf(final String payloadJSON) throws JsonProcessingException {
        final UVFMasterkey masterKey = UVFMasterkey.fromDecryptedPayload(payloadJSON);
        final CryptorProvider provider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
        final Cryptor cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        DirectoryMetadata rootDirMetadata = cryptor.directoryContentCryptor().rootDirectoryMetadata();
        DirectoryContentCryptor dirContentCryptor = cryptor.directoryContentCryptor();
        return dirContentCryptor.encryptDirectoryMetadata(rootDirMetadata);
    }

    // load -> unlock -> open
    @Override
    public CryptoVault load(final Session<?> session, final PasswordCallback callback, final VaultMetadataProvider metadata) throws BackgroundException {
        final Payload payload;

        if(metadata instanceof VaultMetadataCredentialsProvider) {
            final Host bookmark = session.getHost();
            String passphrase = keychain.getPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                    new DefaultUrlProvider(bookmark).toUrl(masterkeyPath, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl());
            payload = this.unlock(session, callback, bookmark, passphrase).getPayload();
        }
        else if(metadata instanceof VaultMetadataUVFProvider) {
            final VaultMetadataUVFProvider metadataProvider = VaultMetadataUVFProvider.cast(metadata);

            try {
                final String jwe = new String(metadataProvider.getMetadata(), StandardCharsets.US_ASCII);
                final JWK jwk = JWKCallback.cast(callback).prompt(session.getHost(), StringUtils.EMPTY, StringUtils.EMPTY, new LoginOptions()).getKey();
                final JWEObjectJSON jweObject = JWEObjectJSON.parse(jwe);
                jweObject.decrypt(new MultiDecrypter(jwk, Collections.singleton(UVF_SPEC_VERSION_KEY_PARAM)));
                payload = jweObject.getPayload();
            }
            catch(ParseException | JOSEException e) {
                throw new VaultException("Failure retrieving key material", e);
            }
        }

        else {
            throw new VaultException("Unsupported metadata provider: " + metadata.getClass().getName());
        }

        masterKey = UVFMasterkey.fromDecryptedPayload(payload.toString());
        final CryptorProvider provider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
        log.debug("Initialized crypto provider {}", provider);
        this.cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        this.filenameProvider = new CryptoFilenameV7Provider(Integer.MAX_VALUE);
        this.directoryProvider = new CryptoDirectoryUVFProvider(this, filenameProvider);
        this.nonceSize = 12;
        return this;
    }

    public JWEObject unlock(final Session<?> session, final PasswordCallback prompt, final Host bookmark, final String passphrase) throws BackgroundException {
        log.debug("Read UVF metadata {}", masterkeyPath);
        final String vaultUVF = new ContentReader(session).read(masterkeyPath);
        return this.unlock(vaultUVF, passphrase, bookmark, prompt,
                MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), home.getName())
        );
    }

    private JWEObject unlock(final String vaultUVF, final String passphrase, final Host bookmark, final PasswordCallback prompt,
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
            final JWEObject jweObject;
            try {

                jweObject = JWEObject.parse(vaultUVF);
                ;
                jweObject.decrypt(new PasswordBasedDecrypter(credentials.getPassword()));
            }
            catch(ParseException e) {
                throw new VaultException("Failure retrieving key material", e);
            }
            if(credentials.isSaved()) {
                log.info("Save passphrase for {}", masterkeyPath);
                // Save password with hostname and path to masterkey.cryptomator in keychain
                keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                        new DefaultUrlProvider(bookmark).toUrl(masterkeyPath, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
            }
            return jweObject;
        }
        catch(JOSEException e) {
            return this.unlock(vaultUVF, null, bookmark, prompt, String.format("%s %s.", e.getMessage(),
                    MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), home.getName())));
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
        //TODO: implement
        return null;
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

    public DirectoryMetadata getRootDirId() {
        return this.cryptor.directoryContentCryptor().rootDirectoryMetadata();
    }

    @Override
    public VaultMetadata getMetadata() {
        return new VaultMetadata(this.getHome(), VaultMetadata.Type.UVF);
    }

    @Override
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T delegate) throws UnsupportedException {
        if(type == Directory.class) {
            return (T) new CryptoDirectoryUVFFeature(session, (Directory) delegate, this);
        }
        return super.getFeature(session, type, delegate);
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
        final StringBuilder sb = new StringBuilder("UVFVault{");
        sb.append("home=").append(home);
        sb.append(", cryptor=").append(cryptor);
        sb.append('}');
        return sb.toString();
    }
}
