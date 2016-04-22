package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LaxHostnameDelegatingTrustManagerTest {

    @Test
    public void testSetTarget() throws Exception {
        assertEquals("s3.amazonaws.com",
                new LaxHostnameDelegatingTrustManager(new DisabledX509TrustManager(), "s3.amazonaws.com").getTarget());
        assertEquals("cyberduck.s3.amazonaws.com",
                new LaxHostnameDelegatingTrustManager(new DisabledX509TrustManager(), "cyberduck.s3.amazonaws.com").getTarget());
        assertEquals("duck.s3.amazonaws.com",
                new LaxHostnameDelegatingTrustManager(new DisabledX509TrustManager(), "cyber.duck.s3.amazonaws.com").getTarget());
    }
}