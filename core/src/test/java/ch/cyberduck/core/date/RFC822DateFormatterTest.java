package ch.cyberduck.core.date;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RFC822DateFormatterTest {

    @Test(expected = InvalidDateException.class)
    public void testNull() throws Exception {
        assertNull(new RFC822DateFormatter().parse(null));
    }

    @Test
    public void testParse() throws Exception {
        assertEquals(1599696000000L, new RFC822DateFormatter().parse("Thu, 10 Sep 2020 00:00:00 GMT").getTime(), 0L);
    }

    @Test
    public void testPrint() {
        assertEquals("Thu, 01 Dec 1994 17:00:00 GMT", new RFC822DateFormatter().format(786297600000L, TimeZone.getTimeZone("Europe/Zurich")));
        assertEquals("Thu, 01 Dec 1994 16:00:00 GMT", new RFC822DateFormatter().format(786297600000L, TimeZone.getTimeZone("UTC")));
    }

}
