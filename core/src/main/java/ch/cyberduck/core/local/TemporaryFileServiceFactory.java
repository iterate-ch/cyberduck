package ch.cyberduck.core.local;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Factory;

public class TemporaryFileServiceFactory extends Factory<TemporaryFileService> {

    private static final TemporaryFileService DEFAULT = new TemporaryFileServiceFactory().create();

    /**
     * @return Shared singleton
     */
    public static TemporaryFileService get() {
        return DEFAULT;
    }

    public static TemporaryFileService instance() {
        return new TemporaryFileServiceFactory().create();
    }

    private TemporaryFileServiceFactory() {
        super("factory.temporaryfiles.class");
    }
}