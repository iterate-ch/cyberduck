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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StreamCancelation;

import org.apache.log4j.Logger;

public class DefaultRetryCallable<T> extends RetryCallable<T> {
    private static final Logger log = Logger.getLogger(DefaultRetryCallable.class);

    private final RetryCallable<T> delegate;

    private final StreamCancelation cancel;


    public DefaultRetryCallable(final RetryCallable<T> delegate, final StreamCancelation cancel) {
        this.delegate = delegate;
        this.cancel = cancel;
    }

    @Override
    public T call() throws BackgroundException {
        while(true) {
            try {
                return delegate.call();
            }
            catch(BackgroundException e) {
                if(this.retry(e, new DisabledProgressListener(), cancel)) {
                    // Continue
                }
                else {
                    throw e;
                }
            }
        }
    }
}
