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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Match checksum with all versions found in repository
 */
public class ChecksumProfileMatcher implements ProfileMatcher {
    private static final Logger log = LogManager.getLogger(ChecksumProfileMatcher.class.getName());

    /**
     * Filter locally installed profiles by matching checksum
     *
     * @param repository Remote list of profiles
     * @param installed  Description of profile installed in application support directory
     * @return Non-null if matching profile is found in remote list and not latest version
     */
    @Override
    public Optional<ProfileDescription> compare(final Set<ProfileDescription> repository, final ProfileDescription installed) {
        // Filter out profiles with matching checksum
        final Optional<ProfileDescription> found = repository.stream()
                .filter(description -> Objects.equals(description.getChecksum(), installed.getChecksum()))
                .findFirst();
        if(found.isPresent()) {
            // Found matching checksum. Determine if latest version
            if(found.get().isLatest()) {
                // Latest version already installed
                return Optional.empty();
            }
            else {
                // Read last profile version from server as we found matching checksum for previous version
                return found;
            }
        }
        log.warn("Local only profile {}", installed);
        return Optional.empty();
    }
}
