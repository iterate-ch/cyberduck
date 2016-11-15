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
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.text.MessageFormat;

public class TerminalListProgressListener extends LimitedListProgressListener {

    private final Console console = new Console();

    private final AbstractUserDateFormatter formatter
            = UserDateFormatterFactory.get();

    private int size = 0;

    private boolean l;

    private TerminalPromptReader prompt;

    public TerminalListProgressListener() {
        super(new TerminalProgressListener());
        this.prompt = new InteractiveTerminalPromptReader();
    }

    /**
     * @param l Long format
     */
    public TerminalListProgressListener(final boolean l) {
        super(new TerminalProgressListener());
        this.l = l;
    }

    public TerminalListProgressListener(final TerminalPromptReader prompt) {
        super(new TerminalProgressListener());
        this.prompt = prompt;
    }

    public TerminalListProgressListener(final TerminalPromptReader prompt, final boolean l) {
        super(new TerminalProgressListener());
        this.l = l;
        this.prompt = prompt;
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) throws ListCanceledException {
        for(int i = size; i < list.size(); i++) {
            final Path file = list.get(i);
            if(l) {
                if(file.isSymbolicLink()) {
                    console.printf("%n%sl%s\t%s\t%s -> %s%s",
                            Ansi.ansi().bold(),
                            file.attributes().getPermission().getSymbol(),
                            formatter.getMediumFormat(
                                    file.attributes().getModificationDate()),
                            file.getName(), file.getSymlinkTarget().getAbsolute(),
                            Ansi.ansi().reset());
                }
                else {
                    console.printf("%n%s%s%s\t%s\t%s\t%s%s",
                            Ansi.ansi().bold(),
                            file.isDirectory() ? "d" : "-",
                            file.attributes().getPermission().getSymbol(),
                            formatter.getMediumFormat(
                                    file.attributes().getModificationDate()),
                            StringUtils.isNotBlank(file.attributes().getRegion())
                                    ? file.attributes().getRegion() : StringUtils.EMPTY,
                            file.getName(),
                            Ansi.ansi().reset());
                }
            }
            else {
                console.printf("%n%s%s%s", Ansi.ansi().bold(), file.getName(), Ansi.ansi().reset());
            }
        }
        size += list.size() - size;
        try {
            super.chunk(folder, list);
        }
        catch(ListCanceledException e) {
            final String message = String.format("%s %s?: ",
                    MessageFormat.format(LocaleFactory.localizedString("Continue listing directory with more than {0} files.", "Alert"), e.getChunk().size()),
                    LocaleFactory.localizedString("Continue", "Credentials"));
            if(!prompt.prompt(message)) {
                throw e;
            }
        }
    }
}
