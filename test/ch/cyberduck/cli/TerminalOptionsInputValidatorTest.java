package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TerminalOptionsInputValidatorTest extends AbstractTestCase {

    @Test
    public void testValidate() throws Exception {
        final String uri = "rackspace://cdn.duck.sh/%%~nc";
        assertFalse(new TerminalOptionsInputValidator().validate(uri));
    }

    @Test
    public void testColonInPath() throws Exception {
        final String uri = "rackspace://cdn.duck.sh/duck-4.6.2.16174:16179M.pkg";
        assertTrue(new TerminalOptionsInputValidator().validate(uri));
    }

    @Test
    public void testListContainers() throws Exception {
        assertTrue(new TerminalOptionsInputValidator().validate("rackspace:///"));
        assertFalse(new TerminalOptionsInputValidator().validate("rackspace://"));
    }
}