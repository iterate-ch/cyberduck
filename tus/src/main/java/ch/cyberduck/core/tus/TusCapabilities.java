package ch.cyberduck.core.tus;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.io.HashAlgorithm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TusCapabilities {
    public static final TusCapabilities none = new TusCapabilities();

    public static final String TUS_HEADER_EXTENSION = "Tus-Extension";
    public static final String TUS_HEADER_VERSION = "Tus-Version";
    public static final String TUS_HEADER_RESUMABLE = "Tus-Resumable";
    public static final String TUS_HEADER_UPLOAD_OFFSET = "Upload-Offset";
    public static final String TUS_HEADER_UPLOAD_METADATA = "Upload-Metadata";
    public static final String TUS_HEADER_UPLOAD_LENGTH = "Upload-Length";
    public static final String TUS_HEADER_UPLOAD_DEFER_LENGTH = "Upload-Defer-Length";
    public static final String TUS_HEADER_UPLOAD_CHECKSUM = "Upload-Checksum";
    public static final String TUS_HEADER_CHECKSUM_ALGORITHM = "Tus-Checksum-Algorithm";

    public static final String TUS_VERSION = "1.0.0";

    /**
     * Comma-separated list of protocol versions supported
     */
    public String[] versions;
    /**
     * checksum algorithm supported by the server
     */
    public HashAlgorithm hashAlgorithm;

    public Set<Extension> extensions = new HashSet<>();

    public enum Extension {
        /**
         * Extension to verify data integrity of each PATCH request
         */
        checksum,
        /**
         * Upload creation extension
         */
        creation,
        creationwithupload,
        /**
         * Remove unfinished uploads once they expire
         */
        expiration,
    }

    public TusCapabilities withVersions(final String[] versions) {
        this.versions = versions;
        return this;
    }

    public TusCapabilities withHashAlgorithm(final HashAlgorithm hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
        return this;
    }

    public TusCapabilities withExtension(final Extension extension) {
        this.extensions.add(extension);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TusCapabilities{");
        sb.append("versions=").append(Arrays.toString(versions));
        sb.append(", hashAlgorithm=").append(hashAlgorithm);
        sb.append(", extensions=").append(extensions);
        sb.append('}');
        return sb.toString();
    }
}
