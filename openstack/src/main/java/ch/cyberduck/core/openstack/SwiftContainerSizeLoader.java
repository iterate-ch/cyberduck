package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.OneTimeSchedulerFeature;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;

/**
 * Preload container size
 */
public class SwiftContainerSizeLoader extends OneTimeSchedulerFeature<Long> {

    private final SwiftSession session;
    private final SwiftRegionService regionService;

    public SwiftContainerSizeLoader(final SwiftSession session, final SwiftRegionService regionService, final Path container) {
        super(container);
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    protected Long operate(final PasswordCallback callback, final Path container) throws BackgroundException {
        try {
            return session.getClient().getContainerInfo(regionService.lookup(container), container.getName()).getTotalSize();
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
