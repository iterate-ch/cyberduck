package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginOptions;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AzureProtocolTest {

    @Test
    public void testValidate() {
        assertFalse(new AzureProtocol().validate(new Credentials("u"), new LoginOptions().password(true)));
        assertFalse(new AzureProtocol().validate(new Credentials("u", "abc"), new LoginOptions().password(true)));
        assertTrue(new AzureProtocol().validate(new Credentials("u", Base64.encodeBase64String("abc".getBytes())), new LoginOptions().password(true)));
    }
}
