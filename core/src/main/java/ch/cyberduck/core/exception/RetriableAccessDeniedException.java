package ch.cyberduck.core.exception;

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

import ch.cyberduck.core.preferences.PreferencesFactory;

import java.time.Duration;

public class RetriableAccessDeniedException extends AccessDeniedException {

    private final Duration delay;

    public RetriableAccessDeniedException(final String detail) {
        super(detail);
        this.delay = Duration.ofSeconds(PreferencesFactory.get().getLong("connection.retry.delay"));
    }

    public RetriableAccessDeniedException(final String detail, final Throwable cause) {
        super(detail, cause);
        this.delay = Duration.ofSeconds(PreferencesFactory.get().getLong("connection.retry.delay"));
    }

    public RetriableAccessDeniedException(final String detail, final Duration delay) {
        super(detail);
        this.delay = delay;
    }

    public RetriableAccessDeniedException(final String detail, final Duration delay, final Throwable cause) {
        super(detail, cause);
        this.delay = delay;
    }

    /**
     * @return Retry after this delay has expired
     */
    public Duration getDelay() {
        return delay;
    }
}
