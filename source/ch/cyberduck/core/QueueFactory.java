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

import java.util.Map;
import java.util.HashMap;

/**
 * @version $Id$
 */
public abstract class QueueFactory {

    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;
    public static final int KIND_SYNC = 2;

    public static Queue create(NSDictionary dict) {
        Object kindObj = dict.objectForKey("Kind");
        if (kindObj != null) {
            int kind = Integer.parseInt((String) kindObj);
            switch (kind) {
                case KIND_DOWNLOAD:
                    return new DownloadQueue(dict);
                case KIND_UPLOAD:
                    return new UploadQueue(dict);
                case KIND_SYNC:
                    return new SyncQueue(dict);
            }
        }
        throw new IllegalArgumentException("Unknown queue");
    }
}
