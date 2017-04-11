package ch.cyberduck.core.cryptomator.random;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.random.NonceGenerator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class RotatingNonceGenerator implements NonceGenerator {

    private final static int len = 16;

    private final List<byte[]> nonces;
    private final SecureRandom random = FastSecureRandomProvider.get().provide();
    private final int capacity;

    private int index = 0;

    public RotatingNonceGenerator(final int capacity) {
        this.capacity = capacity;
        this.nonces = new ArrayList<>(capacity);
    }

    @Override
    public byte[] next() {
        if(index >= capacity) {
            index = 0;
        }
        if(nonces.size() <= index) {
            final byte[] nonce = new byte[len];
            random.nextBytes(nonce);
            nonces.add(nonce);
        }
        return nonces.get(index++);
    }
}
