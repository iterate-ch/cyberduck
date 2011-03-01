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

import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id:$
 */
public class Profile extends Protocol implements Serializable {
    private static Logger log = Logger.getLogger(Profile.class);

    private Deserializer dict;

    /**
     * The actual protocol implementation registered
     */
    private Protocol parent;

    public <T> Profile(T dict) {
        this.init(dict);
    }

    private Profile() {
        super();
    }

    public <T> void init(T serialized) {
        dict = DeserializerFactory.createDeserializer(serialized);
        parent = ProtocolFactory.forName(dict.stringForKey("Protocol"));
    }

    public <T> T getAsDictionary() {
        final Serializer dict = SerializerFactory.createSerializer();
        dict.setStringForKey("Identifier", this.getIdentifier());
        dict.setStringForKey("Protocol", parent.getIdentifier());
        dict.setStringForKey("Description", this.getDescription());
        dict.setStringForKey("Default Hostname", this.getDefaultHostname());
        dict.setStringForKey("Default Port", String.valueOf(this.getDefaultPort()));
        dict.setStringForKey("Username Placeholder", this.getUsernamePlaceholder());
        dict.setStringForKey("Password Placeholder", this.getPasswordPlaceholder());
        return dict.<T>getSerialized();
    }

    @Override
    public void register() {
        super.register();
    }

    public Protocol getProtocol() {
        return parent;
    }

    /**
     * Custom profile always enabled.
     *
     * @return True
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    private String getValue(String key) {
        final String value = dict.stringForKey(key);
        if(StringUtils.isBlank(value)) {
            log.warn("No value for key:" + key);
        }
        return value;
    }

    @Override
    public String getIdentifier() {
        final String v = this.getValue("Identifier");
        if(StringUtils.isBlank(v)) {
            return parent.getIdentifier();
        }
        return v;
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
            log.warn("Port is not a number:" + e.getMessage());
        }
        return parent.getDefaultPort();
    }

    @Override
    public String getName() {
        return parent.getName();
    }

    @Override
    public String icon() {
        final String v = this.getValue("Icon");
        if(StringUtils.isBlank(v)) {
            return parent.favicon();
        }
        final byte[] favicon = Base64.decodeBase64(v);
        final Local file = LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"),
                this.getIdentifier() + ".ico");
        try {
            IOUtils.write(favicon, file.getOutputStream(false));
            return file.getAbsolute();
        }
        catch(IOException e) {
            log.error("Error writing temporary file:" + e.getMessage());
        }
        return parent.favicon();
    }

    @Override
    public String disk() {
        return parent.disk();
    }

    @Override
    public String favicon() {
        return parent.favicon();
    }

    @Override
    public boolean isSecure() {
        return parent.isSecure();
    }

    @Override
    public boolean isHostnameConfigurable() {
        return false;
    }

    @Override
    public boolean isWebUrlConfigurable() {
        return parent.isHostnameConfigurable();
    }

    @Override
    public String getScheme() {
        return parent.getScheme();
    }

    @Override
    public String[] getSchemes() {
        return parent.getSchemes();
    }

    @Override
    public boolean isPortConfigurable() {
        return parent.isPortConfigurable();
    }

    @Override
    public boolean isEncodingConfigurable() {
        return parent.isEncodingConfigurable();
    }

    @Override
    public boolean isConnectModeConfigurable() {
        return parent.isConnectModeConfigurable();
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
    public boolean validate(Credentials credentials) {
        return parent.validate(credentials);
    }

    @Override
    public SessionFactory getSessionFactory() {
        return parent.getSessionFactory();
    }

    @Override
    public PathFactory getPathFactory() {
        return parent.getPathFactory();
    }
}