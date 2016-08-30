package ch.cyberduck.core.identity;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;

public abstract class AbstractIdentityConfiguration implements IdentityConfiguration {

    @Override
    public void delete(final String username, final LoginCallback prompt) throws BackgroundException {
        //
    }

    @Override
    public void create(final String username, final String policy, final LoginCallback prompt) throws BackgroundException {
        //
    }
}
