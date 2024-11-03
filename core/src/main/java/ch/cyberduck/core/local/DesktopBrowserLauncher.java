package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class DesktopBrowserLauncher implements BrowserLauncher {
    private static final Logger log = LogManager.getLogger(DesktopBrowserLauncher.class);

    @Override
    public boolean open(final String url) {
        if(Desktop.isDesktopSupported()) {
            // Use the isDesktopSupported() method to determine whether the Desktop API is available. On the Solaris Operating System and the Linux platform, this API is dependent on Gnome libraries. If those libraries are unavailable, this method will return false.
            try {
                Desktop.getDesktop().browse(URI.create(url));
            }
            catch(IOException e) {
                log.warn("Failure opening URL {} with browser", url);
                return false;
            }
            return true;
        }
        return false;
    }
}
