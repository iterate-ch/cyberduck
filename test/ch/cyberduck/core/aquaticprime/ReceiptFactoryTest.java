package ch.cyberduck.core.aquaticprime;

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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.test.Depends;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Depends(platform = Factory.Platform.Name.mac)
public class ReceiptFactoryTest extends AbstractTestCase {

    @Test
    @Ignore
    public void testCreate() throws Exception {
        assertEquals(new Receipt(null, "c42c030b8670"), new ReceiptFactory().create());
    }

    @Test
    @Ignore
    public void testOpen() throws Exception {
        assertEquals(LicenseFactory.EMPTY_LICENSE, new ReceiptFactory().open());
    }

    @Test
    public void testOpenDirectory() throws Exception {
        assertEquals(1, new ReceiptFactory(new Local("/Applications/Cyberduck.app/Contents/_MASReceipt")).open().size());
    }
}
