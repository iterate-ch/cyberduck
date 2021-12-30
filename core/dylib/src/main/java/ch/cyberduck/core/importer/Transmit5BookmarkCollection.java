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

import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.impl.jna.PlistDeserializer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Transmit5BookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = LogManager.getLogger(Transmit5BookmarkCollection.class);

    private static final long serialVersionUID = 2422398644582883578L;

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.transmit5.location"));
    }

    @Override
    public String getBundleIdentifier() {
        return "com.panic.Transmit";
    }

    @Override
    public String getName() {
        return "Transmit 5";
    }

    public String getConfiguration() {
        return String.format("bookmark.import.%s%d", this.getBundleIdentifier(), 5);
    }

    @Override
    protected void parse(final ProtocolFactory protocols, final Local folder) throws AccessDeniedException {
        for(Local f : folder.list().filter(new NullFilter<Local>() {
            @Override
            public boolean accept(Local file) {
                if(file.isFile()) {
                    return "favoriteMetadata".equals(file.getExtension());
                }
                return false;
            }
        })) {
            this.read(protocols, f);
        }
    }

    protected void read(final ProtocolFactory protocols, final Local file) throws LocalAccessDeniedException {
        final NSDictionary serialized = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        if(null == serialized) {
            throw new LocalAccessDeniedException(String.format("Invalid bookmark file %s", file));
        }
        final PlistDeserializer bookmark = new PlistDeserializer(serialized);
        final String identifier = bookmark.stringForKey("com_panic_transmit_protocol");
        if(StringUtils.isBlank(identifier)) {
            log.warn("Missing key com_panic_transmit_protocol");
            return;
        }
        final String hostname = bookmark.stringForKey("com_panic_transmit_server");
        if(StringUtils.isBlank(hostname)) {
            log.warn("Missing key com_panic_transmit_server");
            return;
        }
        final Protocol protocol;
        switch(identifier) {
            case "FTP":
            case "FTP with Implicit SSL":
            case "FTP with TLS/SSL":
                protocol = protocols.forType(Protocol.Type.ftp);
                break;
            case "SFTP":
                protocol = protocols.forType(Protocol.Type.sftp);
                break;
            case "WebDAV":
            case "WebDAV HTTPS":
                protocol = protocols.forType(Protocol.Type.dav);
                break;
            case "OpenStack Swift":
            case "Rackspace Cloud Files":
                protocol = protocols.forType(Protocol.Type.swift);
                break;
            case "Backblaze B2":
                protocol = protocols.forType(Protocol.Type.b2);
                break;
            case "Amazon S3":
                protocol = protocols.forType(Protocol.Type.s3);
                break;
            case "Dropbox":
                protocol = protocols.forType(Protocol.Type.dropbox);
                break;
            case "Google Drive":
                protocol = protocols.forType(Protocol.Type.googledrive);
                break;
            case "Microsoft Azure":
                protocol = protocols.forType(Protocol.Type.azure);
                break;
            case "Microsoft OneDrive":
            case "Microsoft OneDrive for Business":
                protocol = protocols.forType(Protocol.Type.onedrive);
                break;
            default:
                protocol = null;
                break;

        }
        if(null == protocol) {
            log.warn(String.format("Unable to determine protocol for %s", identifier));
            return;
        }
        final Host host = new Host(protocol, hostname, new Credentials(bookmark.stringForKey("com_panic_transmit_username")));
        host.setNickname(bookmark.stringForKey("com_panic_transmit_nickname"));
        host.setDefaultPath(bookmark.stringForKey("com_panic_transmit_remotePath"));
        final String port = bookmark.stringForKey("com_panic_transmit_port");
        if(StringUtils.isNotBlank(port)) {
            try {
                host.setPort(Integer.parseInt(port));
            }
            catch(NumberFormatException e) {
                log.warn(String.format("Ignore invalid port number %s", port));
            }
        }
        this.add(host);
    }
}
