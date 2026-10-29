package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import org.apache.commons.lang3.StringUtils;

public enum HashAlgorithm {
    none,
    md5,
    sha1,
    sha256,
    sha512,
    crc32,
    cdash64,
    /**
     * MD5 of the concatenated MD5 digests of the parts followed by a hyphen and the number of parts as returned as ETag
     * for objects uploaded in multiple parts to Amazon S3
     */
    md5_multipart,
    /**
     * MD5 of the concatenated hexadecimal MD5 checksums of the segments as returned as ETag of the manifest of a large
     * object in OpenStack Swift
     */
    md5_segments,
    dropbox_content_hash;

    @Override
    public String toString() {
        return StringUtils.upperCase(this.name());
    }
}
