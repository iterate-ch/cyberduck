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

import ch.cyberduck.core.Profile;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Optional;
import java.util.stream.Stream;

public class FilenameProfileMatcher implements ProfileMatcher {
    private static final Logger log = Logger.getLogger(FilenameProfileMatcher.class.getName());

    private final Stream<ProfilesFinder.ProfileDescription> installed;

    public FilenameProfileMatcher(final Stream<ProfilesFinder.ProfileDescription> installed) {
        this.installed = installed;
    }

    /**
     * Filter locally installed profiles by matching filename only
     *
     * @param next Description of profile from server
     * @return Non null if matching profile is found on disk with checksum mismatch
     */
    @Override
    public Optional<Profile> compare(final ProfilesFinder.ProfileDescription next) {
        // Filter out profiles with matching checksum
        // Found already installed with same filename
        final Optional<ProfilesFinder.ProfileDescription> found = installed
            .filter(description -> !description.getChecksum().equals(next.getChecksum()))
            .filter(description -> {
                // Should match identifier and provider but only have filename
                return StringUtils.equals(description.getName(), next.getName());
            }).findFirst();
        if(found.isPresent()) {
            // Read profile from server
            return Optional.of(next.getProfile().get());
        }
        return Optional.empty();
    }
}
