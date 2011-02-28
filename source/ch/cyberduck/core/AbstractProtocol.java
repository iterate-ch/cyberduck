package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class AbstractProtocol implements Protocol {
    protected static Logger log = Logger.getLogger(AbstractProtocol.class);

    /**
     * Must be unique across all available protocols.
     *
     * @return The identifier for this protocol which is the scheme by default
     */
    public String getIdentifier() {
        return this.getScheme();
    }

    public String getName() {
        return this.getScheme().toUpperCase();
    }

    public String favicon() {
        return null;
    }

    public boolean isEnabled() {
        return Preferences.instance().getBoolean("protocol." + this.getIdentifier() + ".enable");
    }

    /**
     * Statically register protocol implementations.
     */
    public void register() {
        if(this.isEnabled()) {
            if(log.isDebugEnabled()) {
                log.debug("Register protocol:" + this);
            }
            SessionFactory.addFactory(this, this.getSessionFactory());
            PathFactory.addFactory(this, this.getPathFactory());
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug("Skip disabled protocol:" + this);
            }
        }
    }

    /**
     * @return
     */
    public abstract String getDescription();

    /**
     * @return
     */
    public abstract String getScheme();

    public String[] getSchemes() {
        return new String[]{this.getScheme()};
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof AbstractProtocol) {
            return ((AbstractProtocol) other).getIdentifier().equals(this.getIdentifier());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getIdentifier();
    }

    /**
     * @return A mounted disk icon to display
     */
    public String disk() {
        return this.getIdentifier();
    }

    /**
     * @return A small icon to display
     */
    public String icon() {
        return this.getIdentifier() + "-icon";
    }

    /**
     * @return
     */
    public boolean isSecure() {
        return false;
    }

    public boolean isHostnameConfigurable() {
        return true;
    }

    public boolean isPortConfigurable() {
        return true;
    }

    public boolean isWebUrlConfigurable() {
        return true;
    }

    public boolean isEncodingConfigurable() {
        return false;
    }

    public boolean isConnectModeConfigurable() {
        return false;
    }

    public boolean isAnonymousConfigurable() {
        return true;
    }

    public boolean isUTCTimezone() {
        return true;
    }

    public String getUsernamePlaceholder() {
        return Locale.localizedString("Username", "Credentials");
    }

    public String getPasswordPlaceholder() {
        return Locale.localizedString("Password", "Credentials");
    }

    public String getDefaultHostname() {
        return Preferences.instance().getProperty("connection.hostname.default");
    }

    /**
     * @return
     */
    public abstract SessionFactory getSessionFactory();

    /**
     * @return
     */
    public abstract PathFactory getPathFactory();

    /**
     * Check login credentials for validity for this protocol.
     *
     * @param credentials
     * @return True if username and password is not a blank string and password
     */
    public boolean validate(Credentials credentials) {
        return StringUtils.isNotBlank(credentials.getUsername())
                && StringUtils.isNotEmpty(credentials.getPassword());
    }

    /**
     * @return The default port this protocol connects to
     */
    public abstract int getDefaultPort();
}