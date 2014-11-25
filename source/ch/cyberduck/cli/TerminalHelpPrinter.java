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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public final class TerminalHelpPrinter {

    private TerminalHelpPrinter() {
        //
    }

    public static void help(final Options options) {
        final HelpFormatter formatter = new TerminalHelpFormatter();
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setWidth(200);
        final StringBuilder protocols = new StringBuilder("\nSupported protocols").append(StringUtils.LF);
        for(Protocol p : ProtocolFactory.getEnabledProtocols()) {
            protocols.append(p.getProvider()).append("\t").append(p.getDescription()).append(StringUtils.LF);
            switch(p.getType()) {
                case s3:
                case googlestorage:
                case swift:
                case azure:
                    protocols.append("\tExample URL: ").append(String.format("%s://<container>/<key>", p.getProvider()));
                    break;
                default:
                    protocols.append("\tExample URL: ").append(String.format("%s://<hostname>/<folder>/<file>", p.getProvider()));
            }
            protocols.append(StringUtils.LF);
        }
        final String header = "\n\tURLs must be a fully qualified with a " +
                "remote file or folder such as ftps://user@example.net/resource.\n" + protocols.toString();
        final StringAppender footer = new StringAppender();
        footer.append("Cyberduck is libre software licenced under the GPL");
        footer.append(String.format("For general help about using Cyberduck, please refer to %s",
                Preferences.instance().getProperty("website.help")));
        footer.append(String.format("For bug reports or feature requests open a ticket at %s",
                MessageFormat.format(Preferences.instance().getProperty("website.bug"),
                        Preferences.instance().getProperty("application.version"))));
        final License l = LicenseFactory.find();
        if(l.verify()) {
            footer.append(l.toString());
        }
        formatter.printHelp("duck [options...]", header, options, footer.toString());
    }

    private static final class TerminalHelpFormatter extends HelpFormatter {
        @Override
        public void printHelp(PrintWriter pw, int width, String cmdLineSyntax,
                              String header, Options options, int leftPad,
                              int descPad, String footer, boolean autoUsage) {
            if(autoUsage) {
                printUsage(pw, width, cmdLineSyntax, options);
            }
            else {
                printUsage(pw, width, cmdLineSyntax);
            }

            if((header != null) && (header.length() > 0)) {
                printWrapped(pw, width, header);
            }

            printOptions(pw, width, options, leftPad, descPad);

            if((footer != null) && (footer.length() > 0)) {
                printWrapped(pw, width, footer);
            }
        }

        protected StringBuffer renderWrappedText(StringBuffer sb, int width,
                                                 int nextLineTabStop, String text) {
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
                text = padding + text.substring(pos);
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
}
