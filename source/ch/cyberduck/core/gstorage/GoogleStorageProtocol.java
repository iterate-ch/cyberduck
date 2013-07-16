package ch.cyberduck.core.gstorage;

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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.i18n.Locale;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Id:$
 */
public final class GoogleStorageProtocol extends AbstractProtocol {
    @Override
    public String getName() {
        return "Google Cloud Storage";
    }

    @Override
    public String getDescription() {
        return Locale.localizedString("Google Cloud Storage", "S3");
    }

    @Override
    public String getIdentifier() {
        return "gs";
    }

    @Override
    public Type getType() {
        return Type.googlestorage;
    }

    @Override
    public String disk() {
        return "googlestorage";
    }

    @Override
    public boolean isHostnameConfigurable() {
        return false;
    }

    @Override
    public String getDefaultHostname() {
        return "storage.googleapis.com";
    }

    @Override
    public Set<String> getLocations() {
        return new HashSet<String>(Arrays.asList(
                "US", "EU"
        ));
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public boolean isPortConfigurable() {
        return false;
    }

    @Override
    public boolean isWebUrlConfigurable() {
        return false;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public String getUsernamePlaceholder() {
        return Locale.localizedString("x-goog-project-id", "Credentials");
    }

    @Override
    public String getPasswordPlaceholder() {
        return Locale.localizedString("Authorization code", "Credentials");
    }

    @Override
    public String favicon() {
        // Return static icon as endpoint has no favicon configured
        return this.icon();
    }

    @Override
    public Session createSession(final Host host) {
        return new GoogleStorageSession(host);
    }
}
