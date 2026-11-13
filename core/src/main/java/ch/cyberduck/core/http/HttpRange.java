/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.http;

import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Objects;

public class HttpRange {

    /**
     * Start byte offset
     */
    private final Long start;
    /**
     * End byte inclusive
     */
    private final Long end;
    private final Long length;

    public static HttpRange withStatus(final TransferStatus status) {
        return byLength(status.getOffset(), status.getLength());
    }

    public static HttpRange byPosition(final long start, final long end) {
        return new HttpRange(start, end);
    }

    public static HttpRange byLength(final long offset, final long length) {
        return new HttpRange(offset, TransferStatus.UNKNOWN_LENGTH == length ? TransferStatus.UNKNOWN_LENGTH : offset + length - 1);
    }

    public HttpRange(final long start, final long end) {
        this(start, end, TransferStatus.UNKNOWN_LENGTH == end ? TransferStatus.UNKNOWN_LENGTH : end - start + 1);
    }

    public HttpRange(final Long start, final Long end, final Long length) {
        this.start = start;
        this.end = end;
        this.length = length;
    }

    /**
     * @return First byte position
     */
    public long getStart() {
        return start;
    }

    /**
     * @return Last byte position
     */
    public long getEnd() {
        return end;
    }

    /**
     * @return Number of bytes
     */
    public long getLength() {
        return length;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof HttpRange)) {
            return false;
        }
        final HttpRange httpRange = (HttpRange) o;
        return Objects.equals(start, httpRange.start) &&
                Objects.equals(end, httpRange.end) &&
                Objects.equals(length, httpRange.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, length);
    }
}
