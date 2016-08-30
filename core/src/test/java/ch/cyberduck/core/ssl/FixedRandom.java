package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class FixedRandom extends SecureRandom {
    private static final long serialVersionUID = -1443745618979920886L;

    private FixedRandom() throws NoSuchAlgorithmException {
        //
    }

    /**
     * @return Return a SecureRandom which produces the same value.
     */
    public static SecureRandom createFixedRandom() throws NoSuchAlgorithmException {
        return new FixedRandom();
    }

    @Override
    public void nextBytes(byte[] bytes) {
        //
    }
}
