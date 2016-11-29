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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProxyListProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;

public class VaultFinderListService implements ListService {
    private static final Logger log = Logger.getLogger(VaultFinderListService.class);

    private final Session<?> proxy;
    private final ListProgressListener[] listeners;

    public VaultFinderListService(final Session<?> proxy, final ListProgressListener[] listeners) {
        this.proxy = proxy;
        this.listeners = listeners;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            return proxy.list(directory, new ProxyListProgressListener(listeners));
        }
        catch(VaultFinderListCanceledException e) {
            // Run again with decrypting list worker
            final ListService service = proxy.getFeature(ListService.class);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Switch list service to %s", service));
            }
            return service.list(directory, listener);
        }
    }
}
