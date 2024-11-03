package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.impl.jna.PlistDeserializer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CloudMounterBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = LogManager.getLogger(CloudMounterBookmarkCollection.class);

    @Override
    public String getName() {
        return "CloudMounter";
    }

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.cloudmounter.location"));
    }

    @Override
    protected void parse(final ProtocolFactory protocols, final Local file) throws AccessDeniedException {
        final NSDictionary serialized = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        if(null == serialized) {
            throw new LocalAccessDeniedException(String.format("Invalid bookmark file %s", file));
        }
        final List<NSDictionary> array = new PlistDeserializer(serialized).listForKey("CustomPluginSettings");
        if(null == array) {
            log.warn("Missing key CustomPluginSettings");
            return;
        }
        for(NSDictionary dict : array) {
            final PlistDeserializer bookmark = new PlistDeserializer(dict);
            final String identifier = bookmark.stringForKey("MountFSClassName");
            if(StringUtils.isBlank(identifier)) {
                log.warn("Missing key MountFSClassName");
                continue;
            }
            final Protocol protocol;
            switch(identifier) {
                case "FtpConnection":
                    protocol = protocols.forType(Protocol.Type.ftp);
                    break;
                case "WebDAVConnection":
                    protocol = protocols.forType(Protocol.Type.dav);
                    break;
                case "OpenStackConnection":
                    protocol = protocols.forType(Protocol.Type.swift);
                    break;
                case "BBConnection":
                    protocol = protocols.forType(Protocol.Type.b2);
                    break;
                case "S3Connection":
                    protocol = protocols.forType(Protocol.Type.s3);
                    break;
                case "DropboxConnection":
                    protocol = protocols.forType(Protocol.Type.dropbox);
                    break;
                case "GDriveConnection":
                    protocol = protocols.forType(Protocol.Type.googledrive);
                    break;
                default:
                    protocol = null;
                    break;

            }
            if(null == protocol) {
                log.warn("Unable to determine protocol for {}", identifier);
                continue;
            }
            final NSDictionary details = bookmark.objectForKey("MountFSOptions");
            if(null == details) {
                continue;
            }
            final PlistDeserializer options = new PlistDeserializer(details);
            final String hostname = options.stringForKey("host");
            if(StringUtils.isBlank(hostname)) {
                continue;
            }
            final Host host = new Host(protocol, hostname, new Credentials(options.stringForKey("login")));
            host.setNickname(bookmark.stringForKey("MountFSLabel"));
            host.setDefaultPath(options.stringForKey("remotePath"));
            this.add(host);
        }
    }

    @Override
    public String getBundleIdentifier() {
        return "com.eltima.cloudmounter";
    }
}
