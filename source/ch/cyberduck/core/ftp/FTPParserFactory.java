package ch.cyberduck.core.ftp;

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

import com.enterprisedt.net.ftp.FTPException;

import ch.cyberduck.core.ftp.parser.NetwareFTPEntryParser;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.CompositeFileEntryParser;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.MVSFTPEntryParser;
import org.apache.commons.net.ftp.parser.NTFTPEntryParser;
import org.apache.commons.net.ftp.parser.OS2FTPEntryParser;
import org.apache.commons.net.ftp.parser.OS400FTPEntryParser;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;

import java.io.IOException;

/**
 * @version $Id$
 */
public class FTPParserFactory implements FTPFileEntryParserFactory {

    public FTPFileEntryParser createFileEntryParser(String key) throws ParserInitializationException {
        String ukey = null;
        if(null != key) {
            ukey = key.toUpperCase();
            if(ukey.indexOf("UNIX") >= 0) {
                return this.createUnixFTPEntryParser();
            }
            else if(ukey.indexOf("VMS") >= 0) {
                throw new ParserInitializationException("\"" + key + "\" is not currently a supported system.");
            }
            else if(ukey.indexOf("NETWARE") >= 0) {
                return this.createNetwareFTPEntryParser();
            }
            else if(ukey.indexOf("WINDOWS") >= 0) {
                return this.createNTFTPEntryParser();
            }
            else if(ukey.indexOf("OS/2") >= 0) {
                return this.createOS2FTPEntryParser();
            }
            else if(ukey.indexOf("OS/400") >= 0) {
                return this.createOS400FTPEntryParser();
            }
            else if(ukey.indexOf("MVS") >= 0) {
                return this.createMVSEntryParser();
            }
        }
        // Defaulting to UNIX parser
        return new UnixFTPEntryParser();
    }

    public FTPFileEntryParser createFileEntryParser(FTPClientConfig config) throws ParserInitializationException {
        return this.createFileEntryParser(config.getServerSystemKey());
    }

    private FTPFileEntryParser createUnixFTPEntryParser() {
        return new UnixFTPEntryParser();
    }

    private FTPFileEntryParser createNetwareFTPEntryParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new NetwareFTPEntryParser(),
                        new UnixFTPEntryParser()
                });
    }

    private FTPFileEntryParser createNTFTPEntryParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new NTFTPEntryParser(),
                        new UnixFTPEntryParser()
                });
    }

    private FTPFileEntryParser createOS2FTPEntryParser() {
        return new OS2FTPEntryParser();
    }

    private FTPFileEntryParser createOS400FTPEntryParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new OS400FTPEntryParser(),
                        new UnixFTPEntryParser()
                });
    }

    private FTPFileEntryParser createMVSEntryParser() {
        return new MVSFTPEntryParser();
    }
}