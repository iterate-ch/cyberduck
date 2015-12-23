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

import org.apache.http.HttpHeaders;

import java.util.Objects;

public class HttpRange {

    private final long start;
    private final long end;
    private final long length;

    public static HttpRange withStatus(final TransferStatus status) {
        return byLength(status.getOffset(), status.getLength());
    }

    public static HttpRange byPosition(final long start, final long end) {
        return new HttpRange(start, end);
    }

    public static HttpRange byLength(final long offset, final long length) {
        return new HttpRange(offset, -1 == length ? -1 : offset + length - 1);
    }

    public HttpRange(final long start, final long end) {
        this.start = start;
        this.end = end;
        if(-1 == end) {
            this.length = -1;
        }
        else {
            this.length = end - start + 1;
        }
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }

    public long getLength() {
        return length;
    }

    public String toHeader(final String header) {
        switch(header) {
            case HttpHeaders.CONTENT_RANGE:
                if(-1 == length) {
                    // Complete length unknown. An asterisk
                    // character ("*") in place of the complete-length indicates that the
                    // representation length was unknown when the header field was generated.
                    return String.format("bytes %d-*/*", start);
                }
                return String.format("bytes %d-%d/%d", start, end, length);
            case HttpHeaders.RANGE:
                if(-1 == length) {
                    return String.format("bytes=%d-%d", start, end);
                }
                return String.format("bytes=%d-", start);
        }
        return null;
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
        return start == httpRange.start &&
                end == httpRange.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
