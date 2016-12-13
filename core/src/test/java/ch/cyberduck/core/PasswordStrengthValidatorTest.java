package ch.cyberduck.core;

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

import static org.junit.Assert.assertEquals;

public class PasswordStrengthValidatorTest {

    @Test
    public void testGetScore() throws Exception {
        assertEquals(PasswordStrengthValidator.Strength.veryweak, new PasswordStrengthValidator().getScore(""));
        assertEquals(PasswordStrengthValidator.Strength.veryweak, new PasswordStrengthValidator().getScore("Cyberduck"));
        assertEquals(PasswordStrengthValidator.Strength.verystrong, new PasswordStrengthValidator().getScore("ahvae7faY3ae"));
    }
}