package ch.cyberduck.core.transfer;

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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.threading.TransferBackgroundActionState;

public class SegmentRetryCallable<T> extends DefaultRetryCallable<T> {

    private final BytecountStreamListener counter;

    public SegmentRetryCallable(final Host host,
                                final BackgroundExceptionCallable<T> delegate,
                                final StreamCancelation status,
                                final BytecountStreamListener counter) {
        super(host, delegate, new DisabledProgressListener(), new TransferBackgroundActionState(status));
        this.counter = counter;
    }

    public SegmentRetryCallable(final Host host, final int retry, final int delay,
                                final BackgroundExceptionCallable<T> delegate,
                                final StreamCancelation status,
                                final BytecountStreamListener counter) {
        super(host, retry, delay, delegate, new DisabledProgressListener(), new TransferBackgroundActionState(status));
        this.counter = counter;
    }

    @Override
    public boolean retry(final BackgroundException failure, final ProgressListener progress, final BackgroundActionState cancel) {
        if(super.retry(failure, progress, cancel)) {
            counter.recv(-counter.getRecv());
            counter.sent(-counter.getSent());
            return true;
        }
        return false;
    }
}
