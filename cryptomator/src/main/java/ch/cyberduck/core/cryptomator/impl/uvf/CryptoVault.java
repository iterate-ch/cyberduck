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

import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryUVFFeature;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryUVFProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.JWKCallback;
import ch.cyberduck.core.vault.VaultException;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.core.vault.VaultMetadataProvider;

import org.apache.commons.lang3.StringUtils;
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
import java.text.ParseException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.auto.service.AutoService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObjectJSON;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MultiDecrypter;
import com.nimbusds.jose.jwk.JWK;

@AutoService(Vault.class)
public class CryptoVault extends AbstractVault {
    private static final Logger log = LogManager.getLogger(CryptoVault.class);

    private static final String UVF_SPEC_VERSION_KEY_PARAM = "uvf.spec.version";

    private static final String REGULAR_FILE_EXTENSION = ".uvf";
    private static final String FILENAME_DIRECTORYID = "dir";
    private static final String DIRECTORY_METADATA_FILENAME = String.format("%s%s", FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);
    private static final String BACKUP_FILENAME_DIRECTORYID = "dirid";
    private static final String BACKUP_DIRECTORY_METADATA_FILENAME = String.format("%s%s", BACKUP_FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^([A-Za-z0-9_=-]+)" + REGULAR_FILE_EXTENSION);

    /**
     * Root of vault directory
     */
    private final Path home;

    private RevolvingMasterkey masterKey;

    private Cryptor cryptor;
    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    private int nonceSize;

    public CryptoVault(final Path home) {
        this.home = home;
    }

    @Override
    public AbstractVault create(final Session<?> session, final String region, final VaultMetadataProvider metadata) throws BackgroundException {
        final VaultMetadataUVFProvider provider = VaultMetadataUVFProvider.cast(metadata);

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
        final Path firstLevel = new Path(dataDir, provider.getDirPath().substring(0, 2), EnumSet.of(Path.Type.directory));
        final Path secondLevel = new Path(firstLevel, provider.getDirPath().substring(2), EnumSet.of(Path.Type.directory));

        directory.mkdir(session._getFeature(Write.class), dataDir, status);
        directory.mkdir(session._getFeature(Write.class), firstLevel, status);
        directory.mkdir(session._getFeature(Write.class), secondLevel, status);

        // vault.uvf
        new ContentWriter(session).write(new Path(home, PreferencesFactory.get().getProperty("cryptomator.vault.config.filename"),
                EnumSet.of(Path.Type.file, Path.Type.vault)), provider.getMetadata());
        // dir.uvf
        new ContentWriter(session).write(new Path(secondLevel, "dir.uvf", EnumSet.of(Path.Type.file)),
                provider.getRootDirectoryMetadata());

        return this;
    }

    public static String decryptWithJWK(final String jwe, final JWK jwk) throws ParseException, JOSEException, JsonProcessingException, VaultException {
        final JWEObjectJSON jweObject = JWEObjectJSON.parse(jwe);
        jweObject.decrypt(new MultiDecrypter(jwk, Collections.singleton(UVF_SPEC_VERSION_KEY_PARAM)));

        // https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.11
        // Recipients MAY consider the JWS to be invalid if the critical
        // list contains any Header Parameter names defined by this
        // specification or [JWA] for use with JWS or if any other constraints on its use are violated.
        final Object uvfSpecVersion = jweObject.getHeader().getCustomParams().get(UVF_SPEC_VERSION_KEY_PARAM);
        if(uvfSpecVersion.equals(1)) {
            throw new VaultException(String.format("Unexpected value for critical header %s: found %s, expected \"1\"", UVF_SPEC_VERSION_KEY_PARAM, uvfSpecVersion));
        }

        final Payload payload = jweObject.getPayload();
        return payload.toString();
    }


    // load -> unlock -> open
    @Override
    public CryptoVault load(final Session<?> session, final PasswordCallback callback, final VaultMetadataProvider metadata) throws BackgroundException {
        final JWKCallback jwkCallback = JWKCallback.cast(callback);
        final VaultMetadataUVFProvider metadataProvider = VaultMetadataUVFProvider.cast(metadata);
        final String uvfMetadata;
        try {
            final String jwe = new String(metadataProvider.getMetadata(), StandardCharsets.US_ASCII);
            final JWK jwk = jwkCallback.prompt(session.getHost(), StringUtils.EMPTY, StringUtils.EMPTY, new LoginOptions()).getKey();
            uvfMetadata = decryptWithJWK(jwe, jwk);
        }
        catch(ParseException | JOSEException | JsonProcessingException e) {
            throw new VaultException("Failure retrieving key material", e);
        }

        masterKey = UVFMasterkey.fromDecryptedPayload(uvfMetadata);
        final CryptorProvider provider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
        log.debug("Initialized crypto provider {}", provider);
        this.cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        this.filenameProvider = new CryptoFilenameV7Provider(Integer.MAX_VALUE);
        this.directoryProvider = new CryptoDirectoryUVFProvider(this, filenameProvider);
        this.nonceSize = 12;
        return this;
    }

    @Override
    public Path encrypt(Session<?> session, Path file, boolean metadata) throws BackgroundException {
        final Path encrypted;
        if(file.isFile() || metadata) {
            if(file.getType().contains(Path.Type.vault)) {
                log.warn("Skip file {} because it is marked as an internal vault path", file);
                return file;
            }
            if(new SimplePathPredicate(file).test(this.getHome())) {
                log.warn("Skip vault home {} because the root has no metadata file", file);
                return file;
            }
            final Path parent;
            final String filename;
            if(file.getType().contains(Path.Type.encrypted)) {
                final Path decrypted = file.attributes().getDecrypted();
                parent = this.getDirectoryProvider().toEncrypted(session, decrypted.getParent());
                filename = this.getDirectoryProvider().toEncrypted(session, decrypted.getParent(), decrypted.getName(), decrypted.getType());
            }
            else {
                parent = this.getDirectoryProvider().toEncrypted(session, file.getParent());
                // / diff to AbstractVault.encrypt
                filename = this.getDirectoryProvider().toEncrypted(session, file.getParent(), file.getName(), file.getType());
                // \ diff to AbstractVault.decrypt
            }
            final PathAttributes attributes = new PathAttributes(file.attributes());
            if(!file.isFile() && !metadata) {
                // The directory is different from the metadata file used to resolve the actual folder
                attributes.setVersionId(null);
                attributes.setFileId(null);
            }
            // Translate file size
            attributes.setSize(this.toCiphertextSize(0L, file.attributes().getSize()));
            final EnumSet<Path.Type> type = EnumSet.copyOf(file.getType());
            type.remove(Path.Type.decrypted);
            type.add(Path.Type.encrypted);
            encrypted = new Path(parent, filename, type, attributes);
        }
        else {
            if(file.getType().contains(Path.Type.encrypted)) {
                log.warn("Skip file {} because it is already marked as an encrypted path", file);
                return file;
            }
            if(file.getType().contains(Path.Type.vault)) {
                return this.getDirectoryProvider().toEncrypted(session, this.getHome());
            }
            encrypted = this.getDirectoryProvider().toEncrypted(session, file);
        }
        // Add reference to decrypted file
        if(!file.getType().contains(Path.Type.encrypted)) {
            encrypted.attributes().setDecrypted(file);
        }
        // Add reference for vault
        file.attributes().setVaultMetadata(this.getMetadata());
        encrypted.attributes().setVaultMetadata(this.getMetadata());
        return encrypted;
    }

    @Override
    public synchronized void close() {
        super.close();
        cryptor.destroy();
    }

    @Override
    public Path getMasterkeyPath() {
        //TODO: implement
        return null;
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
