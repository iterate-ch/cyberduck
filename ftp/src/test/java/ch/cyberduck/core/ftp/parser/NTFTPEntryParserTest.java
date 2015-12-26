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
package ch.cyberduck.core.ftp.parser;

import ch.cyberduck.core.ftp.FTPParserSelector;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static org.junit.Assert.*;

public class NTFTPEntryParserTest {

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
                    "07-10-07  07:32PM                69610 Algemene Leveringsvoorwaarden *******.pdf",
                    "07-11-07  12:52AM       <DIR>          aspnet_client",
                    "07-10-07  07:30PM       <DIR>          auth",
                    "07-03-07  01:55PM       <DIR>          cgi-bin",
                    "07-10-07  07:32PM                  428 global.asa",
                    "07-03-07  01:55PM       <DIR>          icon",
                    "07-10-07  07:29PM       <DIR>          img",
                    "07-10-07  07:32PM       <DIR>          include",
                    "07-10-07  07:32PM                 3384 index.html",
                    "07-10-07  07:32PM       <DIR>          js",
                    "07-10-07  07:37PM       <DIR>          kandidaten",
                    "07-10-07  07:32PM       <DIR>          lib",
                    "07-10-07  07:37PM       <DIR>          opdrachtgevers",
                    "07-10-07  07:32PM                 1309 stijl1.css",
                    "07-10-07  07:32PM       <DIR>          style",
                    "07-15-07  02:40PM       <DIR>          temp",
                    "07-10-07  07:32PM       <DIR>          vacatures"
            };

    private FTPFileEntryParser parser;
    private SimpleDateFormat df;

    @Before
    public void configure() {
        this.parser = new FTPParserSelector().getParser("WINDOWS");
        this.df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
    }

    @Test
    public void testParse() throws Exception {
        for(String sample : samples) {
            assertNotNull(sample, parser.parseFTPEntry(sample));
        }
    }

    @Test
    public void testParseFieldsOnDirectory() throws Exception {
        FTPFile parsed = parser.parseFTPEntry("12-05-96  05:03PM       <DIR>          absoft2");
        assertNotNull("Could not parse entry.", parsed);
        assertEquals("Thu Dec 05 17:03:00 1996",
                df.format(parsed.getTimestamp().getTime()));
        assertTrue(parsed.isDirectory());
        assertEquals("absoft2", parsed.getName());

        parsed = parser.parseFTPEntry(
                "12-03-96  06:38AM       <DIR>          123456");
        assertNotNull("Could not parse entry.", parsed);
        assertTrue(parsed.isDirectory());
        assertEquals("123456", parsed.getName());
    }

    @Test
    public void testParseFieldsOnFile() throws Exception {
        FTPFile parsed = parser.parseFTPEntry(
                "05-22-97  12:08AM                  5000000000 AUTOEXEC.BAK");
        assertNotNull("Could not parse entry.", parsed);
        assertEquals("Thu May 22 00:08:00 1997",
                df.format(parsed.getTimestamp().getTime()));
        assertTrue(parsed.isFile());
        assertEquals("AUTOEXEC.BAK", parsed.getName());
        assertEquals(5000000000l, parsed.getSize());
    }

    @Test
    public void testDirectoryBeginningWithNumber() throws Exception {
        FTPFile parsed = parser.parseFTPEntry("12-03-96  06:38AM       <DIR>          123xyz");
        assertNotNull(parsed);
        assertEquals("name", "123xyz", parsed.getName());
    }

    @Test
    public void testDirectoryBeginningWithNumberFollowedBySpaces() throws Exception {
        FTPFile parsed = parser.parseFTPEntry(
                "12-03-96  06:38AM       <DIR>          123 xyz");
        assertNotNull(parsed);
        assertEquals("123 xyz", parsed.getName());
        parsed = parser.parseFTPEntry(
                "12-03-96  06:38AM       <DIR>          123 abc xyz");
        assertNotNull(parsed);
        assertEquals("123 abc xyz", parsed.getName());
    }

    @Test
    public void testElectic() throws Exception {
        FTPFile parsed = parser.parseFTPEntry(
                "09-04-06  11:28AM                  149 gearkommandon with spaces.txt");
        assertNotNull(parsed);
        assertEquals("gearkommandon with spaces.txt", parsed.getName());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals(Calendar.SEPTEMBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(4, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(2006, parsed.getTimestamp().get(Calendar.YEAR));
    }
}
