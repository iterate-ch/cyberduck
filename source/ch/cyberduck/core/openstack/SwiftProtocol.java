package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.i18n.Locale;

/**
 * @version $Id$
 */
public final class SwiftProtocol extends AbstractProtocol {
    @Override
    public String getName() {
        return Locale.localizedString("Swift", "Mosso");
    }

    @Override
    public String getDescription() {
        return Locale.localizedString("Swift (OpenStack Object Storage)", "Mosso");
    }

    @Override
    public String getIdentifier() {
        return "swift";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String[] getSchemes() {
        return new String[]{this.getScheme().name(), "swift"};
    }

    @Override
    public boolean isHostnameConfigurable() {
        return true;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public String getPasswordPlaceholder() {
        return Locale.localizedString("API Access Key", "Mosso");
    }

    @Override
    public SwiftSession createSession(final Host host) {
        return new SwiftSession(host);
    }
}
