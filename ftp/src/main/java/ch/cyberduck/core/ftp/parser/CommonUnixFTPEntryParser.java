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
import org.apache.commons.net.ftp.parser.FTPTimestampParser;
import org.apache.log4j.Logger;

import java.text.ParseException;

public abstract class CommonUnixFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {
    private static final Logger log = Logger.getLogger(CommonUnixFTPEntryParser.class);

    /**
     * @param regex Line regular expression
     */
    public CommonUnixFTPEntryParser(String regex) {
        super(regex);
    }

    /**
     * Defines a default configuration to be used when this class is
     * instantiated without a {@link  FTPClientConfig  FTPClientConfig}
     * parameter being specified.
     *
     * @return the default configuration for this parser.
     */
    @Override
    protected FTPClientConfig getDefaultConfiguration() {
        final FTPClientConfig config = new FTPClientConfig(
                FTPClientConfig.SYST_UNIX,
                FTPTimestampParser.DEFAULT_SDF,
                FTPTimestampParser.DEFAULT_RECENT_SDF,
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
        final FTPExtendedFile file = new FTPExtendedFile();
        int type;
        try {
            file.setTimestamp(this.parseTimestamp(datestr));
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
            // Use != '-' to avoid having to check for suid and sticky bits.
            file.setPermission(access, FTPFile.READ_PERMISSION, (!group(g).equals("-")));
            file.setPermission(access, FTPFile.WRITE_PERMISSION, (!group(g + 1).equals("-")));

            String execPerm = group(g + 2);
            if(execPerm.equals("-")) {
                file.setPermission(access, FTPFile.EXECUTE_PERMISSION, false);
            }
            else {
                file.setPermission(access, FTPFile.EXECUTE_PERMISSION, Character.isLowerCase(execPerm.charAt(0)));
                if(0 == access) {
                    file.setSetuid(execPerm.charAt(0) == 's' || execPerm.charAt(0) == 'S');
                }
                if(1 == access) {
                    file.setSetgid(execPerm.charAt(0) == 's' || execPerm.charAt(0) == 'S');
                }
                if(2 == access) {
                    file.setSticky(execPerm.charAt(0) == 't' || execPerm.charAt(0) == 'T');
                }
            }
        }

        file.setSize(filesize);

        if(null == endtoken) {
            file.setName(name);
        }
        else {
            // oddball cases like symbolic links, file names
            // with spaces in them.
            name += endtoken;
            if(type == FTPFile.SYMBOLIC_LINK_TYPE) {

                int end = name.indexOf(" -> ");
                // Give up if no link indicator is present
                if(end == -1) {
                    file.setName(name);
                }
                else {
                    file.setName(name.substring(0, end));
                    file.setLink(name.substring(end + 4));
                }

            }
            else {
                file.setName(name);
            }
        }
        return file;
    }
}
