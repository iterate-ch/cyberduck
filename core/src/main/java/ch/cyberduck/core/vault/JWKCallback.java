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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.exception.LoginCanceledException;

public interface JWKCallback extends PasswordCallback {

    @Override
    JWKCredentials prompt(Host bookmark, String title, String reason, LoginOptions options) throws LoginCanceledException;

    static JWKCallback cast(PasswordCallback callback) {
        if(callback instanceof JWKCallback) {
            return (JWKCallback) callback;
        }
        else {
            throw new IllegalArgumentException("Unsupported metadata type " + callback.getClass());
        }
    }
}
