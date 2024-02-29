package ch.cyberduck.core.ftp.parser;

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

import ch.cyberduck.core.ftp.FTPParserSelector;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class AXSPortFTPEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void configure() {
        this.parser = new FTPParserSelector().getParser("CMX TCP/IP - REMOTE FTP server (version 2.0) ready.");
    }

    /**
     * #9192
     */
    @Test
    public void testParseFile() {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
            "---------   1 owner    group         1845484 Dec 22 10:20 OPTICS.RPY");
        assertNull(parsed);

    }

    @Test
    public void testParseDirectory() {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
            "d--------   1 owner    group               0 Dec 22 10:19 DATALOGS");
        assertNull(parsed);
    }
}
