package ch.cyberduck.core.transfer;

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

    private static final Map<String, TransferAction> registry
            = new HashMap<String, TransferAction>();

    public static TransferAction forName(String name) {
        return registry.get(name);
    }

    private String name;

    public TransferAction(final String name) {
        registry.put(name, this);
        this.name = name;
    }

    public abstract String getLocalizableString();

    public String name() {
        return name;
    }

    /**
     * Overwrite any prior existing file
     */
    public static final TransferAction ACTION_OVERWRITE = new TransferAction("overwrite") {
        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Overwrite");
        }
    };

    /**
     * Append to any exsisting file when writing
     */
    public static final TransferAction ACTION_RESUME = new TransferAction("resume") {
        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Resume");
        }
    };

    /**
     * Create a new file with a similar name
     */
    public static final TransferAction ACTION_RENAME = new TransferAction("similar") {
        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Rename");
        }
    };

    /**
     * Create a new file with a similar name
     */
    public static final TransferAction ACTION_RENAME_EXISTING = new TransferAction("rename") {
        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Rename existing");
        }
    };

    /**
     * Do not transfer file
     */
    public static final TransferAction ACTION_SKIP = new TransferAction("skip") {
        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Skip");
        }
    };

    /**
     * Prompt the user about existing files
     */
    public static final TransferAction ACTION_CALLBACK = new TransferAction("ask") {
        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Prompt");
        }
    };

    /**
     * Automatically decide the transfer action using the comparision service for paths.
     */
    public static final TransferAction ACTION_COMPARISON = new TransferAction("compare") {
        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Compare");
        }
    };

    public static final TransferAction ACTION_CANCEL = new TransferAction("cancel") {
        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Cancel");
        }
    };
}