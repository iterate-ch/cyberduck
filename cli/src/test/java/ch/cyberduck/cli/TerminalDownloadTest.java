package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class TerminalDownloadTest {

    @Test
    public void testDownloadHttp() throws Exception {
        final CommandLineParser parser = new DefaultParser();
        final Options options = TerminalOptionsBuilder.options();
        final CommandLine input = parser.parse(options, new String[]{
            "--assumeyes",
            "--download", "https://ftp.gnu.org/gnu/wget/wget-1.19.4.tar.gz",
            LocalFactory.get(LocalFactory.get(),
                new AlphanumericRandomStringService().random()).getAbsolute()});
        final LinuxTerminalPreferences preferences = new LinuxTerminalPreferences();
        preferences.load();
        preferences.setFactories();
        preferences.setDefaults();
        final ProtocolFactory protocols = new ProtocolFactory(new HashSet<>(Collections.singletonList(new DAVSSLProtocol())));
        protocols.register(new ProfilePlistReader(protocols).read(this.getClass().getResourceAsStream("/DAVS.cyberduckprofile")));
        final Terminal terminal = new Terminal(protocols, preferences, options, input);
        assertEquals(Terminal.Exit.success, terminal.execute());
    }
}
