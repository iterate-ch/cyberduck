package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.azure.AzureProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.apache.commons.cli.HelpFormatter;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class TerminalHelpPrinterTest {

    @Test
    public void testPrintWidth100() {
        TerminalHelpPrinter.print(TerminalOptionsBuilder.options(), new TerminalHelpFormatter(100));
    }

    @Test
    @Ignore
    public void testPrintWidth20DefaultFormatter() {
        final HelpFormatter f = new TerminalHelpFormatter();
        f.setWidth(20);
        TerminalHelpPrinter.print(TerminalOptionsBuilder.options(), f);
    }

    @Test
    public void testPrintWidth20() {
        TerminalHelpPrinter.print(TerminalOptionsBuilder.options(), new TerminalHelpFormatter(40));
    }

    @Test
    public void testScheme() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new AzureProtocol())));
        final ProfilePlistReader reader = new ProfilePlistReader(factory);
        final Profile profile = reader.read(
            this.getClass().getResourceAsStream("/Azure.cyberduckprofile")
        );
        assertEquals("azure", TerminalHelpPrinter.getScheme(profile));
    }
}
