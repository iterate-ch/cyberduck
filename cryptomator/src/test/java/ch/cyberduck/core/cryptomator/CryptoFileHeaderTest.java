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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;

/**
 * Invalid file headers are reported as unauthentic content and not as runtime failure
 */
@RunWith(value = Parameterized.class)
public class CryptoFileHeaderTest extends AbstractCryptoTests {

    @Test(expected = CryptoAuthenticationException.class)
    public void testTruncatedHeader() throws Exception {
        final AbstractVault vault = this.unlockedVault();
        try {
            vault.openFile(ByteBuffer.allocate(10));
        }
        finally {
            vault.close();
        }
    }

    @Test(expected = CryptoAuthenticationException.class)
    public void testEmptyHeader() throws Exception {
        final AbstractVault vault = this.unlockedVault();
        try {
            // Zero bytes as read from a file shorter than the header
            vault.openFile(ByteBuffer.allocate(vault.getHeaderSize()));
        }
        finally {
            vault.close();
        }
    }

    @Test(expected = CryptoAuthenticationException.class)
    public void testHeaderOfOtherVault() throws Exception {
        final AbstractVault other = this.unlockedVault();
        final ByteBuffer header = other.createHeader();
        other.close();
        final AbstractVault vault = this.unlockedVault();
        try {
            // Unknown seed for Universal Vault Format, other key for vault format 8
            vault.openFile(header);
        }
        finally {
            vault.close();
        }
    }
}
