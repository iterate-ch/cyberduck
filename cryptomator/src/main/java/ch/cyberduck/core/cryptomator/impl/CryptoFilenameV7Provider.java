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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.CryptoInvalidFilenameException;
import ch.cyberduck.core.exception.BackgroundException;

public class CryptoFilenameV7Provider implements CryptoFilename {

    public static final int DEFAULT_NAME_SHORTENING_THRESHOLD = 220; // https://github.com/cryptomator/cryptofs/issues/60#issuecomment-523238303
    private final int threshold;

    public CryptoFilenameV7Provider() {
        this(DEFAULT_NAME_SHORTENING_THRESHOLD);
    }

    public CryptoFilenameV7Provider(final int threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isDeflated(final String filename) {
        return false;
    }

    @Override
    public boolean isValid(final String filename) {
        return this.isBelowThreshold(filename);
    }

    @Override
    public String inflate(final Session<?> session, final String shortName) throws BackgroundException {
        return shortName;
    }

    @Override
    public String deflate(final Session<?> session, final String filename) throws BackgroundException {
        if(this.isBelowThreshold(filename)) {
            return filename;
        }
        throw new CryptoInvalidFilenameException(String.format("Filename length %d exceeds maximum length %d", filename.length(), DEFAULT_NAME_SHORTENING_THRESHOLD));
    }

    @Override
    public Path resolve(final String filename) {
        return null;
    }

    @Override
    public void invalidate(final String filename) {
        //
    }

    @Override
    public void destroy() {
        //
    }

    private boolean isBelowThreshold(final String filename) {
        return filename.length() <= threshold;
    }
}
