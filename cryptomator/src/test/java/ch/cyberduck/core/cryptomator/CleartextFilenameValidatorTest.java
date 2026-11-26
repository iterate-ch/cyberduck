package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Factory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CleartextFilenameValidatorTest {

    @Test
    public void testInvalidOnAllPlatforms() {
        for(Factory.Platform.Name platform : Factory.Platform.Name.values()) {
            final CleartextFilenameValidator validator = new CleartextFilenameValidator(platform);
            assertFalse(validator.isValid(null));
            assertFalse(validator.isValid(""));
            assertFalse(validator.isValid("."));
            assertFalse(validator.isValid(".."));
            assertFalse(validator.isValid("a/b"));
            assertFalse(validator.isValid("../etc"));
            assertFalse(validator.isValid("a\0b"));
            assertTrue(validator.isValid("f"));
            assertTrue(validator.isValid(".hidden"));
            assertTrue(validator.isValid("..f"));
            assertTrue(validator.isValid("ä"));
        }
    }

    @Test
    public void testWindows() {
        final CleartextFilenameValidator validator = new CleartextFilenameValidator(Factory.Platform.Name.windows);
        assertFalse(validator.isValid("..\\..\\Startup\\evil.exe"));
        assertFalse(validator.isValid("a\\b"));
        assertFalse(validator.isValid("C:"));
        assertFalse(validator.isValid("a:b"));
        assertFalse(validator.isValid("a*b"));
        assertFalse(validator.isValid("a?b"));
        assertFalse(validator.isValid("a\"b"));
        assertFalse(validator.isValid("a<b"));
        assertFalse(validator.isValid("a>b"));
        assertFalse(validator.isValid("a|b"));
        assertFalse(validator.isValid("a\tb"));
        assertFalse(validator.isValid("f."));
        assertFalse(validator.isValid("f "));
        assertFalse(validator.isValid("CON"));
        assertFalse(validator.isValid("con"));
        assertFalse(validator.isValid("NUL.txt"));
        assertFalse(validator.isValid("COM1"));
        assertFalse(validator.isValid("LPT9.tar.gz"));
        assertTrue(validator.isValid("CONTACTS"));
        assertTrue(validator.isValid("COM10"));
        assertTrue(validator.isValid("f.txt"));
        assertTrue(validator.isValid(" f"));
    }

    @Test
    public void testPosix() {
        for(Factory.Platform.Name platform : new Factory.Platform.Name[]{Factory.Platform.Name.mac, Factory.Platform.Name.linux}) {
            final CleartextFilenameValidator validator = new CleartextFilenameValidator(platform);
            assertTrue(validator.isValid("a\\b"));
            assertTrue(validator.isValid("a:b"));
            assertTrue(validator.isValid("f."));
            assertTrue(validator.isValid("CON"));
        }
    }
}
