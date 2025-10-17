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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.core.vault.VaultProviderFactory;

import java.text.MessageFormat;
import java.util.Objects;

public class CreateVaultWorker extends Worker<Vault> {

    private final String region;
    private final VaultCredentials passphrase;
    private final VaultMetadata metadata;

    public CreateVaultWorker(final String region, final VaultCredentials passphrase, final VaultMetadata metadata) {
        this.region = region;
        this.passphrase = passphrase;
        this.metadata = metadata;
    }

    @Override
    public Vault run(final Session<?> session) throws BackgroundException {
        final Vault vault = VaultProviderFactory.get(session).create(session, region, passphrase, metadata);
        vault.close();
        return vault;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"), metadata.root.getName());
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
