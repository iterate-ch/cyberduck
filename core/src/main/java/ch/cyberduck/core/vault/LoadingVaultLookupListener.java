package ch.cyberduck.core.vault;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;

public class LoadingVaultLookupListener implements VaultLookupListener {
    private static final Logger log = LogManager.getLogger(LoadingVaultLookupListener.class);

    private final PasswordStore keychain = PasswordStoreFactory.get();
    private final VaultRegistry registry;
    private final PasswordCallback prompt;

    public LoadingVaultLookupListener(final VaultRegistry registry, final PasswordCallback prompt) {
        this.registry = registry;
        this.prompt = prompt;
    }

    @Override
    public Vault load(final Session<?> session, final Path directory, final VaultMetadata metadata) throws VaultUnlockCancelException {
        synchronized(registry) {
            if(registry.contains(directory)) {
                return registry.find(session, directory);
            }
            log.info("Loading vault for session {}", session);
            final Vault vault;
            try {
                vault = session.getFeature(VaultProvider.class).provide(session, directory, metadata);
            }
            catch(UnsupportedException e) {
                throw new VaultUnlockCancelException(Vault.DISABLED, e);
            }
            try {
                final Host bookmark = session.getHost();
                String passphrase = keychain.getPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                        new DefaultUrlProvider(bookmark).toUrl(directory, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl());
                if(null == passphrase) {
                    final Path masterKey = new Path(directory, HostPreferencesFactory.get(bookmark).getProperty("cryptomator.vault.masterkey.filename"), EnumSet.of(Path.Type.file, Path.Type.vault));
                    passphrase = keychain.getPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                            new DefaultUrlProvider(bookmark).toUrl(masterKey).find(DescriptiveUrl.Type.provider).getUrl());
                    if(null == passphrase) {
                        // Legacy
                        passphrase = keychain.getPassword(String.format("Cryptomator Passphrase %s", bookmark.getHostname()),
                                new DefaultUrlProvider(bookmark).toUrl(masterKey, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl());
                    }
                }
                final VaultCredentials credentials;
                if(null == passphrase) {
                    credentials = new VaultCredentials(prompt.prompt(
                            bookmark, LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                            MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), directory.getName()),
                            new LoginOptions()
                                    .save(HostPreferencesFactory.get(bookmark).getBoolean("cryptomator.vault.keychain"))
                                    .user(false)
                                    .anonymous(false)
                                    .icon("cryptomator.tiff")
                                    .passwordPlaceholder(LocaleFactory.localizedString("Passphrase", "Cryptomator"))).getPassword());
                }
                else {
                    credentials = new VaultCredentials(passphrase).setSaved(false);
                }
                this.load(session, vault, credentials);
                if(credentials.isSaved()) {
                    log.info("Save passphrase for {}", directory);
                    // Save password with hostname and path to masterkey.cryptomator in keychain
                    keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                            new DefaultUrlProvider(bookmark).toUrl(directory, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
                }
                if(registry.add(vault)) {
                    final EnumSet<Path.Type> type = directory.getType();
                    type.add(Path.Type.vault);
                    directory.setType(type);
                }
            }
            catch(BackgroundException e) {
                log.warn("Failure {} loading vault", e.getMessage());
                throw new VaultUnlockCancelException(vault, e);
            }
            return vault;
        }
    }

    private void load(final Session<?> session, final Vault vault, final VaultCredentials credentials) throws BackgroundException {
        try {
            vault.load(session, new DefaultVaultMetadataCredentialsProvider(credentials));
        }
        catch(VaultUnlockException e) {
            final Host bookmark = session.getHost();
            credentials.setPassword(prompt.prompt(
                    bookmark, LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                    String.format("%s %s.", e.getDetail(),
                            MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), vault.getHome())),
                    new LoginOptions()
                            .save(HostPreferencesFactory.get(bookmark).getBoolean("cryptomator.vault.keychain"))
                            .user(false)
                            .anonymous(false)
                            .icon("cryptomator.tiff")
                            .passwordPlaceholder(LocaleFactory.localizedString("Passphrase", "Cryptomator"))).getPassword());
            this.load(session, vault, credentials);
        }
    }
}
