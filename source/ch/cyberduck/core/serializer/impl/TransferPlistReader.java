package ch.cyberduck.core.serializer.impl;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.TransferReaderFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.copy.CopyTransfer;
import ch.cyberduck.core.transfer.download.DownloadTransfer;
import ch.cyberduck.core.transfer.synchronisation.SyncTransfer;
import ch.cyberduck.core.transfer.upload.UploadTransfer;
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
            switch(Transfer.Type.values()[Integer.parseInt(kindObj.toString())]) {
                case download:
                    return new DownloadTransfer(dict, s);
                case upload:
                    return new UploadTransfer(dict, s);
                case synchronisation:
                    return new SyncTransfer(dict, s);
                case copy:
                    return new CopyTransfer(dict, s);
            }
        }
        log.error(String.format("Unknown transfer %s", kindObj));
        return null;
    }

    @Override
    public Transfer deserialize(NSDictionary dict) {
        NSObject hostObj = dict.objectForKey("Host");
        if(hostObj != null) {
            final Host host = new Host(Rococoa.cast(hostObj, NSDictionary.class));
            return this.deserialize(dict, SessionFactory.createSession(host));
        }
        throw new IllegalArgumentException("Unknown transfer");
    }
}
