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
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;

public class PasswordVaultLoader implements VaultLoader {
    private static final Logger log = LogManager.getLogger(PasswordVaultLoader.class);

    private final PasswordStore keychain = PasswordStoreFactory.get();
    private final VaultRegistry registry;
    private final PasswordCallback prompt;

    public PasswordVaultLoader(final VaultRegistry registry, final PasswordCallback prompt) {
        this.registry = registry;
        this.prompt = prompt;
    }

    @Override
    public Vault load(final Session<?> session, final Path directory, final VaultVersion version) throws VaultUnlockCancelException {
        synchronized(registry) {
            if(registry.contains(directory)) {
                return registry.find(session, directory);
            }
            log.info("Loading vault for session {}", session);
            try {
                final Host bookmark = session.getHost();
                String passphrase = keychain.getPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                        new DefaultUrlProvider(bookmark).toUrl(directory, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl());
                if(null == passphrase) {
                    final Path masterKey = new Path(directory, HostPreferencesFactory.get(bookmark).getProperty("cryptomator.vault.masterkey.filename"), EnumSet.of(Path.Type.file, Path.Type.vaultmetadata));
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
                return this.load(session, version, directory, credentials);
            }
            catch(BackgroundException e) {
                log.warn("Failure {} loading vault", e.getMessage());
                throw new VaultUnlockCancelException(directory, e);
            }
        }
    }

    private Vault load(final Session<?> session, final VaultVersion version, final Path directory, final VaultCredentials credentials) throws BackgroundException {
        try {
            final VaultProvider provider = session.getFeature(VaultProvider.class);
            final Vault vault = provider.load(session, directory, version, credentials);
            if(credentials.isSaved()) {
                log.info("Save passphrase for {}", directory);
                // Save password with hostname and path to masterkey.cryptomator in keychain
                keychain.addPassword(String.format("Cryptomator Passphrase (%s)", session.getHost().getCredentials().getUsername()),
                        new DefaultUrlProvider(session.getHost()).toUrl(directory, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
            }
            return vault;
        }
        catch(VaultUnlockException e) {
            final Host bookmark = session.getHost();
            credentials.setPassword(prompt.prompt(
                    bookmark, LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                    String.format("%s %s.", e.getDetail(),
                            MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault {0}", "Cryptomator"), directory)),
                    new LoginOptions()
                            .save(HostPreferencesFactory.get(bookmark).getBoolean("cryptomator.vault.keychain"))
                            .user(false)
                            .anonymous(false)
                            .icon("cryptomator.tiff")
                            .passwordPlaceholder(LocaleFactory.localizedString("Passphrase", "Cryptomator"))).getPassword());
            return this.load(session, version, directory, credentials);
        }
        catch(BackgroundException e) {
            throw new VaultUnlockCancelException(directory, e);
        }
    }
}
