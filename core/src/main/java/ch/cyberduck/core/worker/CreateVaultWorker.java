package ch.cyberduck.core.worker;

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
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultProvider;
import ch.cyberduck.core.vault.VaultVersion;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Objects;

public class CreateVaultWorker extends Worker<Vault> {

    private final PasswordStore keychain = PasswordStoreFactory.get();
    private final String region;
    private final Path directory;
    private final VaultCredentials passphrase;
    private final VaultVersion metadata;

    public CreateVaultWorker(final String region, final Path directory, final VaultCredentials passphrase, final VaultVersion metadata) {
        this.region = region;
        this.directory = directory;
        this.passphrase = passphrase;
        this.metadata = metadata;
    }

    @Override
    public Vault run(final Session<?> session) throws BackgroundException {
        final VaultProvider provider = session.getFeature(VaultProvider.class);
        final Vault vault = provider.create(session, region, directory, metadata, passphrase);
        if(passphrase.isSaved()) {
            final Host bookmark = session.getHost();
            keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                    new DefaultUrlProvider(bookmark).toUrl(directory, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl(), passphrase.getPassword());
        }
        return vault;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"), directory.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final CreateVaultWorker that = (CreateVaultWorker) o;
        return Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateVaultWorker{");
        sb.append("metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }
}
