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

import ch.cyberduck.core.TransferReaderFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

/**
 * @version $Id$
 */
public class TransferPlistReader extends PlistReader<Transfer> {

    public static void register() {
        TransferReaderFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends TransferReaderFactory {
        @Override
        public Reader<Transfer> create() {
            return new TransferPlistReader();
        }
    }

    @Override
    public Transfer deserialize(final NSDictionary dict) {
        NSObject kindObj = dict.objectForKey("Kind");
        if(kindObj != null) {
            switch(Transfer.Type.values()[Integer.parseInt(kindObj.toString())]) {
                case download:
                    return new DownloadTransfer(dict);
                case upload:
                    return new UploadTransfer(dict);
                case sync:
                    return new SyncTransfer(dict);
                case copy:
                    return new CopyTransfer(dict);
            }
        }
        throw new IllegalArgumentException("Unknown transfer");
    }
}
