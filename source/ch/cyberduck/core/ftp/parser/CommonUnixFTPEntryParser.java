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

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.ConfigurableFTPFileEntryParserImpl;
import org.apache.log4j.Logger;

import java.text.ParseException;

/**
 * @version $Id:$
 */
public abstract class CommonUnixFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {
    private static Logger log = Logger.getLogger(CommonUnixFTPEntryParser.class);

    static final String DEFAULT_DATE_FORMAT
            = "MMM d yyyy"; //Nov 9 2001

    static final String DEFAULT_RECENT_DATE_FORMAT
            = "MMM d HH:mm"; //Nov 9 20:06

    static final String NUMERIC_DATE_FORMAT
            = "yyyy-MM-dd HH:mm"; //2001-11-09 20:06

    /**
     * Some Linux distributions are now shipping an FTP server which formats
     * file listing dates in an all-numeric format:
     * <code>"yyyy-MM-dd HH:mm</code>.
     * This is a very welcome development,  and hopefully it will soon become
     * the standard.  However, since it is so new, for now, and possibly
     * forever, we merely accomodate it, but do not make it the default.
     * <p/>
     * For now end users may specify this format only via
     * <code>UnixFTPEntryParser(FTPClientConfig)</code>.
     * Steve Cohen - 2005-04-17
     */
    public static final FTPClientConfig NUMERIC_DATE_CONFIG =
            new FTPClientConfig(
                    FTPClientConfig.SYST_UNIX,
                    NUMERIC_DATE_FORMAT,
                    null, null, null, null);

    /**
     * @param REGEX
     */
    public CommonUnixFTPEntryParser(String REGEX) {
        super(REGEX);
    }

    /**
     * Defines a default configuration to be used when this class is
     * instantiated without a {@link  FTPClientConfig  FTPClientConfig}
     * parameter being specified.
     *
     * @return the default configuration for this parser.
     */
    protected FTPClientConfig getDefaultConfiguration() {
        final FTPClientConfig config = new FTPClientConfig(
                FTPClientConfig.SYST_UNIX,
                DEFAULT_DATE_FORMAT,
                DEFAULT_RECENT_DATE_FORMAT,
                null, null, null);
        config.setLenientFutureDates(true);
        return config;
    }

    protected FTPFile parseFTPEntry(String typeStr, String usr, String grp, String filesize, String datestr, String name, String endtoken) {
        try {
            return this.parseFTPEntry(typeStr, usr, grp, Long.parseLong(filesize), datestr, name, endtoken);
        }
        catch(NumberFormatException e) {
            // intentionally do nothing
        }
        return this.parseFTPEntry(typeStr, usr, grp, -1, datestr, name, endtoken);
    }

    protected FTPFile parseFTPEntry(String typeStr, String usr, String grp, long filesize, String datestr, String name, String endtoken) {
        FTPFile file = new FTPFile();
        int type;
        boolean isDevice;
        try {
            file.setTimestamp(super.parseTimestamp(datestr));
        }
        catch(ParseException e) {
            log.warn(e.getMessage());
        }

        // bcdlfmpSs-
        switch(typeStr.charAt(0)) {
            case 'd':
                type = FTPFile.DIRECTORY_TYPE;
                break;
            case 'l':
                type = FTPFile.SYMBOLIC_LINK_TYPE;
                break;
            case 'b':
            case 'c':
                isDevice = true;
                // break; - fall through
            case 'f':
            case '-':
                type = FTPFile.FILE_TYPE;
                break;
            default:
                type = FTPFile.UNKNOWN_TYPE;
        }

        file.setType(type);
        file.setUser(usr);
        file.setGroup(grp);

        int g = 4;
        for(int access = 0; access < 3; access++, g += 4) {
            // Use != '-' to avoid having to check for suid and sticky bits
            file.setPermission(access, FTPFile.READ_PERMISSION,
                    (!group(g).equals("-")));
            file.setPermission(access, FTPFile.WRITE_PERMISSION,
                    (!group(g + 1).equals("-")));

            String execPerm = group(g + 2);
            if(!execPerm.equals("-") && !Character.isUpperCase(execPerm.charAt(0))) {
                file.setPermission(access, FTPFile.EXECUTE_PERMISSION, true);
            } else {
                file.setPermission(access, FTPFile.EXECUTE_PERMISSION, false);
            }
        }

        file.setSize(filesize);

        if(null == endtoken) {
            file.setName(name);
        } else {
            // oddball cases like symbolic links, file names
            // with spaces in them.
            name += endtoken;
            if(type == FTPFile.SYMBOLIC_LINK_TYPE) {

                int end = name.indexOf(" -> ");
                // Give up if no link indicator is present
                if(end == -1) {
                    file.setName(name);
                } else {
                    file.setName(name.substring(0, end));
                    file.setLink(name.substring(end + 4));
                }

            } else {
                file.setName(name);
            }
        }
        return file;
    }
}
