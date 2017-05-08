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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.VaultCredentials;

import java.text.MessageFormat;
import java.util.Objects;

public class CreateVaultWorker extends Worker<Path> {

    private final String region;
    private final VaultCredentials passphrase;
    private final Vault vault;

    public CreateVaultWorker(final String region, final VaultCredentials passphrase, final Vault vault) {
        this.region = region;
        this.passphrase = passphrase;
        this.vault = vault;
    }

    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        final Vault vault = this.vault.create(session, region, passphrase);
        vault.close();
        return vault.getHome();
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"),
                vault.getHome());
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
        return Objects.equals(vault, that.vault);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vault);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateVaultWorker{");
        sb.append("vault=").append(vault);
        sb.append('}');
        return sb.toString();
    }
}
