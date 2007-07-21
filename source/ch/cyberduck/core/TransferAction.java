package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSBundle;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
*/
public abstract class TransferAction {

    public TransferAction() {
        actions.put(this.toString(), this);
    }

    protected static Map actions = new HashMap();

    public abstract String toString();

    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        return this.toString().equals(other.toString());
    }

    public abstract String getLocalizableString();

    public static TransferAction forName(String name) {
        return (TransferAction)actions.get(name);
    }

    /**
     * Overwrite any prior existing file
     */
    public static final TransferAction ACTION_OVERWRITE = new TransferAction() {
        public String toString() {
            return "overwrite";
        }

        public String getLocalizableString() {
            return NSBundle.localizedString("Overwrite", "");
        }
    };

    /**
     * Append to any exsisting file when writing
     */
    public static final TransferAction ACTION_RESUME = new TransferAction() {
        public String toString() {
            return "resume";
        }

        public String getLocalizableString() {
            return NSBundle.localizedString("Resume", "");
        }
    };

    /**
     * Create a new file with a similar name
     *
     * @see DownloadTransfer#adjustFilename(ch.cyberduck.core.Path)
     * @see UploadTransfer#adjustFilename(ch.cyberduck.core.Path)
     */
    public static final TransferAction ACTION_RENAME = new TransferAction() {
        public String toString() {
            return "similar";
        }

        public String getLocalizableString() {
            return NSBundle.localizedString("Rename", "");
        }
    };

    /**
     * Do not transfer file
     */
    public static final TransferAction ACTION_SKIP = new TransferAction() {
        public String toString() {
            return "skip";
        }

        public String getLocalizableString() {
            return NSBundle.localizedString("Skip", "");
        }
    };

    /**
     * Prompt the user about existing files
     */
    public static final TransferAction ACTION_CALLBACK = new TransferAction() {
        public String toString() {
            return "ask";
        }

        public String getLocalizableString() {
            return NSBundle.localizedString("Prompt", "");
        }
    };

    public static final TransferAction ACTION_CANCEL = new TransferAction() {
        public String toString() {
            return "cancel";
        }

        public String getLocalizableString() {
            return NSBundle.localizedString("Cancel", "");
        }
    };
}