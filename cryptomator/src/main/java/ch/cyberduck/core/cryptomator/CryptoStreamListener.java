package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.io.StreamListener;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Translates the number of bytes of the encrypted content that are transferred to the number of bytes of the
 * cleartext content.
 */
public class CryptoStreamListener implements StreamListener {

    private final StreamListener proxy;
    private final long cleartext;
    private final long ciphertext;

    private final AtomicLong sent = new AtomicLong();
    private final AtomicLong recv = new AtomicLong();

    /**
     * @param proxy      Listener for the cleartext content
     * @param cleartext  Size of the cleartext content
     * @param ciphertext Size of the encrypted content
     */
    public CryptoStreamListener(final StreamListener proxy, final long cleartext, final long ciphertext) {
        this.proxy = proxy;
        this.cleartext = cleartext;
        this.ciphertext = ciphertext;
    }

    @Override
    public void sent(final long bytes) {
        proxy.sent(this.translate(sent, bytes));
    }

    @Override
    public void recv(final long bytes) {
        proxy.recv(this.translate(recv, bytes));
    }

    /**
     * @param total Number of bytes of the encrypted content seen
     * @param bytes Number of bytes of the encrypted content added
     * @return Number of bytes of the cleartext content added
     */
    private long translate(final AtomicLong total, final long bytes) {
        if(ciphertext <= 0) {
            return bytes;
        }
        final long previous = total.getAndAdd(bytes);
        return this.scale(previous + bytes) - this.scale(previous);
    }

    private long scale(final long encrypted) {
        return Math.min(cleartext, (long) ((double) encrypted * cleartext / ciphertext));
    }
}
