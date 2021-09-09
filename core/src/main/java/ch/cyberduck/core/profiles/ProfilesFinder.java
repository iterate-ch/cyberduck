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

import ch.cyberduck.core.exception.AccessDeniedException;

import java.util.Set;

public interface ProfilesFinder {
    default Set<ProfileDescription> find() throws AccessDeniedException {
        return this.find(Visitor.Noop);
    }

    Set<ProfileDescription> find(Visitor visitor) throws AccessDeniedException;

    interface Visitor {
        ProfileDescription visit(ProfileDescription description);

        Visitor Noop = new Visitor() {
            @Override
            public ProfileDescription visit(final ProfileDescription description) {
                return description;
            }
        };

        Visitor Prefetch = description -> {
            description.getProfile();
            return description;
        };
    }
}
