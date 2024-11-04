package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.stream.JsonReader;

public class NetDrive2BookmarkCollection extends JsonBookmarkCollection {
    private static final Logger log = LogManager.getLogger(NetDrive2BookmarkCollection.class);

    @Override
    public String getName() {
        return "NetDrive 2";
    }

    @Override
    public String getBundleIdentifier() {
        return "net.netdrive.NetDrive2";
    }

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.netdrive2.location"));
    }

    @Override
    protected void parse(final ProtocolFactory protocols, final Local file) throws AccessDeniedException {
        try {
            final JsonReader reader = new JsonReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            reader.beginArray();
            String url;
            String user;
            boolean ssl;
            Protocol protocol;
            while(reader.hasNext()) {
                reader.beginObject();
                boolean skip = false;
                url = null;
                ssl = false;
                protocol = null;
                user = null;
                while(reader.hasNext()) {
                    final String name = reader.nextName();
                    switch(name) {
                        case "url":
                            url = this.readNext(name, reader);
                            if(StringUtils.isBlank(url)) {
                                skip = true;
                            }
                            break;
                        case "ssl":
                            ssl = reader.nextBoolean();
                            break;
                        case "user":
                            user = this.readNext(name, reader);
                            break;
                        case "type":
                            final String type = this.readNext(name, reader);
                            switch(type) {
                                case "google_cloud_storage":
                                    protocol = protocols.forType(Protocol.Type.googlestorage);
                                    break;
                                case "gdrive":
                                    protocol = protocols.forType(Protocol.Type.googledrive);
                                    break;
                                default:
                                    protocol = protocols.forName(type);
                            }
                            break;

                        default:
                            log.warn("Ignore property {}", name);
                            reader.skipValue();
                            break;
                    }
                }
                reader.endObject();
                if(!skip && protocol != null && StringUtils.isNotBlank(user)) {
                    if(ssl) {
                        switch(protocol.getType()) {
                            case ftp:
                                protocol = protocols.forScheme(Scheme.ftps);
                                break;
                            case dav:
                                protocol = protocols.forScheme(Scheme.davs);
                                break;
                        }
                    }
                    try {
                        this.add(new HostParser(protocols, protocol).get(url));
                    }
                    catch(HostParserException e) {
                        log.warn(e);
                    }
                }
            }
            reader.endArray();
        }
        catch(IllegalStateException | IOException e) {
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }
}
