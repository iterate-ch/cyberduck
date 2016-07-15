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
import ch.cyberduck.core.googlestorage.GoogleStorageProtocol;
import ch.cyberduck.core.s3.S3Protocol;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

public abstract class CloudberryBookmarkCollection extends XmlBookmarkCollection {
    private static final Logger log = Logger.getLogger(CloudberryBookmarkCollection.class);

    private static final long serialVersionUID = 2245328157886337606L;

    @Override
    protected AbstractHandler getHandler() {
        return new ServerHandler();
    }

    private class ServerHandler extends AbstractHandler {
        private Host current = null;


        @Override
        public void startElement(String name, Attributes attrs) {
            switch(name) {
                case "Settings":
                    String type = attrs.getValue("xsi:type");
                    switch(type) {
                        case "GoogleSettings":
                            current = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new GoogleStorageProtocol().getDefaultPort());
                            break;
                        case "S3Settings":
                            current = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new S3Protocol().getDefaultPort());
                            break;
                        case "DunkelSettings":
                            current = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new S3Protocol().getDefaultPort());
                            break;
                        default:
                            log.warn("Unsupported connection type:" + type);
                            break;
                    }
                    break;
            }
        }

        @Override
        public void endElement(String name, String elementText) {
            if(null == current) {
                return;
            }
            switch(name) {
                case "ServicePoint":
                    current.setHostname(elementText);
                    break;
                case "AWSKey":
                    current.getCredentials().setUsername(elementText);
                    break;
                case "Account":
                    current.getCredentials().setUsername(elementText);
                    break;
                case "SharedKey":
                    current.getCredentials().setPassword(elementText);
                    break;
                case "Name":
                    current.setNickname(elementText);
                    break;
                case "Settings":
                    add(current);
                    current = null;
                    break;
            }
        }
    }
}
