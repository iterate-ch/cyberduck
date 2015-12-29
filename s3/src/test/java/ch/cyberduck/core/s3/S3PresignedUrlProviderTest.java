package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.utils.SignatureUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
@Category(IntegrationTest.class)
public class S3PresignedUrlProviderTest {

    @Test
    public void testCreate() throws Exception {
        final URI uri = SignatureUtils.awsV4CorrectHostnameForRegion(new URI("https://s3.amazonaws.com/bucket/?max-keys=1000&prefix=%26%2F&delimiter=%2F"), "eu-central-1");
        assertNotNull(uri);
        assertEquals("max-keys=1000&prefix=&/&delimiter=/", uri.getQuery());
        assertEquals("max-keys=1000&prefix=%26%2F&delimiter=%2F", uri.getRawQuery());
        assertEquals("https://s3-eu-central-1.amazonaws.com/bucket/?max-keys=1000&prefix=%26%2F&delimiter=%2F", uri.toString());
    }
}