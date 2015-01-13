package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.LimitedListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.date.AbstractUserDateFormatter;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class TerminalListProgressListener extends LimitedListProgressListener {

    private Console console = new Console();

    private AbstractUserDateFormatter formatter
            = UserDateFormatterFactory.get();

    private int size = 0;

    private boolean l;

    public TerminalListProgressListener() {
        super(new TerminalProgressListener());
    }

    /**
     * @param l Long format
     */
    public TerminalListProgressListener(final boolean l) {
        super(new TerminalProgressListener());
        this.l = l;
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) throws ListCanceledException {
        for(int i = size; i < list.size(); i++) {
            final Path file = list.get(i);
            if(l) {
                if(file.isSymbolicLink()) {
                    console.printf("%nl%s\t%s\t%s -> %s",
                            file.attributes().getPermission().getSymbol(),
                            formatter.getMediumFormat(
                                    file.attributes().getModificationDate()),
                            file.getName(), file.getSymlinkTarget().getAbsolute());
                }
                else {
                    console.printf("%n%s%s\t%s\t%s\t%s", file.isDirectory() ? "d" : "-",
                            file.attributes().getPermission().getSymbol(),
                            formatter.getMediumFormat(
                                    file.attributes().getModificationDate()),
                            StringUtils.isNotBlank(file.attributes().getRegion())
                                    ? file.attributes().getRegion() : StringUtils.EMPTY,
                            file.getName());
                }
            }
            else {
                console.printf("%n%s", file.getName());
            }
        }
        size += list.size() - size;
        try {
            super.chunk(folder, list);
        }
        catch(ListCanceledException e) {
            if(!this.prompt(e)) {
                throw e;
            }
        }
    }

    private boolean prompt(final ListCanceledException e) {
        final String input;
        try {
            input = console.readLine("%n%s %s? (y/n): ",
                    MessageFormat.format(LocaleFactory.localizedString("Continue listing directory with more than {0} files.", "Alert"), e.getChunk().size()),
                    LocaleFactory.localizedString("Continue", "Credentials"));
        }
        catch(ConnectionCanceledException e1) {
            return false;
        }
        switch(input) {
            case "y":
                return true;
            case "n":
                return false;
            default:
                console.printf("Please type 'y' or 'n'");
                this.prompt(e);
        }
        return true;
    }
}
