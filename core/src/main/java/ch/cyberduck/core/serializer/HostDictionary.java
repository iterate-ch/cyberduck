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
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.TimeZone;

public class HostDictionary<T> {
    private static final Logger log = LogManager.getLogger(HostDictionary.class);

    private final DeserializerFactory<T> factory;
    private final ProtocolFactory protocols;

    public HostDictionary() {
        this(ProtocolFactory.get());
    }

    public HostDictionary(final ProtocolFactory protocols) {
        this(protocols, new DeserializerFactory<>());
    }

    public HostDictionary(final DeserializerFactory<T> factory) {
        this(ProtocolFactory.get(), factory);
    }

    public HostDictionary(final ProtocolFactory protocols, final DeserializerFactory<T> factory) {
        this.protocols = protocols;
        this.factory = factory;
    }

    public Host deserialize(final T serialized) {
        final Deserializer<T> dict = factory.create(serialized);
        Object protocolObj = dict.stringForKey("Protocol");
        if(protocolObj == null) {
            log.warn("Missing protocol key in {}", dict);
            return null;
        }
        final Protocol protocol;
        final String identifier = protocolObj.toString();
        final Object providerObj = dict.stringForKey("Provider");
        if(null == providerObj) {
            protocol = protocols.forName(identifier);
        }
        else {
            protocol = protocols.forName(identifier, providerObj.toString());
        }
        if(null != protocol) {
            final Host bookmark = new Host(protocol);
            final Object hostnameObj = dict.stringForKey("Hostname");
            if(hostnameObj != null) {
                bookmark.setHostname(hostnameObj.toString());
            }
            final Object uuidObj = dict.stringForKey("UUID");
            if(uuidObj != null) {
                bookmark.setUuid(uuidObj.toString());
            }
            final Object usernameObj = dict.stringForKey("Username");
            if(usernameObj != null) {
                bookmark.getCredentials().setUsername(usernameObj.toString());
            }
            final Object cdnCredentialsObj = dict.stringForKey("CDN Credentials");
            if(cdnCredentialsObj != null) {
                bookmark.getCdnCredentials().setUsername(cdnCredentialsObj.toString());
            }
            // Legacy
            final String keyObjDeprecated = dict.stringForKey("Private Key File");
            if(keyObjDeprecated != null) {
                bookmark.getCredentials().setIdentity(LocalFactory.get(keyObjDeprecated));
            }
            final T keyObj = dict.objectForKey("Private Key File Dictionary");
            if(keyObj != null) {
                bookmark.getCredentials().setIdentity(new LocalDictionary<>(factory).deserialize(keyObj));
            }
            final Object certObj = dict.stringForKey("Client Certificate");
            if(certObj != null) {
                bookmark.getCredentials().setCertificate(certObj.toString());
            }
            final Object portObj = dict.stringForKey("Port");
            if(portObj != null) {
                bookmark.setPort(Integer.parseInt(portObj.toString()));
            }
            final Object pathObj = dict.stringForKey("Path");
            if(pathObj != null) {
                bookmark.setDefaultPath(pathObj.toString());
            }
            // Legacy
            final Object workdirObjDeprecated = dict.stringForKey("Workdir");
            if(workdirObjDeprecated != null) {
                bookmark.setWorkdir(new Path(workdirObjDeprecated.toString(), EnumSet.of(Path.Type.directory)));
            }
            final T workdirObj = dict.objectForKey("Workdir Dictionary");
            if(workdirObj != null) {
                bookmark.setWorkdir(new PathDictionary<>(factory).deserialize(workdirObj));
            }
            final Object nicknameObj = dict.stringForKey("Nickname");
            if(nicknameObj != null) {
                bookmark.setNickname(nicknameObj.toString());
            }
            final Object encodingObj = dict.stringForKey("Encoding");
            if(encodingObj != null) {
                bookmark.setEncoding(encodingObj.toString());
            }
            final Object connectModeObj = dict.stringForKey("FTP Connect Mode");
            if(connectModeObj != null) {
                bookmark.setFTPConnectMode(FTPConnectMode.valueOf(connectModeObj.toString()));
            }
            final Object transferObj = dict.stringForKey("Transfer Connection");
            if(transferObj != null) {
                final Host.TransferType transfer = Host.TransferType.valueOf(transferObj.toString());
                if(PreferencesFactory.get().getList("queue.transfer.type.enabled").contains(transfer.name())) {
                    bookmark.setTransfer(transfer);
                }
            }
            else {
                // Legacy
                Object connObj = dict.stringForKey("Maximum Connections");
                if(connObj != null) {
                    if(1 == Integer.parseInt(connObj.toString())) {
                        bookmark.setTransfer(Host.TransferType.browser);
                    }
                }
            }
            // Legacy
            final Object downloadObjDeprecated = dict.stringForKey("Download Folder");
            if(downloadObjDeprecated != null) {
                bookmark.setDownloadFolder(LocalFactory.get(downloadObjDeprecated.toString()));
            }
            final T downloadObj = dict.objectForKey("Download Folder Dictionary");
            if(downloadObj != null) {
                bookmark.setDownloadFolder(new LocalDictionary<>(factory).deserialize(downloadObj));
            }
            final T uploadObj = dict.objectForKey("Upload Folder Dictionary");
            if(uploadObj != null) {
                bookmark.setUploadFolder(new LocalDictionary<>(factory).deserialize(uploadObj));
            }
            final Object timezoneObj = dict.stringForKey("Timezone");
            if(timezoneObj != null) {
                bookmark.setTimezone(TimeZone.getTimeZone(timezoneObj.toString()));
            }
            final Object commentObj = dict.stringForKey("Comment");
            if(commentObj != null) {
                bookmark.setComment(commentObj.toString());
            }
            final Object urlObj = dict.stringForKey("Web URL");
            if(urlObj != null) {
                bookmark.setWebURL(urlObj.toString());
            }
            final Object accessObj = dict.stringForKey("Access Timestamp");
            if(accessObj != null) {
                bookmark.setTimestamp(new Date(Long.parseLong(accessObj.toString())));
            }
            final Object volumeObj = dict.stringForKey("Volume");
            if(volumeObj != null) {
                bookmark.setVolume(LocalFactory.get(volumeObj.toString()));
            }
            final Object readonlyObj = dict.stringForKey("Readonly");
            if(readonlyObj != null) {
                bookmark.setReadonly(Boolean.valueOf(readonlyObj.toString()));
            }
            final Map customObj = dict.mapForKey("Custom");
            if(customObj != null) {
                bookmark.setCustom(customObj);
            }
            final Object labelObj = dict.stringForKey("Labels");
            if(labelObj != null) {
                bookmark.setLabels(new HashSet<>(dict.listForKey("Labels")));
            }
            return bookmark;
        }
        else {
            log.warn("No protocol registered for identifier {}", protocolObj);
            return null;
        }
    }
}
