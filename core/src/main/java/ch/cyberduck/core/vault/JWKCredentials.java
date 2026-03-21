package ch.cyberduck.core.vault;

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

import com.nimbusds.jose.jwk.JWK;

public class JWKCredentials extends VaultCredentials {

    /**
     * This variable holds the JSON Web Key (JWK) used for cryptographic operations,
     * such as signing or encrypting data.
     */
    private final JWK key;

    public JWKCredentials(final JWK key) {
        super(null);
        this.key = key;
    }

    public JWK getKey() {
        return key;
    }
}
