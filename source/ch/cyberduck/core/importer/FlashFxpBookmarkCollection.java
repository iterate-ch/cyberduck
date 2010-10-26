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

import ch.cyberduck.core.*;

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
public class FlashFxpBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(FlashFxpBookmarkCollection.class);

    @Override
    public String getBundleIdentifier() {
        return "com.flashfxp";
    }

    @Override
    public String getName() {
        return "FlashFXP";
    }

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.flashfxp.location"));
    }

    @Override
    protected void parse(Local file) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream()));
            try {
                Host current = null;
                String line;
                while((line = in.readLine()) != null) {
                    if(line.startsWith("[")) {
                        current = new Host(Preferences.instance().getProperty("connection.hostname.default"));
                        current.getCredentials().setUsername(
                                Preferences.instance().getProperty("connection.login.anon.name"));
                        current.setProtocol(Protocol.FTP);
                        Pattern pattern = Pattern.compile("\\[(.*)\\]");
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.matches()) {
                            current.setNickname(matcher.group(1));
                        }
                    }
                    else if(StringUtils.isBlank(line)) {
                        this.add(current);
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
                        if("ip".equals(name)) {
                            current.setHostname(value);
                        }
                        else if("port".equals(name)) {
                            try {
                                current.setPort(Integer.parseInt(value));
                            }
                            catch(NumberFormatException e) {
                                log.warn("Invalid Port:" + e.getMessage());
                            }
                        }
                        else if("path".equals(name)) {
                            current.setDefaultPath(value);
                        }
                        else if("notes".equals(name)) {
                            current.setComment(value);
                        }
                        else if("user".equals(name)) {
                            current.getCredentials().setUsername(value);
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