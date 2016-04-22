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

import java.time.Duration;

public class RetriableAccessDeniedException extends AccessDeniedException {

    private final Duration retry;

    public RetriableAccessDeniedException(final String detail) {
        super(detail);
        this.retry = Duration.ZERO;
    }

    public RetriableAccessDeniedException(final String detail, final Throwable cause) {
        super(detail, cause);
        this.retry = Duration.ZERO;
    }

    public RetriableAccessDeniedException(final String detail, final Duration seconds) {
        super(detail);
        this.retry = seconds;
    }

    public RetriableAccessDeniedException(final String detail, final Duration seconds, final Throwable cause) {
        super(detail, cause);
        this.retry = seconds;
    }

    /**
     * @return Retry after this delay has expired
     */
    public Duration getRetry() {
        return retry;
    }
}
