package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

/**
 * @version $Id$
 */
public abstract class AbstractProtocol implements Protocol {

    @Override
    public String getProvider() {
        return this.getIdentifier();
    }

    @Override
    public String getName() {
        return this.getScheme().name().toUpperCase(Locale.ROOT);
    }

    @Override
    public String favicon() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String[] getSchemes() {
        return new String[]{this.getScheme().name()};
    }

    @Override
    public String toString() {
        return this.getProvider();
    }

    /**
     * @return A mounted disk icon to display
     */
    @Override
    public String disk() {
        return String.format("%s.tiff", this.getIdentifier());
    }

    /**
     * @return A small icon to display
     */
    @Override
    public String icon() {
        return this.disk();
    }

    @Override
    public boolean isSecure() {
        return this.getScheme().isSecure();
    }

    @Override
    public boolean isHostnameConfigurable() {
        return StringUtils.isBlank(this.getDefaultHostname());
    }

    /**
     * @return False if the port to connect is static.
     */
    @Override
    public boolean isPortConfigurable() {
        return true;
    }

    @Override
    public boolean isEncodingConfigurable() {
        return false;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return true;
    }

    @Override
    public boolean isUsernameConfigurable() {
        return true;
    }

    @Override
    public boolean isPasswordConfigurable() {
        return true;
    }

    @Override
    public boolean isUTCTimezone() {
        return true;
    }

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Username", "Credentials");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Password", "Credentials");
    }

    @Override
    public String getDefaultHostname() {
        // Blank by default
        return PreferencesFactory.get().getProperty("connection.hostname.default");
    }

    /**
     * @return Available regions for containers
     */
    @Override
    public Set<Location.Name> getRegions() {
        return Collections.emptySet();
    }

    /**
     * @return The default port this protocol connects to
     */
    @Override
    public int getDefaultPort() {
        return this.getScheme().getPort();
    }

    /**
     * @return Authentication path
     */
    @Override
    public String getContext() {
        return null;
    }

    @Override
    public String getRegion() {
        return null;
    }

    @Override
    public Type getType() {
        return Type.valueOf(this.getIdentifier());
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", this.getClass().getPackage().getName(), StringUtils.capitalize(this.getType().name()));
    }

    @Override
    public boolean validate(Credentials credentials, LoginOptions options) {
        return this.getType().validate(credentials, options);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Protocol)) {
            return false;
        }
        Protocol protocol = (Protocol) o;
        if(this.getIdentifier() != null ? !this.getIdentifier().equals(protocol.getIdentifier()) : protocol.getIdentifier() != null) {
            return false;
        }
        if(this.getScheme() != null ? !this.getScheme().equals(protocol.getScheme()) : protocol.getScheme() != null) {
            return false;
        }
        if(this.getProvider() != null ? !this.getProvider().equals(protocol.getProvider()) : protocol.getProvider() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final Protocol o) {
        return this.getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public int hashCode() {
        int result = this.getIdentifier() != null ? this.getIdentifier().hashCode() : 0;
        result = 31 * result + (this.getScheme() != null ? this.getScheme().hashCode() : 0);
        result = 31 * result + (this.getProvider() != null ? this.getProvider().hashCode() : 0);
        return result;
    }
}
