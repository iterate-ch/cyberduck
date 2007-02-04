package org.apache.commons.net.ftp.parser;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

import java.util.Calendar;

/**
 * @author <a href="mailto:dkocher@cyberduck.ch">David Kocher</a>
 * @version $Id$
 */
public class NetwareFTPEntryParser extends RegexFTPFileEntryParserImpl {

    /**
     * months abbreviations looked for by this parser.  Also used
     * to determine which month is matched by the parser
     */
    private static final String MONTHS =
            "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";

    /**
     * - [RWCEAFMS]          0                       1593 Feb 18 16:36 00README
     * - [RWCEAFMS]          0                       1445 Apr 28  1994 ANON.DAT
     * - [RWCEAFMS] anonymous                           0 Jan 11 16:11 DSNS_B~1
     * - [RWCEAFMS] Labspace                           91 Oct 13 07:42 LABSPACE.SUM
     * - [RWCEAFMS]          0                      27945 Sep 05  1994 WHATIS.MSC
     * d [RWCEAFMS]          0                        512 Jan 22 20:44 MAINT
     * d [RWCEAFMS]          0                        512 Dec 29  2003 MISC
     */
    private static final String REGEX =
            "([-d])\\s+" //file type
                    + "(\\S+)\\s+"//"(\\\[(R|-)(W|-)(C|-)(E|-)(A|-)(F|-)(M|-)(S|-)\\\])\\s+"
                    + "(\\d+|\\S+)\\s+" //owner
                    + "(\\d+)\\s+" //size
                    + MONTHS + "\\s+" //month
                    + "(\\d{2})\\s+" //day
                    + "((([0-2][0-9]):([0-5][0-9]))|(\\d{4}))\\s+" // hour:minutes | year
                    + "(\\S.*)";


    public NetwareFTPEntryParser() {
        super(REGEX);
    }

    /**
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public Path parseFTPEntry(Path parent, String entry) {
        Path f = PathFactory.createPath(parent.getSession());
        if(matches(entry)) {
            String typeStr = group(1);
            String user = group(3);
            String filesize = group(4);
            String mo = group(5);
            String da = group(6);
            String hr = group(9);
            String min = group(10);
            String yr = group(11);
            String name = group(12);
            if(null == name || name.equals("") || name.equals(".") || name.equals("..")) {
                return null;
            }
            int type;
            switch(typeStr.charAt(0)) {
                case'd':
                    type = Path.DIRECTORY_TYPE;
                    break;
                default:
                    type = Path.FILE_TYPE;
            }

            f.attributes.setType(type);
            f.attributes.setOwner(user);

            try {
                f.attributes.setSize(Double.parseDouble(filesize));
            }
            catch(NumberFormatException e) {
                // intentionally do nothing
            }

            Calendar cal = Calendar.getInstance();

            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            try {
                int pos = MONTHS.indexOf(mo);
                int month = pos / 4;

                if(null != yr) {
                    // it's a year
                    cal.set(Calendar.YEAR, Integer.parseInt(yr));
                }
                else {
                    // it must be  hour/minute or we wouldn't have matched
                    int year = cal.get(Calendar.YEAR);
                    // if the month we're reading is greater than now, it must
                    // be last year
                    if(cal.get(Calendar.MONTH) < month) {
                        year--;
                    }
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hr));
                    cal.set(Calendar.MINUTE, Integer.parseInt(min));
                }
                cal.set(Calendar.MONTH, month);

                cal.set(Calendar.DATE, Integer.parseInt(da));
                f.attributes.setModificationDate(cal.getTime().getTime());
            }
            catch(NumberFormatException e) {
                // do nothing, date will be uninitialized
            }
            f.setPath(parent.getAbsolute(), name);
            return f;
        }
        return null;
    }
}
