package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.foundation.NSDictionary;

/**
 * @version $Id$
 */
public abstract class TransferFactory {

    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;
    public static final int KIND_SYNC = 2;

    public static Transfer create(NSDictionary dict) {
        Object kindObj = dict.objectForKey("Kind");
        if (kindObj != null) {
            int kind = Integer.parseInt((String) kindObj);
            switch (kind) {
                case KIND_DOWNLOAD:
                    return new DownloadTransfer(dict);
                case KIND_UPLOAD:
                    return new UploadTransfer(dict);
                case KIND_SYNC:
                    return new SyncTransfer(dict);
            }
        }
        throw new IllegalArgumentException("Unknown queue");
    }
}
