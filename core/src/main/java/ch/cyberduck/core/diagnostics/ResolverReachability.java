package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Resolver;
import ch.cyberduck.core.exception.ResolveCanceledException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.threading.CancelCallback;

public class ResolverReachability extends DisabledReachability {

    private final Resolver resolver = new Resolver();

    @Override
    public boolean isReachable(final Host bookmark) {
        try {
            resolver.resolve(bookmark.getHostname(), CancelCallback.noop);
            return true;
        }
        catch(ResolveFailedException | ResolveCanceledException e) {
            return false;
        }
    }
}
