package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.serializer.impl.PlistDeserializer;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @version $Id$
 */
public class FlowBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = Logger.getLogger(FlowBookmarkCollection.class);

    private static final long serialVersionUID = 2017398431454618548L;

    @Override
    public String getBundleIdentifier() {
        return "com.extendmac.Flow";
    }

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.flow.location"));
    }

    @Override
    protected void parse(final Local file) throws AccessDeniedException {
        NSDictionary serialized = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        if(null == serialized) {
            throw new AccessDeniedException(String.format("Invalid bookmark file %s", file));
        }
        this.parse(serialized);
    }

    private void parse(NSDictionary serialized) {
        List<NSDictionary> items = new PlistDeserializer(serialized).listForKey("BookmarkItems");
        for(NSDictionary item : items) {
            final PlistDeserializer bookmark = new PlistDeserializer(item);
            String classname = bookmark.stringForKey("ClassName");
            if(null == classname) {
                continue;
            }
            if("Bookmark".equals(classname)) {
                this.read(bookmark);
            }
            if("BookmarkFolder".equals(classname)) {
                this.parse(item);
            }
        }
    }

    private boolean read(final PlistDeserializer bookmark) {
        final String server = bookmark.stringForKey("Server");
        if(null == server) {
            return false;
        }
        final Host host = new Host(server);
        final String port = bookmark.stringForKey("Port");
        if(StringUtils.isNotBlank(port)) {
            host.setPort(Integer.parseInt(port));
        }
        String path = bookmark.stringForKey("InitialPath");
        if(StringUtils.isNotBlank(path)) {
            host.setDefaultPath(path);
        }
        String name = bookmark.stringForKey("Name");
        if(StringUtils.isNotBlank(name)) {
            host.setNickname(name);
        }
        String user = bookmark.stringForKey("Username");
        if(StringUtils.isNotBlank(user)) {
            host.getCredentials().setUsername(user);
        }
        else {
            host.getCredentials().setUsername(
                    Preferences.instance().getProperty("connection.login.anon.name"));
        }
        final String mode = bookmark.stringForKey("PreferredFTPDataConnectionType");
        if(StringUtils.isNotBlank(mode)) {
            if("Passive".equals(mode)) {
                host.setFTPConnectMode(FTPConnectMode.passive);
            }
            if("Active".equals(mode)) {
                host.setFTPConnectMode(FTPConnectMode.active);
            }
        }
        final String protocol = bookmark.stringForKey("Protocol");
        if(StringUtils.isNotBlank(protocol)) {
            try {
                switch(Integer.parseInt(protocol)) {
                    case 0:
                        host.setProtocol(ProtocolFactory.FTP);
                        break;
                    case 1:
                        host.setProtocol(ProtocolFactory.SFTP);
                        break;
                    case 3:
                        host.setProtocol(ProtocolFactory.S3_SSL);
                        break;
                    case 2:
                    case 4:
                        if(host.getPort() == ProtocolFactory.WEBDAV_SSL.getDefaultPort()) {
                            host.setProtocol(ProtocolFactory.WEBDAV_SSL);
                        }
                        else {
                            host.setProtocol(ProtocolFactory.WEBDAV);
                        }
                        break;
                }
            }
            catch(NumberFormatException e) {
                log.warn("Unknown protocol:" + e.getMessage());
            }
        }
        this.add(host);
        return true;
    }
}