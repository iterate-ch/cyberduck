package ch.cyberduck.core.cryptomator.random;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.random.NonceGenerator;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Nonce for a chunk derived from a random seed and the index of the chunk. The same nonce is returned for the same
 * chunk index, regardless of the order or the number of times chunks are encrypted. This allows chunks to be
 * encrypted in parallel, retried and encrypted again to calculate the checksum prior to the transfer.
 */
public final class SeededNonceGenerator implements NonceGenerator {
    private static final String ALGORITHM = "HmacSHA256";

    private final int len;
    private final byte[] seed;
    private final AtomicLong index = new AtomicLong();

    public SeededNonceGenerator(final int len) {
        this.len = len;
        this.seed = new byte[32];
        FastSecureRandomProvider.get().provide().nextBytes(seed);
    }

    @Override
    public byte[] next() {
        return this.next(index.getAndIncrement());
    }

    @Override
    public byte[] next(final long index) {
        try {
            final Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(seed, ALGORITHM));
            return Arrays.copyOf(mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(index).array()), len);
        }
        catch(GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SeededNonceGenerator{");
        sb.append("len=").append(len);
        sb.append('}');
        return sb.toString();
    }
}
