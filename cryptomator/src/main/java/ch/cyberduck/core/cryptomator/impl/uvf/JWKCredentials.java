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

import ch.cyberduck.core.vault.VaultCredentials;

import com.nimbusds.jose.jwk.JWK;

public class JWKCredentials extends VaultCredentials {

    private final JWK keyset;

    public JWKCredentials(final JWK keyset) {
        super(null);
        this.keyset = keyset;
    }

    public JWK getKeyset() {
        return keyset;
    }
}
