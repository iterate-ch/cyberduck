package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.core.ftp.parser.EPLFFTPEntryParser;
import ch.cyberduck.core.ftp.parser.LaxUnixFTPEntryParser;
import ch.cyberduck.core.ftp.parser.RumpusFTPEntryParser;
import ch.cyberduck.core.ftp.parser.TrellixFTPEntryParser;
import ch.cyberduck.core.ftp.parser.UnitreeFTPEntryParser;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.MVSFTPEntryParser;
import org.apache.commons.net.ftp.parser.NTFTPEntryParser;
import org.apache.commons.net.ftp.parser.NetwareFTPEntryParser;
import org.apache.commons.net.ftp.parser.OS2FTPEntryParser;
import org.apache.commons.net.ftp.parser.OS400FTPEntryParser;
import org.apache.commons.net.ftp.parser.ParserInitializationException;

import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class FTPParserFactory implements FTPFileEntryParserFactory {

    public CompositeFileEntryParser createFileEntryParser(final FTPClientConfig config) throws ParserInitializationException {
        return this.createFileEntryParser(config.getServerSystemKey(), TimeZone.getTimeZone(config.getServerTimeZoneId()));
    }

    public CompositeFileEntryParser createFileEntryParser(final String system) throws ParserInitializationException {
        return this.createFileEntryParser(system, TimeZone.getDefault());
    }

    public CompositeFileEntryParser createFileEntryParser(final String system, final TimeZone timezone) throws ParserInitializationException {
        if(null != system) {
            String ukey = system.toUpperCase(Locale.ROOT);
            if(ukey.contains(FTPClientConfig.SYST_UNIX)) {
                return this.createUnixFTPEntryParser(timezone);
            }
            else if(ukey.contains(FTPClientConfig.SYST_VMS)) {
                throw new ParserInitializationException(String.format("\"%s\" is not currently a supported system.", system));
            }
            else if(ukey.contains(FTPClientConfig.SYST_NETWARE)) {
                return this.createNetwareFTPEntryParser(timezone);
            }
            else if(ukey.contains(FTPClientConfig.SYST_NT)) {
                return this.createNTFTPEntryParser(timezone);
            }
            else if(ukey.contains(FTPClientConfig.SYST_OS2)) {
                return this.createOS2FTPEntryParser(timezone);
            }
            else if(ukey.contains(FTPClientConfig.SYST_OS400)) {
                return this.createOS400FTPEntryParser(timezone);
            }
            else if(ukey.contains(FTPClientConfig.SYST_MVS)) {
                return this.createUnixFTPEntryParser(timezone);
            }
        }
        // Defaulting to UNIX parser
        return this.createUnixFTPEntryParser(timezone);
    }

    private CompositeFileEntryParser createUnixFTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(Arrays.asList(
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
        ));
    }

    private CompositeFileEntryParser createNetwareFTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(Arrays.asList(
                new NetwareFTPEntryParser() {
                    @Override
                    protected FTPClientConfig getDefaultConfiguration() {
                        final FTPClientConfig config = super.getDefaultConfiguration();
                        config.setServerTimeZoneId(timezone.getID());
                        return config;
                    }
                },
                this.createUnixFTPEntryParser(timezone)
        ));
    }

    private CompositeFileEntryParser createNTFTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(Arrays.asList(
                new NTFTPEntryParser() {
                    @Override
                    public FTPClientConfig getDefaultConfiguration() {
                        final FTPClientConfig config = super.getDefaultConfiguration();
                        config.setServerTimeZoneId(timezone.getID());
                        return config;
                    }
                },
                this.createUnixFTPEntryParser(timezone)
        ));
    }

    private CompositeFileEntryParser createOS2FTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(Arrays.asList(
                new OS2FTPEntryParser() {
                    @Override
                    protected FTPClientConfig getDefaultConfiguration() {
                        final FTPClientConfig config = super.getDefaultConfiguration();
                        config.setServerTimeZoneId(timezone.getID());
                        return config;
                    }
                }
        ));
    }

    private CompositeFileEntryParser createOS400FTPEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(Arrays.asList(
                new OS400FTPEntryParser() {
                    @Override
                    protected FTPClientConfig getDefaultConfiguration() {
                        final FTPClientConfig config = super.getDefaultConfiguration();
                        config.setServerTimeZoneId(timezone.getID());
                        return config;
                    }
                },
                this.createUnixFTPEntryParser(timezone)
        ));
    }

    private CompositeFileEntryParser createMVSEntryParser(final TimeZone timezone) {
        return new CompositeFileEntryParser(Arrays.asList(
                new MVSFTPEntryParser() {
                    @Override
                    protected FTPClientConfig getDefaultConfiguration() {
                        final FTPClientConfig config = super.getDefaultConfiguration();
                        config.setServerTimeZoneId(timezone.getID());
                        return config;
                    }
                }
        ));
    }
}