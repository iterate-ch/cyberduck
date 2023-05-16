package ch.cyberduck.core.io;

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

import ch.cyberduck.core.Factory;

public final class ChecksumComputeFactory extends Factory<ChecksumCompute> {

    private ChecksumComputeFactory(final HashAlgorithm algorithm) {
        super(String.format("factory.checksumcompute.%s.class", algorithm.name()));
    }

    public static ChecksumCompute get(final HashAlgorithm algorithm) {
        return new ChecksumComputeFactory(algorithm).create();
    }
}
