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

import ch.cyberduck.core.i18n.Locale;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
*/
public abstract class TransferAction {

    public TransferAction() {
        actions.put(this.toString(), this);
    }

    protected static Map<String, TransferAction> actions = new HashMap<String, TransferAction>();

    public abstract String toString();

    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        return this.toString().equals(other.toString());
    }

    public abstract String getLocalizableString();

    public static TransferAction forName(String name) {
        return actions.get(name);
    }

    /**
     * Overwrite any prior existing file
     */
    public static final TransferAction ACTION_OVERWRITE = new TransferAction() {
        @Override
        public String toString() {
            return "overwrite";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Overwrite");
        }
    };

    /**
     * Append to any exsisting file when writing
     */
    public static final TransferAction ACTION_RESUME = new TransferAction() {
        @Override
        public String toString() {
            return "resume";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Resume");
        }
    };

    /**
     * Create a new file with a similar name
     */
    public static final TransferAction ACTION_RENAME = new TransferAction() {
        @Override
        public String toString() {
            return "similar";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Rename");
        }
    };

    /**
     * Create a new file with a similar name
     */
    public static final TransferAction ACTION_RENAME_EXISTING = new TransferAction() {
        @Override
        public String toString() {
            return "rename";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Rename existing");
        }
    };

    /**
     * Do not transfer file
     */
    public static final TransferAction ACTION_SKIP = new TransferAction() {
        @Override
        public String toString() {
            return "skip";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Skip");
        }
    };

    /**
     * Prompt the user about existing files
     */
    public static final TransferAction ACTION_CALLBACK = new TransferAction() {
        @Override
        public String toString() {
            return "ask";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Prompt");
        }
    };

    public static final TransferAction ACTION_CANCEL = new TransferAction() {
        @Override
        public String toString() {
            return "cancel";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Cancel");
        }
    };
}