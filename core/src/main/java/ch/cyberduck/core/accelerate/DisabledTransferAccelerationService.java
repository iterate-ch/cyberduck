package ch.cyberduck.core.accelerate;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

public class DisabledTransferAccelerationService<C extends HttpSession<?>> implements TransferAccelerationService<C> {
    @Override
    public boolean prompt(final Host bookmark, final Path file, final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        return false;
    }

    @Override
    public C open(final Host bookmark, final Path file, final X509TrustManager trust, final X509KeyManager key) throws BackgroundException {
        throw new ConnectionCanceledException();
    }
}
