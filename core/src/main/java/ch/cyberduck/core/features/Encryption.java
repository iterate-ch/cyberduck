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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.List;
import java.util.Objects;

public interface Encryption {

    /**
     * Get list of key names available for use for server side encryption
     *
     * @param prompt Login callback
     * @return List of key names
     */
    List<String> getKeys(LoginCallback prompt) throws BackgroundException;

    /**
     * @return List of supported algorithms by provider
     */
    List<Encryption.Properties> getAlgorithms();

    /**
     * Enable server side encryption for file
     *
     * @param file      File
     * @param algorithm Algorithm to use
     */
    void setEncryption(Path file, Properties algorithm) throws BackgroundException;

    /**
     * @param file Default encryption setting for file
     * @return Default server side algorithm to use or null if SSE is disabled
     */
    Properties getDefault(final Path file);

    /**
     * Get server side encryption algorithm
     *
     * @param file File
     * @return Null if not encrypted or server side encryption algorithm used
     * @throws BackgroundException
     */
    Properties getEncryption(Path file) throws BackgroundException;

    final class Properties {
        public static final Properties NONE = new Properties(null, null);

        public Properties(final String algorithm, final String key) {
            this.algorithm = algorithm;
            this.key = key;
        }

        public String algorithm;
        public String key;

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof Properties)) {
                return false;
            }
            final Properties that = (Properties) o;
            return Objects.equals(algorithm, that.algorithm) &&
                    Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(algorithm, key);
        }
    }
}
