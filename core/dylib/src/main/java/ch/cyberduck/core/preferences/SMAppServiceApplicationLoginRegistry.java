package ch.cyberduck.core.preferences;

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

import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.local.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SMAppServiceApplicationLoginRegistry implements ApplicationLoginRegistry {
    private static final Logger log = LogManager.getLogger(SMAppServiceApplicationLoginRegistry.class);

    static {
        Native.load("core");
    }

    @Override
    public boolean register(final Application application) {
        return SMAppService.loginItemServiceWithIdentifier(application.getIdentifier()).registerAndReturnError(null);
    }

    @Override
    public boolean unregister(final Application application) {
        return SMAppService.loginItemServiceWithIdentifier(application.getIdentifier()).unregisterAndReturnError(null);
    }
}
