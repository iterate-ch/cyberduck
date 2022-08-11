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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Profile metadata
 */
public class ProfileDescription {
    private static final Logger log = LogManager.getLogger(ProfileDescription.class);

    private final LazyInitializer<Checksum> checksum;
    private final LazyInitializer<Local> local;
    private final LazyInitializer<Profile> profile;

    /**
     * @param protocols Registered protocols
     * @param checksum  Checksum of connection profile
     * @param local     File on disk
     */
    public ProfileDescription(final ProtocolFactory protocols, final Checksum checksum, final Local local) {
        this(protocols, protocol -> true, checksum, local);
    }

    /**
     * @param protocols Registered protocols
     * @param parent    Filter to apply for parent protocol reference in registered protocols
     * @param checksum  Checksum of connection profile
     * @param local     File on disk
     */
    public ProfileDescription(final ProtocolFactory protocols, final Predicate<Protocol> parent, final Checksum checksum, final Local local) {
        this(protocols, parent, new LazyInitializer<Checksum>() {
            @Override
            protected Checksum initialize() {
                return checksum;
            }
        }, new LazyInitializer<Local>() {
            @Override
            protected Local initialize() {
                return local;
            }
        });
    }

    public ProfileDescription(final ProtocolFactory protocols, final Predicate<Protocol> parent,
                              final LazyInitializer<Checksum> checksum, final LazyInitializer<Local> local) {
        this.checksum = checksum;
        this.local = local;
        this.profile = new LazyInitializer<Profile>() {
            @Override
            protected Profile initialize() throws ConcurrentException {
                try {
                    return new ProfilePlistReader(protocols, parent).read(local.get());
                }
                catch(AccessDeniedException e) {
                    log.warn(String.format("Failure %s reading profile %s", e, e));
                    throw new ConcurrentException(e);
                }
            }
        };
    }

    public Checksum getChecksum() {
        try {
            return checksum.get();
        }
        catch(ConcurrentException e) {
            return Checksum.NONE;
        }
    }

    public Optional<Local> getFile() {
        try {
            return Optional.of(local.get());
        }
        catch(ConcurrentException e) {
            return Optional.empty();
        }
    }

    public Optional<Profile> getProfile() {
        try {
            return Optional.of(profile.get());
        }
        catch(ConcurrentException e) {
            return Optional.empty();
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

    public boolean isInstalled() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProfileDescription{");
        sb.append("checksum=").append(checksum);
        sb.append(", profile=").append(local);
        sb.append('}');
        return sb.toString();
    }
}
