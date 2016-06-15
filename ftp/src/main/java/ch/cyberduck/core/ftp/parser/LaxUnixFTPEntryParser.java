package ch.cyberduck.core.ftp.parser;

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

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;

public class LaxUnixFTPEntryParser extends CommonUnixFTPEntryParser {

    private static final String REGEX_WHITESPACE_AWARE =
            "([bcdlfmpSs-])"
                    + "(((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-])))\\+?\\s+"
                    /**
                     * hard link count
                     */
                    + "(\\d+)\\s+"
                    /**
                     * user
                     */
                    + "(\\S+)\\s+"
                    /**
                     * group, maybe missing
                     */
                    + "(?:(\\S+)\\s+)?"
                    /**
                     * file size
                     */
                    + "(\\d+)"
                    /**
                     * file size maybe given in human readable format eg 15.6k
                     */
                    + "(\\.?\\d?)(\\w?)\\s+"
                    /**
                     numeric or standard format date
                     */
                    + "((?:\\d+[-/]\\d+[-/]\\d+)|(?:\\S+\\s+\\S+))\\s+"
                    /**
                     year (for non-recent standard format) or time (for numeric or recent standard format
                     */
                    + "((?:\\d{4}\\s?)|(?:\\d{1,2}:\\d{2}))\\s"
                    /**
                     * Filename and special token like symbolic link target
                     */
                    + "(\\s*\\S+)(\\s*.*)";

    public LaxUnixFTPEntryParser() {
        super(REGEX_WHITESPACE_AWARE);
    }

    @Override
    public FTPFile parseFTPEntry(String entry) {
        if(matches(entry)) {
            String typeStr = group(1);
            String usr = group(16);
            String grp = group(17);
            String filesize = group(18) + group(19);
            String filesizeIndicator = group(20);
            String datestr = group(21) + " " + group(22).trim();
            String name = group(23);
            String endtoken = group(24);
            if(StringUtils.isNotBlank(filesizeIndicator)) {
                // See #1076
                try {
                    double size = Double.parseDouble(filesize);
                    if(filesizeIndicator.equalsIgnoreCase("K")) {
                        size = size * TransferStatus.KILO;
                    }
                    else if(filesizeIndicator.equalsIgnoreCase("M")) {
                        size = size * TransferStatus.MEGA;
                    }
                    else if(filesizeIndicator.equalsIgnoreCase("G")) {
                        size = size * TransferStatus.GIGA;
                    }
                    return this.parseFTPEntry(typeStr, usr, grp, (long) size, datestr, name, endtoken);
                }
                catch(NumberFormatException e) {
                    return this.parseFTPEntry(typeStr, usr, grp, -1, datestr, name, endtoken);
                }
            }
            return this.parseFTPEntry(typeStr, usr, grp, filesize, datestr, name, endtoken);
        }
        return null;
    }
}
