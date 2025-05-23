package ch.cyberduck.core.exception;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

/**
 * An exception that is thrown when a file transfer operation is canceled by the user.
 *
 * This exception extends {@link ConnectionCanceledException}, representing a higher-level
 * interruption specific to transfer operations. It provides constructors to specify
 * detailed error messages and optional causes for the cancellation.
 */
public class TransferCanceledException extends ConnectionCanceledException {
    public TransferCanceledException() {
    }

    public TransferCanceledException(final String detail) {
        super(detail);
    }

    public TransferCanceledException(final Throwable cause) {
        super(cause);
    }

    public TransferCanceledException(final String message, final String detail) {
        super(message, detail);
    }

    public TransferCanceledException(final String detail, final Throwable cause) {
        super(detail, cause);
    }

    public TransferCanceledException(final String message, final String detail, final Throwable cause) {
        super(message, detail, cause);
    }
}
