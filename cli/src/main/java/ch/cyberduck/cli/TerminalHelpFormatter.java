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

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;

public final class TerminalHelpFormatter extends HelpFormatter {

    private static final int DEFAULT_WIDTH = 200;

    public TerminalHelpFormatter() {
        this(DEFAULT_WIDTH);
    }

    public TerminalHelpFormatter(final int width) {
        this.setWidth(width);
    }

    @Override
    public void printHelp(PrintWriter pw, int width, String cmdLineSyntax,
                          String header, Options options, int leftPad,
                          int descPad, String footer, boolean autoUsage) {
        if(autoUsage) {
            this.printUsage(pw, width, cmdLineSyntax, options);
        }
        else {
            this.printUsage(pw, width, cmdLineSyntax);
        }
        if((header != null) && (header.length() > 0)) {
            this.printWrapped(pw, width, header);
        }
        this.printOptions(pw, width, options, leftPad, descPad);
        if((footer != null) && (footer.length() > 0)) {
            this.printWrapped(pw, width, footer);
        }
    }

    protected StringBuffer renderWrappedText(final StringBuffer sb, final int width, int nextLineTabStop, String text) {
        int pos = findWrapPos(text, width, 0);
        if(pos == -1) {
            sb.append(rtrim(text));

            return sb;
        }
        sb.append(rtrim(text.substring(0, pos))).append(getNewLine());
        if(nextLineTabStop >= width) {
            // stops infinite loop happening
            nextLineTabStop = 1;
        }
        // all following lines must be padded with nextLineTabStop space characters
        final String padding = createPadding(nextLineTabStop);
        while(true) {
            text = padding + StringUtils.removeStart(text.substring(pos), StringUtils.SPACE);
            pos = findWrapPos(text, width, 0);
            if(pos == -1) {
                sb.append(text);
                return sb;
            }
            if((text.length() > width) && (pos == nextLineTabStop - 1)) {
                pos = width;
            }
            sb.append(rtrim(text.substring(0, pos))).append(getNewLine());
        }
    }
}
