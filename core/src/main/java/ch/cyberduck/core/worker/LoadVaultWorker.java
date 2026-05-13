package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.VaultLoader;
import ch.cyberduck.core.vault.VaultProvider;
import ch.cyberduck.core.vault.VaultProviderFactory;

import java.util.Objects;

public class LoadVaultWorker extends Worker<Vault> {

    private final VaultLoader loader;
    private final Path directory;

    public LoadVaultWorker(final VaultLoader loader, final Path directory) {
        this.loader = loader;
        this.directory = directory;
    }

    @Override
    public Vault run(final Session<?> session) throws BackgroundException {
        final VaultProvider provider = VaultProviderFactory.get(session);
        return loader.load(session, directory,
                provider.find(directory, session.getFeature(Find.class), new DisabledListProgressListener()));
    }

    @Override
    public String getActivity() {
        return LocaleFactory.localizedString("Unlock Vault", "Cryptomator");
    }

    @Override
    public boolean equals(final Object o) {
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        LoadVaultWorker that = (LoadVaultWorker) o;
        return Objects.equals(directory, that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(directory);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LoadVaultWorker{");
        sb.append("loader=").append(loader);
        sb.append(", directory=").append(directory);
        sb.append('}');
        return sb.toString();
    }
}
