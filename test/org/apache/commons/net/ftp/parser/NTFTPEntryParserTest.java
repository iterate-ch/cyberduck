/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp.parser;

import java.util.Calendar;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Test;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.Host;

import org.apache.commons.net.ftp.FTPFileEntryParser;

public class NTFTPEntryParserTest extends TestCase
{

    private static final String[] samples =
            {
            "05-26-95  10:57AM               143712 $LDR$",
            "05-20-97  03:31PM                  681 .bash_history",
            "12-05-96  05:03PM       <DIR>          absoft2",
            "11-14-97  04:21PM                  953 AUDITOR3.INI",
            "05-22-97  08:08AM                  828 AUTOEXEC.BAK",
            "01-22-98  01:52PM                  795 AUTOEXEC.BAT",
            "05-13-97  01:46PM                  828 AUTOEXEC.DOS",
            "12-03-96  06:38AM                  403 AUTOTOOL.LOG",
            "12-03-96  06:38AM       <DIR>          123xyz",
            "01-20-97  03:48PM       <DIR>          bin",
            "05-26-1995  10:57AM               143712 $LDR$",
            };

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public NTFTPEntryParserTest (String name)
    {
        super(name);
    }

    private FTPFileEntryParser parser;
    private Path parent;
    private SimpleDateFormat df;

    public void setUp() throws Exception
    {
        this.parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser("WINDOWS");
        this.parent = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                "/");
        this.df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
    }

    public void testParse() throws Exception
    {
        for(int i = 0; i < samples.length; i++) {
            assertNotNull(samples[i], parser.parseFTPEntry(parent, samples[i]));
        }
    }

    public void testParseFieldsOnDirectory() throws Exception
    {
        Path dir = parser.parseFTPEntry(parent, "12-05-96  05:03PM       <DIR>          absoft2");
        assertNotNull("Could not parse entry.", dir);
        assertEquals("Thu Dec 05 17:03:00 1996",
                     df.format(new Date(dir.attributes.getTimestamp())));
        assertTrue("Should have been a directory.",
                   dir.attributes.isDirectory());
        assertEquals("absoft2", dir.getName());
        assertEquals(-1, (int)dir.attributes.getSize());

        dir = parser.parseFTPEntry(parent,
                "12-03-96  06:38AM       <DIR>          123456");
        assertNotNull("Could not parse entry.", dir);
        assertTrue("Should have been a directory.",
                dir.attributes.isDirectory());
        assertEquals("123456", dir.getName());
        assertEquals(-1, (int)dir.attributes.getSize());

    }

    public void testParseFieldsOnFile() throws Exception
    {
        Path f = parser.parseFTPEntry(parent,
                "05-22-97  12:08AM                  5000000000 AUTOEXEC.BAK");
        assertNotNull("Could not parse entry.", f);
        assertEquals("Thu May 22 00:08:00 1997",
                df.format(new Date(f.attributes.getTimestamp())));
        assertTrue("Should have been a file.",
                   f.attributes.isFile());
        assertEquals("AUTOEXEC.BAK", f.getName());
        assertEquals(5000000000l, (long)f.attributes.getSize());
    }

    public void testDirectoryBeginningWithNumber() throws Exception
    {
        Path f = parser.parseFTPEntry(parent, "12-03-96  06:38AM       <DIR>          123xyz");
        assertNotNull(f);
        assertEquals("name", "123xyz", f.getName());
    }

    public void testDirectoryBeginningWithNumberFollowedBySpaces() throws Exception
    {
        Path f = parser.parseFTPEntry(parent,
                "12-03-96  06:38AM       <DIR>          123 xyz");
        assertNotNull(f);
        assertEquals("name", "123 xyz", f.getName());
        f = parser.parseFTPEntry(parent,
                "12-03-96  06:38AM       <DIR>          123 abc xyz");
        assertNotNull(f);
        assertEquals("name", "123 abc xyz", f.getName());
    }

    public static Test suite()
    {
        return new TestSuite(NTFTPEntryParserTest.class);
    }
}
