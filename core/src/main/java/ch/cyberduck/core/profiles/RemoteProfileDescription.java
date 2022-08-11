package ch.cyberduck.core.profiles;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.concurrent.LazyInitializer;

public final class RemoteProfileDescription extends ProfileDescription {
    private final Path file;

    /**
     * @param protocols Registered protocols
     * @param file      Connection profile
     * @param profile   Read connection profile to file on local disk
     */
    public RemoteProfileDescription(final ProtocolFactory protocols, final Path file, final LazyInitializer<Local> profile) {
        super(protocols, ProtocolFactory.BUNDLED_PROFILE_PREDICATE, new LazyInitializer<Checksum>() {
            @Override
            protected Checksum initialize() {
                return file.attributes().getChecksum();
            }
        }, profile);
        this.file = file;
    }

    @Override
    public boolean isLatest() {
        return !file.attributes().isDuplicate();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PathProfileDescription{");
        sb.append("file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
