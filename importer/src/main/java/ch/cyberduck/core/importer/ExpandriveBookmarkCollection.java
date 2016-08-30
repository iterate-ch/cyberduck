package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.stream.JsonReader;

public abstract class ExpandriveBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = Logger.getLogger(ExpandriveBookmarkCollection.class);

    @Override
    protected void parse(final Local file) throws AccessDeniedException {
        try {
            final JsonReader reader = new JsonReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
            reader.beginArray();
            while(reader.hasNext()) {
                reader.beginObject();
                final Host current = new Host(new FTPProtocol(), PreferencesFactory.get().getProperty("connection.hostname.default"));
                while(reader.hasNext()) {
                    final String name = reader.nextName();
                    switch(name) {
                        case "server":
                            current.setHostname(reader.nextString());
                            break;
                        case "username":
                            current.getCredentials().setUsername(reader.nextString());
                            break;
                        case "private_key_file":
                            current.getCredentials().setIdentity(LocalFactory.get(reader.nextString()));
                            break;
                        case "remotePath":
                            current.setDefaultPath(reader.nextString());
                            break;
                        case "type":
                            final Protocol type = ProtocolFactory.forName(reader.nextString());
                            if(null != type) {
                                current.setProtocol(type);
                            }
                            break;
                        case "protocol":
                            final Protocol protocol = ProtocolFactory.forName(reader.nextString());
                            if(null != protocol) {
                                current.setProtocol(protocol);
                                // Reset port to default
                                current.setPort(-1);
                            }
                            break;
                        case "name":
                            current.setNickname(reader.nextString());
                            break;
                        case "region":
                            current.setRegion(reader.nextString());
                            break;
                        default:
                            log.warn(String.format("Ignore property %s", name));
                            reader.skipValue();
                            break;
                    }
                }
                reader.endObject();
                this.add(current);
            }
            reader.endArray();
        }
        catch(IllegalStateException | IOException e) {
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }
}
