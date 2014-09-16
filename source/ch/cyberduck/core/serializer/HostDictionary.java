package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.ftp.FTPConnectMode;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class HostDictionary {
    private static final Logger log = Logger.getLogger(HostDictionary.class);


    public <T> Host deserialize(final T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        Object hostnameObj = dict.stringForKey("Hostname");
        if(hostnameObj != null) {
            final Host bookmark = new Host(hostnameObj.toString());
            Object uuidObj = dict.stringForKey("UUID");
            if(uuidObj != null) {
                bookmark.setUuid(uuidObj.toString());
            }
            Object protocolObj = dict.stringForKey("Protocol");
            if(protocolObj != null) {
                final Protocol p = ProtocolFactory.forName(protocolObj.toString());
                if(null != p) {
                    bookmark.setProtocol(p);
                }
                else {
                    log.warn(String.format("Protocol %s no more available", protocolObj));
                    return null;
                }
            }
            Object providerObj = dict.stringForKey("Provider");
            if(providerObj != null) {
                final Protocol provider = ProtocolFactory.forName(providerObj.toString());
                if(null != provider) {
                    bookmark.setProtocol(provider);
                }
                else {
                    log.warn(String.format("Provider %s no more available. Default to %s", providerObj, bookmark.getProtocol()));
                }
            }
            Object usernameObj = dict.stringForKey("Username");
            if(usernameObj != null) {
                bookmark.getCredentials().setUsername(usernameObj.toString());
            }
            Object passwordObj = dict.stringForKey("Password");
            if(passwordObj != null) {
                bookmark.getCredentials().setPassword(passwordObj.toString());
            }
            Object cdnCredentialsObj = dict.stringForKey("CDN Credentials");
            if(cdnCredentialsObj != null) {
                bookmark.getCdnCredentials().setUsername(cdnCredentialsObj.toString());
            }
            // Legacy
            String keyObjDeprecated = dict.stringForKey("Private Key File");
            if(keyObjDeprecated != null) {
                bookmark.getCredentials().setIdentity(LocalFactory.createLocal(keyObjDeprecated));
            }
            Object keyObj = dict.objectForKey("Private Key File Dictionary");
            if(keyObj != null) {
                bookmark.getCredentials().setIdentity(new LocalDictionary().deserialize(keyObj));
            }
            Object portObj = dict.stringForKey("Port");
            if(portObj != null) {
                bookmark.setPort(Integer.parseInt(portObj.toString()));
            }
            Object pathObj = dict.stringForKey("Path");
            if(pathObj != null) {
                bookmark.setDefaultPath(pathObj.toString());
            }
            // Legacy
            Object workdirObjDeprecated = dict.stringForKey("Workdir");
            if(workdirObjDeprecated != null) {
                bookmark.setWorkdir(new Path(workdirObjDeprecated.toString(), EnumSet.of(Path.Type.directory)));
            }
            Object workdirObj = dict.objectForKey("Workdir Dictionary");
            if(workdirObj != null) {
                bookmark.setWorkdir(new PathDictionary().deserialize(workdirObj));
            }
            Object nicknameObj = dict.stringForKey("Nickname");
            if(nicknameObj != null) {
                bookmark.setNickname(nicknameObj.toString());
            }
            Object encodingObj = dict.stringForKey("Encoding");
            if(encodingObj != null) {
                bookmark.setEncoding(encodingObj.toString());
            }
            Object connectModeObj = dict.stringForKey("FTP Connect Mode");
            if(connectModeObj != null) {
                if(connectModeObj.toString().equals(FTPConnectMode.PORT.toString())) {
                    bookmark.setFTPConnectMode(FTPConnectMode.PORT);
                }
                if(connectModeObj.toString().equals(FTPConnectMode.PASV.toString())) {
                    bookmark.setFTPConnectMode(FTPConnectMode.PASV);
                }
            }
            Object transferObj = dict.stringForKey("Transfer Connection");
            if(transferObj != null) {
                bookmark.setTransfer(Host.TransferType.valueOf(transferObj.toString()));
            }
            else {
                Object connObj = dict.stringForKey("Maximum Connections");
                if(connObj != null) {
                    if(1 == Integer.valueOf(connObj.toString())) {
                        bookmark.setTransfer(Host.TransferType.browser);
                    }
                    else {
                        bookmark.setTransfer(Host.TransferType.newconnection);
                    }
                }
            }
            // Legacy
            Object downloadObjDeprecated = dict.stringForKey("Download Folder");
            if(downloadObjDeprecated != null) {
                bookmark.setDownloadFolder(LocalFactory.createLocal(downloadObjDeprecated.toString()));
            }
            Object downloadObj = dict.objectForKey("Download Folder Dictionary");
            if(downloadObj != null) {
                bookmark.setDownloadFolder(new LocalDictionary().deserialize(downloadObj));
            }
            Object timezoneObj = dict.stringForKey("Timezone");
            if(timezoneObj != null) {
                bookmark.setTimezone(TimeZone.getTimeZone(timezoneObj.toString()));
            }
            Object commentObj = dict.stringForKey("Comment");
            if(commentObj != null) {
                bookmark.setComment(commentObj.toString());
            }
            Object urlObj = dict.stringForKey("Web URL");
            if(urlObj != null) {
                bookmark.setWebURL(urlObj.toString());
            }
            Object accessObj = dict.stringForKey("Access Timestamp");
            if(accessObj != null) {
                bookmark.setTimestamp(new Date(Long.parseLong(accessObj.toString())));
            }
            return bookmark;
        }
        return null;
    }
}
