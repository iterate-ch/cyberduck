package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RequestEntityRestStorageServiceTest {

    @Test
    public void testGetBucket() {
        assertEquals("bucketname", RequestEntityRestStorageService.findBucketInHostname(new Host(new S3Protocol(), "bucketname.s3.amazonaws.com")));
        assertNull(RequestEntityRestStorageService.findBucketInHostname(new Host(new TestProtocol(), "bucketname.s3.amazonaws.com")));
    }

}