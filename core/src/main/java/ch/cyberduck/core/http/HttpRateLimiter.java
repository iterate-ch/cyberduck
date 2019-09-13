package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

public interface HttpRateLimiter {

    /**
     * Acquires a permit from if it can be acquired immediately without
     * delay.
     */
    boolean tryAcquire();

    double acquire();

    HttpRateLimiter DISABLED = new HttpRateLimiter() {
        @Override
        public boolean tryAcquire() {
            return true;
        }

        @Override
        public double acquire() {
            return 0;
        }
    };
}
