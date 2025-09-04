package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryUVFFeature;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryUVFProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;
import org.cryptomator.cryptolib.api.RevolvingMasterkey;
import org.cryptomator.cryptolib.api.UVFMasterkey;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;

public class UVFVault extends AbstractVault {

    private static final Logger log = LogManager.getLogger(UVFVault.class);

    private static final String REGULAR_FILE_EXTENSION = ".uvf";
    private static final String FILENAME_DIRECTORYID = "dir";
    private static final String DIRECTORY_METADATA_FILENAME = String.format("%s%s", FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);
    private static final String BACKUP_FILENAME_DIRECTORYID = "dirid";
    private static final String BACKUP_DIRECTORY_METADATA_FILENAME = String.format("%s%s", BACKUP_FILENAME_DIRECTORYID, REGULAR_FILE_EXTENSION);

    private static final Pattern BASE64URL_PATTERN = Pattern.compile("^([A-Za-z0-9_=-]+)" + REGULAR_FILE_EXTENSION);

    /**
     * Root of vault directory
     */
    protected final Path home;

    private RevolvingMasterkey masterKey;

    private Cryptor cryptor;
    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    private int nonceSize;
    private byte[] rootDirId;

    public UVFVault(final Path home) {
        this.home = home;
    }

    @Override
    public Path create(final Session<?> session, final String region, final VaultCredentials credentials) throws BackgroundException {
        throw new UnsupportedOperationException();
    }

    // load -> unlock -> open
    @Override
    public UVFVault load(final Session<?> session, final PasswordCallback prompt) throws BackgroundException {
        masterKey = UVFMasterkey.fromDecryptedPayload(prompt.prompt(session.getHost(),
                LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), home.getName()),
                new LoginOptions()
                        .save(false)
                        .user(false)
                        .anonymous(false)
                        .icon("cryptomator.tiff")
                        .passwordPlaceholder(LocaleFactory.localizedString("Passphrase", "Cryptomator"))).getPassword());
        final CryptorProvider provider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
        log.debug("Initialized crypto provider {}", provider);
        this.cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        this.filenameProvider = new CryptoFilenameV7Provider(Integer.MAX_VALUE);
        this.directoryProvider = new CryptoDirectoryUVFProvider(this, filenameProvider);
        this.nonceSize = 12;
        this.rootDirId = masterKey.rootDirId();
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
            if(metadata && this.getVersion() == VAULT_VERSION_DEPRECATED) {
                type.remove(Path.Type.directory);
                type.add(Path.Type.file);
            }
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
        file.attributes().setVault(this.getHome());
        encrypted.attributes().setVault(this.getHome());
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
    public int getVersion() {
        return VAULT_VERSION;
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
    public Pattern getBase64URLPattern() {
        return BASE64URL_PATTERN;
    }

    public byte[] getRootDirId() {
        return rootDirId;
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
