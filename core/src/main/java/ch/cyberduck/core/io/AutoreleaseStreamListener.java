package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;

/**
 * If your application or thread is long-lived and potentially generates a lot of autoreleased objects, you should
 * periodically drain and create autorelease pools (like the Application Kit does on the main thread); otherwise,
 * autoreleased objects accumulate and your memory footprint grows. If, however, your detached thread does not make
 * Cocoa calls, you do not need to create an autorelease pool.
 */
public class AutoreleaseStreamListener implements StreamListener {

    private final ThreadLocal<ActionOperationBatcher> autorelease;

    public AutoreleaseStreamListener() {
        this(ThreadLocal.withInitial(() -> ActionOperationBatcherFactory.get(100)));
    }

    public AutoreleaseStreamListener(final ThreadLocal<ActionOperationBatcher> autorelease) {
        this.autorelease = autorelease;
    }

    @Override
    public void sent(final long bytes) {
        autorelease.get().operate();
    }

    @Override
    public void recv(final long bytes) {
        autorelease.get().operate();
    }
}
