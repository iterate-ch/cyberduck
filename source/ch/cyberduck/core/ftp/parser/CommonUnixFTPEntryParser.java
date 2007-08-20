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

import org.apache.commons.net.ftp.parser.ConfigurableFTPFileEntryParserImpl;
import org.apache.commons.net.ftp.FTPClientConfig;

/**
 * @version $Id:$
 */
public abstract class CommonUnixFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {

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
     *
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
        return new FTPClientConfig(
                FTPClientConfig.SYST_UNIX,
                DEFAULT_DATE_FORMAT,
                DEFAULT_RECENT_DATE_FORMAT,
                null, null, null);
    }
}
