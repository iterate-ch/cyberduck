package ch.cyberduck.ui.browser;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;

import org.apache.commons.lang3.StringUtils;

public enum BrowserColumn {
    icon {
        @Override
        public String toString() {
            return StringUtils.EMPTY;
        }
    },
    filename {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Filename");
        }
    },
    size {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Size");
        }
    },
    modified {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Modified");
        }
    },
    owner {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Owner");
        }
    },
    group {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Group");
        }
    },
    permission {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Permissions");
        }
    },
    kind {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Kind");
        }
    },
    extension {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Extension");
        }
    },
    region {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Region");
        }
    },
    version {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Version");
        }
    },
    storageclass {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Storage Class", "Info");
        }
    },
    checksum {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Checksum", "Info");
        }
    }
}
