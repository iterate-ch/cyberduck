package ch.cyberduck.core.sparkle;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.library.Native;

import org.apache.log4j.Logger;

public final class Sandbox {
    private static final Logger log = Logger.getLogger(Sandbox.class);

    static {
        Native.load("core");
    }

    public static Sandbox get() {
        if(log.isDebugEnabled()) {
            log.debug("Obtain sandbox environment with requirement com.apple.security.app-sandbox");
        }
        return new Sandbox();
    }

    public native boolean isSandboxed();
}
