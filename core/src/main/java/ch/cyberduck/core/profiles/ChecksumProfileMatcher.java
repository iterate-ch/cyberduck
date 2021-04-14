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

import org.apache.log4j.Logger;

import java.util.Optional;
import java.util.stream.Stream;

public class ChecksumProfileMatcher implements ProfileMatcher {
    private static final Logger log = Logger.getLogger(ChecksumProfileMatcher.class.getName());

    private final Stream<ProfilesFinder.ProfileDescription> installed;

    public ChecksumProfileMatcher(final Stream<ProfilesFinder.ProfileDescription> installed) {
        this.installed = installed;
    }

    /**
     * @param next Description of profile from server
     * @return Non null if profile from server has been updated
     */
    @Override
    public Optional<Profile> compare(final ProfilesFinder.ProfileDescription next) {
        // Read profile from server
        final Profile profile = next.getProfile().get();
        final Optional<ProfilesFinder.ProfileDescription> found = installed
            .filter(description -> !description.getChecksum().equals(next.getChecksum()))
            .filter(description -> {
                // Matches identifier and provider
                final Profile installed = description.getProfile().get();
                if(null == installed) {
                    // Failure parsing
                    log.warn(String.format("Ignore unknown profile %s", installed));
                    return false;
                }
                return new IdentifierProtocolPredicate(installed).test(profile);
            }).findFirst();
        if(found.isPresent()) {
            return Optional.of(profile);
        }
        return Optional.empty();
    }
}
