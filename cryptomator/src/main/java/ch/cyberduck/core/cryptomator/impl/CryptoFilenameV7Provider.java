package ch.cyberduck.core.cryptomator.impl;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.CryptoInvalidFilenameException;
import ch.cyberduck.core.exception.BackgroundException;

public class CryptoFilenameV7Provider implements CryptoFilename {

    public static final int DEFAULT_NAME_SHORTENING_THRESHOLD = 220; // https://github.com/cryptomator/cryptofs/issues/60#issuecomment-523238303

    private final AbstractVault vault;
    /**
     * Maximum length of encrypted filenames
     */
    private final int threshold;

    public CryptoFilenameV7Provider(final AbstractVault vault) {
        this(vault, DEFAULT_NAME_SHORTENING_THRESHOLD);
    }

    public CryptoFilenameV7Provider(final AbstractVault vault, final int threshold) {
        this.vault = vault;
        this.threshold = threshold;
    }

    @Override
    public boolean isValid(final String filename) {
        // The length of the encrypted name does not depend on the directory, encrypt with root directory to avoid lookup of parent
        return this.isBelowThreshold(vault.getRootDirectory().encryptName(filename).name());
    }

    @Override
    public String deflate(final Session<?> session, final String filename) throws BackgroundException {
        if(this.isBelowThreshold(filename)) {
            return filename;
        }
        throw new CryptoInvalidFilenameException(String.format("Encrypted filename with length %d exceeds maximum of %d characters supported by vault",
                filename.length(), threshold));
    }

    @Override
    public void destroy() {
        //
    }

    private boolean isBelowThreshold(final String filename) {
        return filename.length() <= threshold;
    }
}
