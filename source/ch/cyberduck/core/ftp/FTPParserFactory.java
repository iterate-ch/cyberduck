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

import ch.cyberduck.core.ftp.parser.EPLFFTPEntryParser;
import ch.cyberduck.core.ftp.parser.NetwareFTPEntryParser;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.*;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class FTPParserFactory implements FTPFileEntryParserFactory {
    private static Logger log = Logger.getLogger(FTPParserFactory.class);

    public FTPFileEntryParser createFileEntryParser(String key) throws ParserInitializationException {
        if(null != key) {
            String ukey = key.toUpperCase();
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
        return this.createUnixFTPEntryParser();
    }

    public FTPFileEntryParser createFileEntryParser(FTPClientConfig config) throws ParserInitializationException {
        return this.createFileEntryParser(config.getServerSystemKey());
    }

    private FTPFileEntryParser createUnixFTPEntryParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new UnixFTPEntryParser(),
                        new EPLFFTPEntryParser()
                });
    }

    private FTPFileEntryParser createNetwareFTPEntryParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new NetwareFTPEntryParser(),
                        this.createUnixFTPEntryParser()
                });
    }

    private FTPFileEntryParser createNTFTPEntryParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new NTFTPEntryParser(),
                        this.createUnixFTPEntryParser()
                });
    }

    private FTPFileEntryParser createOS2FTPEntryParser() {
        return new OS2FTPEntryParser();
    }

    private FTPFileEntryParser createOS400FTPEntryParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new OS400FTPEntryParser(),
                        this.createUnixFTPEntryParser()
                });
    }

    private FTPFileEntryParser createMVSEntryParser() {
        return new MVSFTPEntryParser();
    }
}