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

import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.core.ftp.parser.*;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.*;
import org.apache.log4j.Logger;

import java.util.TimeZone;

/**
 * @version $Id$
 */
public class FTPParserFactory implements FTPFileEntryParserFactory {
    private static Logger log = Logger.getLogger(FTPParserFactory.class);

    public FTPFileEntryParser createFileEntryParser(String system, TimeZone timezone) throws ParserInitializationException {
        if(null != system) {
            String ukey = system.toUpperCase();
            if(ukey.indexOf(FTPClientConfig.SYST_UNIX) >= 0) {
                return this.createUnixFTPEntryParser(timezone);
            }
            else if(ukey.indexOf(FTPClientConfig.SYST_VMS) >= 0) {
                throw new ParserInitializationException("\"" + system + "\" is not currently a supported system.");
            }
            else if(ukey.indexOf(FTPClientConfig.SYST_NETWARE) >= 0) {
                return this.createNetwareFTPEntryParser(timezone);
            }
            else if(ukey.indexOf(FTPClientConfig.SYST_NT) >= 0) {
                return this.createNTFTPEntryParser(timezone);
            }
            else if(ukey.indexOf(FTPClientConfig.SYST_OS2) >= 0) {
                return this.createOS2FTPEntryParser(timezone);
            }
            else if(ukey.indexOf(FTPClientConfig.SYST_OS400) >= 0) {
                return this.createOS400FTPEntryParser(timezone);
            }
            else if(ukey.indexOf(FTPClientConfig.SYST_MVS) >= 0) {
                return this.createMVSEntryParser(timezone);
            }
        }
        // Defaulting to UNIX parser
        return this.createUnixFTPEntryParser(timezone);
    }

    public FTPFileEntryParser createFileEntryParser(String system) throws ParserInitializationException {
        return this.createFileEntryParser(system, TimeZone.getDefault());
    }

    public FTPFileEntryParser createFileEntryParser(FTPClientConfig config) throws ParserInitializationException {
        return this.createFileEntryParser(config.getServerSystemKey(), TimeZone.getTimeZone(config.getServerTimeZoneId()));
    }

    private FTPFileEntryParser createUnixFTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new LaxUnixFTPEntryParser() {
                            @Override
                            protected FTPClientConfig getDefaultConfiguration() {
                                final FTPClientConfig config = super.getDefaultConfiguration();
                                config.setServerTimeZoneId(timezone.getID());
                                return config;
                            }
                        },
                        new EPLFFTPEntryParser(),
                        new RumpusFTPEntryParser() {
                            @Override
                            protected FTPClientConfig getDefaultConfiguration() {
                                final FTPClientConfig config = super.getDefaultConfiguration();
                                config.setServerTimeZoneId(timezone.getID());
                                return config;
                            }
                        },
                        new TrellixFTPEntryParser() {
                            @Override
                            protected FTPClientConfig getDefaultConfiguration() {
                                final FTPClientConfig config = super.getDefaultConfiguration();
                                config.setServerTimeZoneId(timezone.getID());
                                return config;
                            }
                        },
                        new UnitreeFTPEntryParser() {
                            @Override
                            protected FTPClientConfig getDefaultConfiguration() {
                                final FTPClientConfig config = super.getDefaultConfiguration();
                                config.setServerTimeZoneId(timezone.getID());
                                return config;
                            }
                        }
                });
    }

    private FTPFileEntryParser createNetwareFTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new NetwareFTPEntryParser() {
                            @Override
                            protected FTPClientConfig getDefaultConfiguration() {
                                final FTPClientConfig config = super.getDefaultConfiguration();
                                config.setServerTimeZoneId(timezone.getID());
                                return config;
                            }
                        },
                        this.createUnixFTPEntryParser(timezone)
                });
    }

    private FTPFileEntryParser createNTFTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new NTFTPEntryParser() {
                            @Override
                            public FTPClientConfig getDefaultConfiguration() {
                                final FTPClientConfig config = super.getDefaultConfiguration();
                                config.setServerTimeZoneId(timezone.getID());
                                return config;
                            }
                        },
                        this.createUnixFTPEntryParser(timezone)
                });
    }

    private FTPFileEntryParser createOS2FTPEntryParser(final TimeZone timezone) {
        return new OS2FTPEntryParser() {
            @Override
            protected FTPClientConfig getDefaultConfiguration() {
                final FTPClientConfig config = super.getDefaultConfiguration();
                config.setServerTimeZoneId(timezone.getID());
                return config;
            }
        };
    }

    private FTPFileEntryParser createOS400FTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                        new OS400FTPEntryParser() {
                            @Override
                            protected FTPClientConfig getDefaultConfiguration() {
                                final FTPClientConfig config = super.getDefaultConfiguration();
                                config.setServerTimeZoneId(timezone.getID());
                                return config;
                            }
                        },
                        this.createUnixFTPEntryParser(timezone)
                });
    }

    private FTPFileEntryParser createMVSEntryParser(final TimeZone timezone) {
        return new MVSFTPEntryParser() {
            @Override
            protected FTPClientConfig getDefaultConfiguration() {
                final FTPClientConfig config = super.getDefaultConfiguration();
                config.setServerTimeZoneId(timezone.getID());
                return config;
            }
        };
    }
}