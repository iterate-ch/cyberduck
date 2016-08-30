package ch.cyberduck.core.ftp.parser;

import org.apache.commons.net.ftp.FTPFile;
/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

/**
 * Parser expecting no hard link count in the directory listing
 */
public class TrellixFTPEntryParser extends CommonUnixFTPEntryParser {

    private static final String REGEX =
            "([bcdlfmpSs-])"
                    + "(((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-])))\\+?\\s+"
                    + "(\\S+)\\s+"
                    + "(?:(\\S+(?:\\s\\S+)*)\\s+)?"
                    + "(\\d+)\\s+"
        /*
          numeric or standard format date
        */
                    + "((?:\\d+[-/]\\d+[-/]\\d+)|(?:\\S+\\s+\\S+))\\s+"

        /*
           year (for non-recent standard format)
		   or time (for numeric or recent standard format
		*/
                    + "(\\d+(?::\\d+)?)\\s+"
                    + "(\\S*)(\\s*.*)";


    public TrellixFTPEntryParser() {
        super(REGEX);
    }

    @Override
    public FTPFile parseFTPEntry(String entry) {
        if(matches(entry)) {
            String typeStr = group(1);
            String usr = group(15);
            String grp = group(16);
            String filesize = group(17);
            String datestr = group(18) + " " + group(19);
            String name = group(20);
            String endtoken = group(21);
            return super.parseFTPEntry(typeStr, usr, grp, filesize, datestr, name, endtoken);
        }
        return null;
    }
}
