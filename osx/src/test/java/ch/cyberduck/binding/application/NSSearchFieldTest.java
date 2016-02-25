package ch.cyberduck.binding.application;

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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NSSearchFieldTest {

    @Test
    public void testSendsSearchStringImmediately() throws Exception {
        final NSSearchField f = NSSearchField.searchField();
        f.setSendsSearchStringImmediately(true);
        assertTrue(f.sendsSearchStringImmediately());
        f.setSendsSearchStringImmediately(false);
        assertFalse(f.sendsSearchStringImmediately());

    }

    @Test
    public void testSendsWholeSearchString() throws Exception {
        final NSSearchField f = NSSearchField.searchField();
        f.setSendsWholeSearchString(true);
        assertTrue(f.sendsWholeSearchString());
        f.setSendsWholeSearchString(false);
        assertFalse(f.sendsWholeSearchString());
    }
}