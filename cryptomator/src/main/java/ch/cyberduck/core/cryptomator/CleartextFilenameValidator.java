package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Factory;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Validate clear text filenames of a vault against the local file system. Decrypted names are untrusted input as
 * another member of a shared vault can store arbitrary names, e.g. with path separators of the local file system.
 *
 * @see <a href="https://github.com/encryption-alliance/unified-vault-format/blob/develop/implementation-notes.md#decrypted-names">Decrypted Names</a>
 */
public class CleartextFilenameValidator {

    /**
     * Reserved characters and control characters on Windows
     */
    private static final Pattern WINDOWS_INVALID_CHARACTERS = Pattern.compile("[<>:\"/\\\\|?*\\x00-\\x1F]");
    /**
     * Reserved device names on Windows, also with an extension
     */
    private static final Pattern WINDOWS_RESERVED_NAMES = Pattern.compile("^(CON|PRN|AUX|NUL|COM[0-9¹²³]|LPT[0-9¹²³])(\\..*)?$",
            Pattern.CASE_INSENSITIVE);

    private final Factory.Platform.Name platform;

    public CleartextFilenameValidator() {
        this(Factory.Platform.getDefault());
    }

    public CleartextFilenameValidator(final Factory.Platform.Name platform) {
        this.platform = platform;
    }

    /**
     * @param filename Clear text filename
     * @return False if the filename is not a single valid path segment on the local file system
     */
    public boolean isValid(final String filename) {
        if(StringUtils.isEmpty(filename)) {
            return false;
        }
        if(".".equals(filename) || "..".equals(filename)) {
            return false;
        }
        if(StringUtils.containsAny(filename, '/', '\0')) {
            return false;
        }
        if(Factory.Platform.Name.windows == platform) {
            if(WINDOWS_INVALID_CHARACTERS.matcher(filename).find()) {
                return false;
            }
            // Trailing dots and spaces are removed by the Windows API
            if(StringUtils.endsWithAny(filename, ".", " ")) {
                return false;
            }
            return !WINDOWS_RESERVED_NAMES.matcher(filename).matches();
        }
        return true;
    }
}
