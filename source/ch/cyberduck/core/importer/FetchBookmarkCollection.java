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
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.serializer.PlistDeserializer;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * @version $Id$
 */
public class FetchBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(FetchBookmarkCollection.class);

    @Override
    public void load() {
        super.load(LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.fetch.location")));
    }

    @Override
    protected void parse(Local file) {
        NSDictionary serialized = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        if(null == serialized) {
            return;
        }
        NSDictionary dict = new PlistDeserializer(serialized).objectForKey("Shortcuts v2");
        if(null == dict) {
            return;
        }
        dict = new PlistDeserializer(dict).objectForKey("Shortcuts");
        if(null == dict) {
            return;
        }
        List<NSDictionary> shortcuts = new PlistDeserializer(dict).listForKey("Shortcuts");
        for(NSDictionary shortcut : shortcuts) {
            PlistDeserializer reader = new PlistDeserializer(shortcut);
            NSDictionary remote = reader.objectForKey("Remote Item");
            if(null == remote) {
                continue;
            }
            NSDictionary location = new PlistDeserializer(remote).objectForKey("Location");
            if(null == location) {
                continue;
            }
            String url = new PlistDeserializer(location).stringForKey("URL");
            if(null == url) {
                continue;
            }
            final Host host = Host.parse(url);
            host.setNickname(reader.stringForKey("Name"));
            this.add(host);
        }
    }
}