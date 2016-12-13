package ch.cyberduck.core.cryptomator.worker;

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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.DisabledVaultLookupListener;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.worker.Worker;

import java.text.MessageFormat;
import java.util.Objects;

public class CreateVaultWorker extends Worker<Boolean> {

    private final Path directory;
    private final String region;
    private final PasswordStore keychain;
    private final PasswordCallback prompt;

    public CreateVaultWorker(final Path directory, final String region, final PasswordStore keychain, final PasswordCallback prompt) {
        this.directory = directory;
        this.region = region;
        this.keychain = keychain;
        this.prompt = prompt;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        try {
            new CryptoVault(directory, keychain, prompt, new DisabledVaultLookupListener()).create(session, region).close();
        }
        catch(LoginCanceledException e) {
            return false;
        }
        return true;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"),
                directory.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof CreateVaultWorker)) {
            return false;
        }
        final CreateVaultWorker that = (CreateVaultWorker) o;
        return Objects.equals(directory, that.directory) &&
                Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory, region);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateVaultWorker{");
        sb.append("directory=").append(directory);
        sb.append(", region='").append(region).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
