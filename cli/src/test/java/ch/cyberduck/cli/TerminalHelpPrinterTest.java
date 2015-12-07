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

import org.apache.commons.cli.HelpFormatter;
import org.junit.Ignore;
import org.junit.Test;

public class TerminalHelpPrinterTest {

    @Test
    public void testPrintWidth100() throws Exception {
        TerminalHelpPrinter.print(TerminalOptionsBuilder.options(), new TerminalHelpFormatter(100));
    }

    @Test
    @Ignore
    public void testPrintWidth20DefaultFormatter() throws Exception {
        final HelpFormatter f = new HelpFormatter();
        f.setWidth(20);
        TerminalHelpPrinter.print(TerminalOptionsBuilder.options(), f);
    }

    @Test
    public void testPrintWidth20() throws Exception {
        TerminalHelpPrinter.print(TerminalOptionsBuilder.options(), new TerminalHelpFormatter(40));
    }
}