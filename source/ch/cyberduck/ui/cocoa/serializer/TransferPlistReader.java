package ch.cyberduck.ui.cocoa.serializer;

/*
 * Copyright (c) 2009 David Kocher. All rights reserved.
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
import ch.cyberduck.core.serializer.HostReaderFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.serializer.TransferReaderFactory;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

/**
 * @version $Id$
 */
public class TransferPlistReader extends PlistReader<Transfer> {
    private static Logger log = Logger.getLogger(TransferPlistReader.class);

    public static void register() {
        TransferReaderFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends TransferReaderFactory {
        @Override
        public Reader<Transfer> create() {
            return new TransferPlistReader();
        }
    }

    public Transfer deserialize(NSDictionary dict, Session s) {
        NSObject kindObj = dict.objectForKey("Kind");
        if(kindObj != null) {
            int kind = Integer.parseInt(kindObj.toString());
            switch(kind) {
                case Transfer.KIND_DOWNLOAD:
                    return new DownloadTransfer(dict, s);
                case Transfer.KIND_UPLOAD:
                    return new UploadTransfer(dict, s);
                case Transfer.KIND_SYNC:
                    return new SyncTransfer(dict, s);
            }
        }
        log.error("Unknown transfer:" + kindObj);
        return null;
    }

    @Override
    public Transfer deserialize(NSDictionary dict) {
        NSObject hostObj = dict.objectForKey("Host");
        if(hostObj != null) {
            Host host = new Host(Rococoa.cast(hostObj, NSDictionary.class));
            Session s = SessionFactory.createSession(host);
            return this.deserialize(dict, s);
        }
        throw new IllegalArgumentException("Unknown transfer");
    }
}