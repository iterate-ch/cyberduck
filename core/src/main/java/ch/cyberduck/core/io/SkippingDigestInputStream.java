package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Digest stream that includes skipped bytes in the digest. The checksum of the complete content is available
 * when resuming a transfer from an offset.
 */
public class SkippingDigestInputStream extends DigestInputStream {

    public SkippingDigestInputStream(final InputStream stream, final MessageDigest digest) {
        super(stream, digest);
    }

    @Override
    public long skip(final long n) throws IOException {
        final byte[] buffer = new byte[(int) Math.min(n, 8192L)];
        long remaining = n;
        while(remaining > 0) {
            final int read = this.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if(read < 0) {
                break;
            }
            remaining -= read;
        }
        return n - remaining;
    }
}
