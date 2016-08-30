package ch.cyberduck.core.openstack;

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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;

public final class PlaceholderCredentials extends Credentials {

    private final String placeholder;

    public PlaceholderCredentials() {
        this(LocaleFactory.localizedString("Tenant", "Mosso"));
    }

    public PlaceholderCredentials(final String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public String getUsernamePlaceholder() {
        return placeholder;
    }

    @Override
    public boolean validate(final Protocol protocol, final LoginOptions options) {
        // Allow empty tenant
        return true;
    }
}
