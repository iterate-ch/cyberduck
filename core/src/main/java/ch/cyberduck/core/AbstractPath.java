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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public abstract class AbstractPath {

    /**
     * Shortcut for the home directory
     */
    public static final String HOME = "~";

    public abstract EnumSet<Type> getType();

    public enum Type {
        file {
            @Override
            public int legacy() {
                return 1;
            }
        },
        directory {
            @Override
            public int legacy() {
                return 2;
            }
        },
        symboliclink {
            @Override
            public int legacy() {
                return 4;
            }
        },
        shared {
            @Override
            public int legacy() {
                return 0;
            }
        },
        volume {
            @Override
            public int legacy() {
                return 8;
            }
        },
        /**
         * Marker file for directory that should be treated for display like a regular folder
         */
        placeholder {
            @Override
            public int legacy() {
                return 0;
            }
        },
        /**
         * In-progress multipart upload
         */
        upload {
            @Override
            public int legacy() {
                return 0;
            }
        },
        /**
         * File from encrypted data room
         */
        @Deprecated
        triplecrypt {
            @Override
            public int legacy() {
                return 0;
            }
        },
        /**
         * Encrypted file in Cryptomator Vault
         */
        encrypted {
            @Override
            public int legacy() {
                return 0;
            }
        },
        /**
         * Decrypted file in Cryptomator Vault
         */
        decrypted {
            @Override
            public int legacy() {
                return 0;
            }
        },
        /**
         * Cryptomator Vault. File is internal part of a secure vault
         */
        vault {
            @Override
            public int legacy() {
                return 0;
            }
        };

        public abstract int legacy();
    }

    public abstract char getDelimiter();

    /**
     * @return true if this paths points to '/'
     * @see #getDelimiter()
     */
    public boolean isRoot() {
        return this.getAbsolute().equals(String.valueOf(this.getDelimiter()));
    }

    public abstract String getAbsolute();

    public abstract String getName();

    /**
     * @return the extension if any or null otherwise
     */
    public String getExtension() {
        return getExtension(this.getName());
    }

    public static String getExtension(final String filename) {
        final int extensionPos = indexOfExtension(filename);
        if(-1 == extensionPos) {
            return StringUtils.EMPTY;
        }
        return filename.substring(extensionPos + 1);
    }

    public static int indexOfExtension(final String filename) {
        if(StringUtils.isBlank(filename)) {
            return -1;
        }
        final int extensionPos = filename.lastIndexOf('.');
        if(-1 == extensionPos) {
            return -1;
        }
        final int lastSeparator = FilenameUtils.indexOfLastSeparator(filename);
        if(lastSeparator > extensionPos) {
            return -1;
        }
        return extensionPos;
    }
}
