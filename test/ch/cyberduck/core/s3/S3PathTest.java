package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class S3PathTest extends AbstractTestCase {

    @Test
    public void testToURL() throws Exception {
        S3Path p = new S3Path(new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname())), "/bucket/f/key", Path.FILE_TYPE) {
            @Override
            public String getContainerName() {
                return "bucket";
            }
        };
        assertEquals("https://bucket.s3.amazonaws.com/f/key", p.toURL());
    }

    @Test
    public void testToHttpURL() throws Exception {
        S3Path p = new S3Path(new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname())), "/bucket/f/key", Path.FILE_TYPE) {
            @Override
            public String getContainerName() {
                return "bucket";
            }
        };
        assertEquals("http://bucket.s3.amazonaws.com/f/key", p.toHttpURL());
    }
}
