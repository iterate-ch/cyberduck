package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;

import java.io.IOException;

public class CryptoTouchFeature implements Touch {

    private final Touch delegate;
    private final CryptoVault cryptomator;

    public CryptoTouchFeature(final Touch delegate, final CryptoVault cryptomator) {
        this.delegate = delegate;
        this.cryptomator = cryptomator;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        try {
            final Path encrypted = cryptomator.encrypt(file);
            delegate.touch(encrypted);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return delegate.isSupported(workdir);
    }
}
