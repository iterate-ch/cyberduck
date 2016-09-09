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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Profile implements Protocol, Serializable {
    private static final Logger log = Logger.getLogger(Profile.class);

    private Deserializer<String> dict;

    /**
     * The actual protocol implementation registered
     */
    private Protocol parent;

    private Local image;

    public Profile(final Protocol parent, final Deserializer<String> dict) {
        this.parent = parent;
        this.dict = dict;
        this.image = this.write(this.value("Disk"));
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        throw new UnsupportedOperationException();
    }

    public Protocol getProtocol() {
        return parent;
    }

    @Override
    public String getPrefix() {
        return parent.getPrefix();
    }

    /**
     * @return False if missing required fields in profile.
     */
    @Override
    public boolean isEnabled() {
        return StringUtils.isNotBlank(this.value("Protocol"))
                && StringUtils.isNotBlank(this.value("Vendor"));
    }

    @Override
    public boolean isSecure() {
        return this.getScheme().isSecure();
    }

    @Override
    public String getIdentifier() {
        return parent.getIdentifier();
    }

    @Override
    public Type getType() {
        return parent.getType();
    }

    @Override
    public String getUsernamePlaceholder() {
        final String v = this.value("Username Placeholder");
        if(StringUtils.isBlank(v)) {
            return parent.getUsernamePlaceholder();
        }
        return v;
    }

    @Override
    public String getPasswordPlaceholder() {
        final String v = this.value("Password Placeholder");
        if(StringUtils.isBlank(v)) {
            return parent.getPasswordPlaceholder();
        }
        return v;
    }

    @Override
    public String getDefaultHostname() {
        final String v = this.value("Default Hostname");
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultHostname();
        }
        return v;
    }

    @Override
    public String getProvider() {
        final String v = this.value("Vendor");
        if(StringUtils.isBlank(v)) {
            return parent.getProvider();
        }
        return v;
    }

    @Override
    public String getName() {
        final String v = this.value("Name");
        if(StringUtils.isBlank(v)) {
            return parent.getName();
        }
        return v;
    }

    @Override
    public String getDescription() {
        final String v = this.value("Description");
        if(StringUtils.isBlank(v)) {
            return parent.getDescription();
        }
        return v;
    }

    @Override
    public int getDefaultPort() {
        final String v = this.value("Default Port");
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultPort();
        }
        try {
            return Integer.valueOf(v);
        }
        catch(NumberFormatException e) {
            log.warn(String.format("Port %s is not a number", e.getMessage()));
        }
        return parent.getDefaultPort();
    }

    @Override
    public String getRegion() {
        final String v = this.value("Region");
        if(StringUtils.isBlank(v)) {
            return parent.getRegion();
        }
        return v;
    }

    @Override
    public String disk() {
        if(null == image) {
            return parent.disk();
        }
        // Temporary file
        return image.getAbsolute();
    }

    @Override
    public String icon() {
        if(null == image) {
            return parent.icon();
        }
        // Temporary file
        return image.getAbsolute();
    }

    @Override
    public String favicon() {
        return parent.favicon();
    }

    /**
     * Write temporary file with data
     *
     * @param icon Base64 encoded image information
     * @return Path to file
     */
    private Local write(final String icon) {
        if(StringUtils.isBlank(icon)) {
            return null;
        }
        final byte[] favicon = Base64.decodeBase64(icon);
        final Local file = TemporaryFileServiceFactory.get().create(String.format("%s.ico", new AlphanumericRandomStringService().random()));
        try {
            new DefaultLocalTouchFeature().touch(file);
            final OutputStream out = file.getOutputStream(false);
            try {
                IOUtils.write(favicon, out);
            }
            finally {
                IOUtils.closeQuietly(out);
            }
            return file;
        }
        catch(IOException | AccessDeniedException e) {
            log.error("Error writing temporary file", e);
        }
        return null;
    }

    @Override
    public boolean validate(Credentials credentials, LoginOptions options) {
        return parent.validate(credentials, options);
    }

    @Override
    public Scheme getScheme() {
        final String v = this.value("Scheme");
        if(StringUtils.isBlank(v)) {
            return parent.getScheme();
        }
        return Scheme.valueOf(v);
    }

    @Override
    public String[] getSchemes() {
        return parent.getSchemes();
    }

    @Override
    public String getContext() {
        final String v = this.value("Context");
        if(StringUtils.isBlank(v)) {
            return parent.getContext();
        }
        return v;
    }

    @Override
    public String getAuthorization() {
        final String v = this.value("Authorization");
        if(StringUtils.isBlank(v)) {
            return parent.getAuthorization();
        }
        return v;
    }

    @Override
    public Set<Location.Name> getRegions() {
        final List<String> regions = this.list("Regions");
        final Set<Location.Name> set = new HashSet<Location.Name>();
        for(String region : regions) {
            set.add(new Location.Name(region));
        }
        return set;
    }

    @Override
    public boolean isEncodingConfigurable() {
        return parent.isEncodingConfigurable();
    }

    @Override
    public boolean isAnonymousConfigurable() {
        if(StringUtils.isBlank(this.value("Anonymous Configurable"))) {
            return parent.isAnonymousConfigurable();
        }
        return this.bool("Anonymous Configurable");
    }

    @Override
    public boolean isUsernameConfigurable() {
        if(StringUtils.isBlank(this.value("Username Configurable"))) {
            return parent.isUsernameConfigurable();
        }
        return this.bool("Username Configurable");
    }

    @Override
    public boolean isPasswordConfigurable() {
        if(StringUtils.isBlank(this.value("Password Configurable"))) {
            return parent.isPasswordConfigurable();
        }
        return this.bool("Password Configurable");
    }

    @Override
    public boolean isHostnameConfigurable() {
        if(StringUtils.isBlank(this.value("Hostname Configurable"))) {
            return parent.isHostnameConfigurable();
        }
        return this.bool("Hostname Configurable");
    }

    @Override
    public boolean isPortConfigurable() {
        if(StringUtils.isBlank(this.value("Port Configurable"))) {
            return parent.isPortConfigurable();
        }
        return this.bool("Port Configurable");
    }

    @Override
    public boolean isUTCTimezone() {
        return parent.isUTCTimezone();
    }

    private String value(final String key) {
        final String value = dict.stringForKey(key);
        if(StringUtils.isBlank(value)) {
            log.debug("No value for key:" + key);
        }
        return value;
    }

    private List<String> list(final String key) {
        final List<String> list = dict.listForKey(key);
        if(null == list) {
            return Collections.emptyList();
        }
        return list;
    }

    private boolean bool(final String key) {
        return dict.booleanForKey(key);
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
        if(this.getContext() != null ? !this.getContext().equals(protocol.getContext()) : protocol.getContext() != null) {
            return false;
        }
        if(this.getAuthorization() != null ? !this.getAuthorization().equals(protocol.getAuthorization()) : protocol.getAuthorization() != null) {
            return false;
        }
        if(this.getProvider() != null ? !this.getProvider().equals(protocol.getProvider()) : protocol.getProvider() != null) {
            return false;
        }
        if(this.getDefaultHostname() != null ? !this.getDefaultHostname().equals(protocol.getDefaultHostname()) : protocol.getDefaultHostname() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = this.getIdentifier() != null ? this.getIdentifier().hashCode() : 0;
        result = 31 * result + (this.getScheme() != null ? this.getScheme().hashCode() : 0);
        result = 31 * result + (this.getContext() != null ? this.getContext().hashCode() : 0);
        result = 31 * result + (this.getAuthorization() != null ? this.getAuthorization().hashCode() : 0);
        result = 31 * result + (this.getProvider() != null ? this.getProvider().hashCode() : 0);
        result = 31 * result + (this.getDefaultHostname() != null ? this.getDefaultHostname().hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(final Protocol o) {
        return this.getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Profile{");
        sb.append("parent=").append(parent);
        sb.append(", image=").append(image);
        sb.append('}');
        return sb.toString();
    }
}