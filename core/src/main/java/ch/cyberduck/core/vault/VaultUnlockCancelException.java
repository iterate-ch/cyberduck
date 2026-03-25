package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConnectionCanceledException;

public class VaultUnlockCancelException extends ConnectionCanceledException {
    private final Path vault;

    public VaultUnlockCancelException(final Path vault) {
        this.vault = vault;
    }

    public VaultUnlockCancelException(final Path vault, final Throwable cause) {
        super(cause);
        this.vault = vault;
    }

    public Path getVault() {
        return vault;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultUnlockCancelException{");
        sb.append("vault=").append(vault);
        sb.append('}');
        return sb.toString();
    }
}
