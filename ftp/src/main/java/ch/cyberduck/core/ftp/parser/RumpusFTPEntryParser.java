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

import org.apache.commons.net.ftp.FTPFile;

/**
 * @see org.apache.commons.net.ftp.parser.UnixFTPEntryParser
 */
public class RumpusFTPEntryParser extends CommonUnixFTPEntryParser {

    /**
     * this is the regular expression used by this parser.
     * <p/>
     * Permissions:
     * r   the file is readable
     * w   the file is writable
     * x   the file is executable
     * -   the indicated permission is not granted
     * L   mandatory locking occurs during access (the set-group-ID bit is
     * on and the group execution bit is off)
     * s   the set-user-ID or set-group-ID bit is on, and the corresponding
     * user or group execution bit is also on
     * S   undefined bit-state (the set-user-ID bit is on and the user
     * execution bit is off)
     * t   the 1000 (octal) bit, or sticky bit, is on [see chmod(1)], and
     * execution is on
     * T   the 1000 bit is turned on, and execution is off (undefined bit-
     * state)
     */
    private static final String REGEX =
            "([bcdlfmpSs-])"
                    + "(((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-])))\\+?\\s+"
                    + "(\\d+)?\\s+"
                    + "(\\d+)?\\s+"
                    + "(\\S+)\\s+"
                    + "(\\d+)\\s+"
                    /* numeric or standard format date*/
                    + "((?:\\d+[-/]\\d+[-/]\\d+)|(?:\\S+\\s+\\S+))\\s+"
                    /* year (for non-recent standard format) or time (for numeric or recent standard format*/
                    + "(\\d+(?::\\d+)?)\\s+"
                    + "(\\S*)(\\s*.*)";

    /**
     * The default constructor for a UnixFTPEntryParser object.
     *
     * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not be seen
     *                                  under normal conditions.  It it is seen, this is a sign that
     *                                  <code>REGEX</code> is  not a valid regular expression.
     */
    public RumpusFTPEntryParser() {
        super(REGEX);
    }

    /**
     * Parses a line of a unix (standard) FTP server file listing and converts
     * it into a usable format in the form of an <code> FTPFile </code>
     * instance.  If the file listing line doesn't describe a file,
     * <code> null </code> is returned, otherwise a <code> FTPFile </code>
     * instance representing the files in the directory is returned.
     * <p/>
     *
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    @Override
    public FTPFile parseFTPEntry(String entry) {
        if(matches(entry)) {
            String typeStr = group(1);
            String filesize = group(18);
            String datestr = group(19) + " " + group(20);
            String name = group(21);
            String endtoken = group(22);
            return parseFTPEntry(typeStr, null, null, filesize, datestr, name, endtoken);
        }
        return null;
    }
}