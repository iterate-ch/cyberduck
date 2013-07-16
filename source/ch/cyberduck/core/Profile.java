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

import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @version $Id$
 */
public final class Profile extends AbstractProtocol implements Serializable {
    private static final Logger log = Logger.getLogger(Profile.class);

    private Deserializer dict;

    /**
     * The actual protocol implementation registered
     */
    private Protocol parent;

    private Local disk;

    public <T> Profile(final T serialized) {
        dict = DeserializerFactory.createDeserializer(serialized);
        final String protocol = this.getValue("Protocol");
        if(StringUtils.isBlank(protocol)) {
            log.error("Missing protocol in profile");
            parent = ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"));
        }
        else {
            parent = ProtocolFactory.forName(protocol);
        }
        disk = this.write(this.getValue("Disk"));
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer serializer = SerializerFactory.createSerializer();
        serializer.setStringForKey("Protocol", parent.getIdentifier());
        serializer.setStringForKey("Scheme", this.getScheme().toString());
        serializer.setStringForKey("Vendor", this.getProvider());
        serializer.setStringForKey("Description", this.getDescription());
        serializer.setStringForKey("Default Hostname", this.getDefaultHostname());
        serializer.setStringForKey("Default Port", String.valueOf(this.getDefaultPort()));
        serializer.setStringForKey("Username Placeholder", this.getUsernamePlaceholder());
        serializer.setStringForKey("Password Placeholder", this.getPasswordPlaceholder());
        return serializer.<T>getSerialized();
    }

    public Protocol getProtocol() {
        return parent;
    }

    /**
     * @return False if missing required fields in profile.
     */
    @Override
    public boolean isEnabled() {
        return StringUtils.isNotBlank(this.getValue("Protocol"))
                && StringUtils.isNotBlank(this.getValue("Vendor"));
    }

    private String getValue(final String key) {
        final String value = dict.stringForKey(key);
        if(StringUtils.isBlank(value)) {
            log.debug("No value for key:" + key);
        }
        return value;
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
        final String v = this.getValue("Username Placeholder");
        if(StringUtils.isBlank(v)) {
            return parent.getUsernamePlaceholder();
        }
        return v;
    }

    @Override
    public String getPasswordPlaceholder() {
        final String v = this.getValue("Password Placeholder");
        if(StringUtils.isBlank(v)) {
            return parent.getPasswordPlaceholder();
        }
        return v;
    }

    @Override
    public String getDefaultHostname() {
        final String v = this.getValue("Default Hostname");
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultHostname();
        }
        return v;
    }

    @Override
    public String getProvider() {
        final String v = this.getValue("Vendor");
        if(StringUtils.isBlank(v)) {
            return parent.getProvider();
        }
        return v;
    }

    @Override
    public String getName() {
        final String v = this.getValue("Name");
        if(StringUtils.isBlank(v)) {
            return parent.getName();
        }
        return v;
    }

    @Override
    public String getDescription() {
        final String v = this.getValue("Description");
        if(StringUtils.isBlank(v)) {
            return parent.getDescription();
        }
        return v;
    }

    @Override
    public int getDefaultPort() {
        final String v = this.getValue("Default Port");
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
    public String disk() {
        if(null == disk) {
            return parent.disk();
        }
        // Temporary file
        return disk.getAbsolute();
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
        final Local file = LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"),
                String.format("%s.ico", UUID.randomUUID().toString()));
        file.delete(true);
        try {
            final OutputStream out = file.getOutputStream(false);
            try {
                IOUtils.write(favicon, out);
            }
            finally {
                IOUtils.closeQuietly(out);
            }
            return file;
        }
        catch(IOException e) {
            log.error("Error writing temporary file", e);
        }
        return null;
    }

    @Override
    public boolean isWebUrlConfigurable() {
        return parent.isWebUrlConfigurable();
    }

    @Override
    public Scheme getScheme() {
        final String v = this.getValue("Scheme");
        if(StringUtils.isBlank(v)) {
            return parent.getScheme();
        }
        return Scheme.valueOf(v);
    }

    @Override
    public String getContext() {
        final String v = this.getValue("Context");
        if(StringUtils.isBlank(v)) {
            return parent.getContext();
        }
        return v;
    }

    @Override
    public boolean isEncodingConfigurable() {
        return parent.isEncodingConfigurable();
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return parent.isAnonymousConfigurable();
    }

    @Override
    public boolean isUTCTimezone() {
        return parent.isUTCTimezone();
    }

    @Override
    public Session createSession(final Host host) {
        return parent.createSession(host);
    }
}