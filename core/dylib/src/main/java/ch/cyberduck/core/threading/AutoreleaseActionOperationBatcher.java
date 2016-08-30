package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * feedback@cyberduck.ch
 */

import org.rococoa.internal.AutoreleaseBatcher;

public final class AutoreleaseActionOperationBatcher implements ActionOperationBatcher {

    /**
     * An autorelease pool is used to manage Foundation's autorelease
     * mechanism for Objective-C objects. If you start off a thread
     * that calls Cocoa, there won't be a top-level pool.
     */
    private final AutoreleaseBatcher impl = AutoreleaseBatcher.forThread(1);

    public void operate() {
        impl.operate();
    }
}
