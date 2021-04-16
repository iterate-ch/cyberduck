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
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.util.Objects;

/**
 * Profile metadata
 */
public class ProfileDescription {
    private final LazyInitializer<Checksum> checksum;
    private final LazyInitializer<Local> profile;

    public ProfileDescription(final String name, final Checksum checksum, final Local profile) {
        this(new LazyInitializer<Checksum>() {
            @Override
            protected Checksum initialize() {
                return checksum;
            }
        }, new LazyInitializer<Local>() {
            @Override
            protected Local initialize() {
                return profile;
            }
        });
    }

    public ProfileDescription(final LazyInitializer<Checksum> checksum, final LazyInitializer<Local> profile) {
        this.checksum = checksum;
        this.profile = profile;
    }

    public Checksum getChecksum() {
        try {
            return checksum.get();
        }
        catch(ConcurrentException e) {
            return Checksum.NONE;
        }
    }

    public Local getProfile() {
        try {
            return profile.get();
        }
        catch(ConcurrentException e) {
            return null;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof ProfileDescription)) {
            return false;
        }
        final ProfileDescription that = (ProfileDescription) o;
        try {
            return Objects.equals(checksum.get(), that.checksum.get());
        }
        catch(ConcurrentException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return Objects.hash(checksum.get());
        }
        catch(ConcurrentException e) {
            return Objects.hash(Checksum.NONE);
        }
    }

    public boolean isLatest() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProfileDescription{");
        sb.append("checksum=").append(checksum);
        sb.append(", profile=").append(profile);
        sb.append('}');
        return sb.toString();
    }
}
