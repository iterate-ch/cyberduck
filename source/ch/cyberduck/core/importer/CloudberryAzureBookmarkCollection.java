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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

/**
 * @version $Id: FilezillaBookmarkCollection.java 7465 2010-11-02 16:28:09Z dkocher $
 */
public class CloudberryAzureBookmarkCollection extends CloudberryBookmarkCollection {
    private static Logger log = Logger.getLogger(CloudberryAzureBookmarkCollection.class);

    @Override
    public String getBundleIdentifier() {
        return "com.cloudberrylab.explorer.azure";
    }

    @Override
    public String getName() {
        return "CloudBerry Explorer for Azure Blob Storage";
    }

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.cloudberry.azure.location"));
    }
}