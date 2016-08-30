package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;

public interface Encryption {

    /**
     * Get list of key names available for use for server side encryption
     *
     *
     * @param file Container
     * @param prompt Login callback
     * @return List of key names
     */
    Set<Algorithm> getKeys(final Path file, LoginCallback prompt) throws BackgroundException;

    /**
     * Enable server side encryption for file
     *
     * @param file      File
     * @param algorithm Algorithm to use
     */
    void setEncryption(Path file, Algorithm algorithm) throws BackgroundException;

    /**
     * @param file Default encryption setting for file
     * @return Default server side algorithm to use or null if SSE is disabled
     */
    Algorithm getDefault(final Path file);

    /**
     * Get server side encryption algorithm
     *
     * @param file File
     * @return Null if not encrypted or server side encryption algorithm used
     */
    Algorithm getEncryption(Path file) throws BackgroundException;

    class Algorithm {
        public static final Algorithm NONE = new Algorithm(null, null) {
            @Override
            public String getDescription() {
                return LocaleFactory.localizedString("None");
            }

            @Override
            public String toString() {
                return "none";
            }
        };

        public Algorithm(final String algorithm, final String key) {
            this.algorithm = algorithm;
            this.key = key;
        }

        public final String algorithm;
        public final String key;

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof Algorithm)) {
                return false;
            }
            final Algorithm that = (Algorithm) o;
            return Objects.equals(algorithm, that.algorithm) &&
                    Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(algorithm, key);
        }

        public static Algorithm fromString(final String value) {
            if(StringUtils.equals(NONE.toString(), value)) {
                return NONE;
            }
            if(StringUtils.contains(value, '|')) {
                return new Algorithm(StringUtils.split(value, '|')[0], StringUtils.split(value, '|')[1]);
            }
            return new Algorithm(value, null);
        }

        @Override
        public String toString() {
            if(null == key) {
                return algorithm;
            }
            return String.format("%s|%s", algorithm, key);
        }

        public String getDescription() {
            if(null == key) {
                return algorithm;
            }
            return key;
        }
    }
}
