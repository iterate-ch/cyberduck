package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.transfer.TransferStatus;

public class DefaultRetryCallable<T> extends AbstractRetryCallable<T> {

    private final BackgroundExceptionCallable<T> delegate;
    private final ProgressListener listener;
    private final BackgroundActionState cancel;

    public DefaultRetryCallable(final BackgroundExceptionCallable<T> delegate, final TransferStatus status) {
        this(delegate, new TransferBackgroundActionState(status));
    }

    public DefaultRetryCallable(final BackgroundExceptionCallable<T> delegate, final BackgroundActionState cancel) {
        this(delegate, new DisabledProgressListener(), cancel);
    }

    public DefaultRetryCallable(final BackgroundExceptionCallable<T> delegate, final ProgressListener listener, final BackgroundActionState cancel) {
        this.delegate = delegate;
        this.listener = listener;
        this.cancel = cancel;
    }

    @Override
    public T call() throws BackgroundException {
        while(!cancel.isCanceled()) {
            try {
                return delegate.call();
            }
            catch(BackgroundException e) {
                if(!this.retry(e, listener, cancel)) {
                    throw e;
                }
                // Try again
            }
        }
        throw new ConnectionCanceledException();
    }
}
