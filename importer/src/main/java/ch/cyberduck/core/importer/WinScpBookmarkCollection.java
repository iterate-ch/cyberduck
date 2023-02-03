package ch.cyberduck.core.importer;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WinScpBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = LogManager.getLogger(WinScpBookmarkCollection.class);

    private static final long serialVersionUID = 4886529703737860985L;

    @Override
    public String getBundleIdentifier() {
        return "net.winscp";
    }

    @Override
    public String getName() {
        return "WinSCP";
    }

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.winscp.location"));
    }

    @Override
    protected void parse(final ProtocolFactory protocols, final Local file) throws AccessDeniedException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            Host current = null;
            String line;
            while((line = in.readLine()) != null) {
                if(line.startsWith("[Sessions\\")) {
                    current = new Host(protocols.forScheme(Scheme.sftp));
                    current.getCredentials().setUsername(
                        PreferencesFactory.get().getProperty("connection.login.anon.name"));
                    Pattern pattern = Pattern.compile("\\[Session\\\\(.*)\\]");
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.matches()) {
                        current.setNickname(matcher.group(1));
                    }
                }
                else if(StringUtils.isBlank(line)) {
                    this.add(current);
                    current = null;
                }
                else {
                    if(null == current) {
                        log.warn("Failed to detect start of bookmark");
                        continue;
                    }
                    Scanner scanner = new Scanner(line);
                    scanner.useDelimiter("=");
                    if(!scanner.hasNext()) {
                        log.warn("Missing key in line:" + line);
                        continue;
                    }
                    String name = scanner.next().toLowerCase(Locale.ROOT);
                    if(!scanner.hasNext()) {
                        log.warn("Missing value in line:" + line);
                        continue;
                    }
                    String value = scanner.next();
                    switch(name) {
                        case "hostname":
                            current.setHostname(value);
                            break;
                        case "username":
                            current.getCredentials().setUsername(value);
                            break;
                        case "portnumber":
                            try {
                                current.setPort(Integer.parseInt(value));
                            }
                            catch(NumberFormatException e) {
                                log.warn("Invalid Port:" + e.getMessage());
                            }
                            break;
                        case "fsprotocol":
                            try {
                                switch(Integer.parseInt(value)) {
                                    case 0:
                                    case 1:
                                    case 2:
                                        current.setProtocol(protocols.forScheme(Scheme.sftp));
                                        break;
                                    case 5:
                                        current.setProtocol(protocols.forScheme(Scheme.ftp));
                                        break;
                                }
                                // Reset port to default
                                current.setPort(-1);
                            }
                            catch(NumberFormatException e) {
                                log.warn("Unknown Protocol:" + e.getMessage());
                            }
                            break;
                    }
                }
            }
        }
        catch(IOException e) {
            throw new AccessDeniedException(e.getMessage(), e);
        }
    }
}
