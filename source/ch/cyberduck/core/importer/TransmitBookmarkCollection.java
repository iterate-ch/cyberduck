package ch.cyberduck.core.importer;

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

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class TransmitBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(TransmitBookmarkCollection.class);

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.transmit.location"));
    }

    @Override
    public String getBundleIdentifier() {
        return "com.panic.Transmit";
    }

    @Override
    protected void parse(Local file) {
        NSDictionary serialized = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        if(null == serialized) {
            log.error("Invalid bookmark file:" + file);
            return;
        }
        // Adds a class translation mapping to NSKeyedUnarchiver whereby objects encoded with a given class name
        // are decoded as instances of a given class instead.
        TransmitFavoriteCollection c = Rococoa.createClass("CDTransmitImportFavoriteCollection", TransmitFavoriteCollection.class);
        NSKeyedUnarchiver.setClass_forClassName(c, "FavoriteCollection");
        NSKeyedUnarchiver.setClass_forClassName(c, "HistoryCollection");

        TransmitFavorite f = Rococoa.createClass("CDTransmitImportFavorite", TransmitFavorite.class);
        NSKeyedUnarchiver.setClass_forClassName(f, "Favorite");

        NSData collectionsData = Rococoa.cast(serialized.objectForKey("FavoriteCollections"), NSData.class);
        TransmitFavoriteCollection rootCollection
                = Rococoa.cast(NSKeyedUnarchiver.unarchiveObjectWithData(collectionsData), TransmitFavoriteCollection.class);

        NSArray collections = rootCollection.favorites(); //The root has collections
        NSEnumerator collectionsEnumerator = collections.objectEnumerator();
        NSObject next;
        while(((next = collectionsEnumerator.nextObject()) != null)) {
            TransmitFavoriteCollection collection = Rococoa.cast(next, TransmitFavoriteCollection.class);
            if("History".equals(collection.name())) {
                continue;
            }
            NSArray favorites = collection.favorites();
            NSEnumerator favoritesEnumerator = favorites.objectEnumerator();
            NSObject favorite;
            while((favorite = favoritesEnumerator.nextObject()) != null) {
                this.parse(Rococoa.cast(favorite, TransmitFavorite.class));
            }
        }
    }

    private void parse(TransmitFavorite favorite) {
        String server = favorite.server();
        if(StringUtils.isBlank(server)) {
            log.warn("No server name:" + server);
            return;
        }
        int port = favorite.port();
        if(0 == port) {
            port = -1;
        }
        String protocolstring = favorite.protocol();
        if(StringUtils.isBlank(protocolstring)) {
            log.warn("Unknown protocol:" + protocolstring);
            return;
        }
        Protocol protocol;
        if("FTP".equals(protocolstring)) {
            protocol = Protocol.FTP;
        }
        else if("SFTP".equals(protocolstring)) {
            protocol = Protocol.SFTP;
        }
        else if("FTPTLS".equals(protocolstring)) {
            protocol = Protocol.FTP_TLS;
        }
        else if("FTPSSL".equals(protocolstring)) {
            protocol = Protocol.FTP_TLS;
        }
        else if("S3".equals(protocolstring)) {
            protocol = Protocol.S3_SSL;
        }
        else if("WebDAV".equals(protocolstring)) {
            protocol = Protocol.WEBDAV;
        }
        else if("WebDAVS".equals(protocolstring)) {
            protocol = Protocol.WEBDAV_SSL;
        }
        else {
            log.warn("Unknown protocol:" + protocolstring);
            return;
        }
        Host bookmark = new Host(protocol, server, port);
        String nickname = favorite.nickname();
        if(StringUtils.isNotBlank(nickname)) {
            bookmark.setNickname(nickname);
        }
        String user = favorite.username();
        if(StringUtils.isNotBlank(user)) {
            bookmark.setCredentials(user, null);
        }
        String path = favorite.initialPath();
        if(StringUtils.isNotBlank(path)) {
            bookmark.setDefaultPath(path);
        }
        this.add(bookmark);
    }

    public interface TransmitFavoriteCollection extends ObjCClass {
        String name();

        NSArray favorites();
    }

    public interface TransmitFavorite extends ObjCClass {
        String server();

        String nickname();

        String username();

        String password();

        String protocol();

        String initialPath();

        int port();
    }
}
