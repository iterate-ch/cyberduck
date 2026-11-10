package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

public class SwiftListService implements ListService {
    private static final Logger log = LogManager.getLogger(SwiftListService.class);

    private final SwiftSession session;
    private final SwiftRegionService regionService;

    public SwiftListService(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            try {
                return new SwiftContainerListService(session,
                        new SwiftLocationFeature.SwiftRegion(session.getHost().getRegion())).list(directory, listener);
            }
            catch(AccessDeniedException e) {
                // Listing the account is not permitted when every user owns a single container in a shared account
                final Path home = new SwiftHomeFeature(session.getHost()).find();
                if(null == home) {
                    throw e;
                }
                log.warn("Listing account denied with {}. Return home container {}", e, home);
                final AttributedList<Path> containers = new AttributedList<>(Collections.singletonList(home));
                listener.chunk(directory, containers);
                return containers;
            }
        }
        else {
            return new SwiftObjectListService(session, regionService).list(directory, listener);
        }
    }
}
