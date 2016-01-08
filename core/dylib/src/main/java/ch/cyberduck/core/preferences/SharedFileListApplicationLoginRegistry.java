package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.local.FinderSidebarService;
import ch.cyberduck.core.local.SidebarService;

/**
 * @version $Id:$
 */
public class SharedFileListApplicationLoginRegistry implements ApplicationLoginRegistry {

    private final FinderSidebarService service = new FinderSidebarService(SidebarService.List.login);

    private final ApplicationFinder finder;

    public SharedFileListApplicationLoginRegistry() {
        this(ApplicationFinderFactory.get());
    }

    public SharedFileListApplicationLoginRegistry(final ApplicationFinder finder) {
        this.finder = finder;
    }

    @Override
    public boolean register(final Application application) {
        try {
            if(finder.isInstalled(application)) {
                service.add(LocalFactory.get(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(application.getIdentifier())));
                return true;
            }
            return false;
        }
        catch(LocalAccessDeniedException e) {
            return false;
        }
    }

    @Override
    public boolean unregister(final Application application) {
        try {
            if(finder.isInstalled(application)) {
                service.remove(LocalFactory.get(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(application.getIdentifier())));
                return true;
            }
            return false;
        }
        catch(LocalAccessDeniedException e) {
            return false;
        }
    }
}
