package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProxyListProgressListener;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class WorkerCanceledListProgressListener extends ProxyListProgressListener {
    private static final Logger log = LogManager.getLogger(WorkerCanceledListProgressListener.class.getName());

    private final Worker worker;

    public WorkerCanceledListProgressListener(final Worker worker) {
        this(worker, new DisabledListProgressListener());
    }

    public WorkerCanceledListProgressListener(final Worker worker, final ListProgressListener proxy) {
        super(proxy);
        this.worker = worker;
    }

    @Override
    public void chunk(final Path directory, final AttributedList<Path> list) throws ConnectionCanceledException {
        log.info("Retrieved chunk of {} items in {}", list.size(), directory);
        if(worker.isCanceled()) {
            throw new ListCanceledException(list);
        }
        super.chunk(directory, list);
    }
}
