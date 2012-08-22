package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Id$
 */
public class WinScpBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(WinScpBookmarkCollection.class);

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
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.winscp.location"));
    }

    @Override
    protected void parse(Local file) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream()));
            try {
                Host current = null;
                String line;
                while((line = in.readLine()) != null) {
                    if(line.startsWith("[Sessions\\")) {
                        current = new Host(Preferences.instance().getProperty("connection.hostname.default"));
                        current.getCredentials().setUsername(
                                Preferences.instance().getProperty("connection.login.anon.name"));
                        current.setProtocol(Protocol.SFTP);
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
                        String name = scanner.next().toLowerCase();
                        if(!scanner.hasNext()) {
                            log.warn("Missing value in line:" + line);
                            continue;
                        }
                        String value = scanner.next();
                        if("HostName".equals(name)) {
                            current.setHostname(value);
                        }
                        else if("UserName".equals(name)) {
                            current.getCredentials().setUsername(value);
                        }
                        else if("PortNumber".equals(name)) {
                            try {
                                current.setPort(Integer.parseInt(value));
                            }
                            catch(NumberFormatException e) {
                                log.warn("Invalid Port:" + e.getMessage());
                            }
                        }
                        else if("FSProtocol".equals(name)) {
                            try {
                                switch(Integer.parseInt(value)) {
                                    case 0:
                                    case 1:
                                    case 2:
                                        current.setProtocol(Protocol.SFTP);
                                        break;
                                    case 5:
                                        current.setProtocol(Protocol.FTP);
                                        break;
                                }
                            }
                            catch(NumberFormatException e) {
                                log.warn("Unknown Protocol:" + e.getMessage());
                            }
                        }
                    }
                }
            }
            finally {
                IOUtils.closeQuietly(in);
            }
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
    }
}
