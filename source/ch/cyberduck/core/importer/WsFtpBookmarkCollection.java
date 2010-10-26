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
public class WsFtpBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(WsFtpBookmarkCollection.class);

    @Override
    public String getBundleIdentifier() {
        return "com.ipswitch.wsftp";
    }

    @Override
    public String getName() {
        return "WS_FTP";
    }

    /**
     * @return Folder
     */
    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.wsftp.location"));
    }

    @Override
    protected void parse(Local folder) {
        for(Local child : folder.children(new PathFilter<Local>() {
            public boolean accept(Local file) {
                if(file.attributes().isDirectory()) {
                    return false;
                }
                return "ini".equals(file.getExtension());
            }
        })) {
            if(child.attributes().isDirectory()) {
                this.parse(child);
            }
            else {
                this.read(child);
            }
        }
    }

    protected void read(Local file) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream()));
            try {
                Host current = null;
                String line;
                while((line = in.readLine()) != null) {
                    log.trace(line);
                    if(line.startsWith("[")) {
                        this.add(current);

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
                        String value = scanner.next().replaceAll("\"", StringUtils.EMPTY);
                        if("conntype".equals(name)) {
                            try {
                                switch(Integer.parseInt(value)) {
                                    case 4:
                                        current.setProtocol(Protocol.SFTP);
                                        break;
                                    case 5:
                                        current.setProtocol(Protocol.FTP_TLS);
                                        break;
                                }
                            }
                            catch(NumberFormatException e) {
                                log.warn("Unknown Protocol:" + e.getMessage());
                            }
                        }
                        else if("host".equals(name)) {
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
                        else if("dir".equals(name)) {
                            current.setDefaultPath(value);
                        }
                        else if("comment".equals(name)) {
                            current.setComment(value);
                        }
                        else if("uid".equals(name)) {
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

    @Override
    public boolean add(Host bookmark) {
        if(bookmark == null) {
            return false;
        }
        if(bookmark.getHostname().equals(
                Preferences.instance().getProperty("connection.hostname.default"))) {
            return false;
        }
        return super.add(bookmark);
    }
}