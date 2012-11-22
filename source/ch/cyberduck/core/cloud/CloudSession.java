package ch.cyberduck.core.cloud;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @version $Id: CloudSession.java 7011 2010-09-18 15:20:05Z dkocher $
 */
public abstract class CloudSession extends HttpSession {

    protected CloudSession(Host h) {
        super(h);
    }

    /**
     * Use ACL support.
     *
     * @return Always returning false because permissions should be set using ACLs
     */
    @Override
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isWriteTimestampSupported() {
        return false;
    }

    public boolean isLoggingSupported() {
        return false;
    }

    @Override
    public boolean isAnalyticsSupported() {
        return this.isLoggingSupported();
    }

    public boolean isLogging(final String container) {
        throw new UnsupportedOperationException();
    }

    public void setLogging(final String container, final boolean enabled, String destination) {
        throw new UnsupportedOperationException();
    }

    public String getLoggingTarget(final String container) {
        throw new UnsupportedOperationException();
    }

    public boolean isVersioningSupported() {
        return false;
    }

    public boolean isVersioning(final String container) {
        throw new UnsupportedOperationException();
    }

    public void setVersioning(final String container, boolean mfa, boolean versioning) {
        throw new UnsupportedOperationException();
    }

    public boolean isLocationSupported() {
        return false;
    }

    public String getLocation(final String container) {
        throw new UnsupportedOperationException();
    }

    public boolean isMultiFactorAuthentication(final String container) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param hostname Hostname with container name in third level
     * @return Null if no container component in hostname prepended
     */
    public String getContainerForHostname(String hostname) {
        if(hostname.equals(host.getProtocol().getDefaultHostname())) {
            return null;
        }
        // Bucket name is available in URL's host name.
        if(hostname.endsWith(host.getProtocol().getDefaultHostname())) {
            // Bucket name is available as S3 subdomain
            return hostname.substring(0, hostname.length() - host.getProtocol().getDefaultHostname().length() - 1);
        }
        return null;
    }

    /**
     * @param container DNS container name
     * @return Generic hostname
     */
    public String getHostnameForContainer(String container) {
        if(StringUtils.isBlank(container)) {
            return this.getHost().getHostname(true);
        }
        return container + "." + this.getHost().getHostname(true);
    }

    @Override
    public boolean isMetadataSupported() {
        return true;
    }

    /**
     * @return List of redundancy level options. Empty list
     *         no storage options are available.
     */
    public List<String> getSupportedStorageClasses() {
        return Collections.emptyList();
    }

    /**
     * @return List of algorithms. Empty list
     *         no encryption options are available.
     */
    public List<String> getSupportedEncryptionAlgorithms() {
        return Collections.emptyList();
    }
}
