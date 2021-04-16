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

import ch.cyberduck.core.Protocol;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Predicate;

public interface ProfileMatcher {
    /**
     * @param next Description of profile installed
     * @return Non null if profile from server has been updated. Matching profile with later version in repository.
     */
    Optional<ProfileDescription> compare(ProfileDescription next);

    class IdentifierProtocolPredicate implements Predicate<Protocol> {
        private final Protocol installed;

        public IdentifierProtocolPredicate(final Protocol installed) {
            this.installed = installed;
        }

        @Override
        public boolean test(final Protocol protocol) {
            return StringUtils.equals(installed.getIdentifier(), protocol.getIdentifier())
                && StringUtils.equals(installed.getProvider(), protocol.getProvider());
        }
    }
}
