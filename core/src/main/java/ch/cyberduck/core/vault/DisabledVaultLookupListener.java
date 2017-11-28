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

import ch.cyberduck.core.features.Vault;

import org.apache.log4j.Logger;

public final class DisabledVaultLookupListener implements VaultLookupListener {
    private static final Logger log = Logger.getLogger(DisabledVaultLookupListener.class);

    @Override
    public void found(final Vault vault) {
        log.warn(String.format("Ignore vault %s", vault));
    }
}
