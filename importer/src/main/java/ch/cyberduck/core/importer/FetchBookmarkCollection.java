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

import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.impl.jna.PlistDeserializer;

import java.util.List;

public class FetchBookmarkCollection extends ThirdpartyBookmarkCollection {

    private static final long serialVersionUID = -7544710198776572190L;

    @Override
    public String getBundleIdentifier() {
        return "com.fetchsoftworks.Fetch";
    }

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.fetch.location"));
    }

    @Override
    protected void parse(final Local file) throws AccessDeniedException {
        NSDictionary serialized = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        if(null == serialized) {
            throw new LocalAccessDeniedException(String.format("Invalid bookmark file %s", file));
        }
        NSDictionary dict = new PlistDeserializer(serialized).objectForKey("Shortcuts v2");
        if(null == dict) {
            throw new LocalAccessDeniedException(String.format("Invalid bookmark file %s", file));
        }
        dict = new PlistDeserializer(dict).objectForKey("Shortcuts");
        if(null == dict) {
            throw new LocalAccessDeniedException(String.format("Invalid bookmark file %s", file));
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
            final Host host = HostParser.parse(url);
            host.setNickname(reader.stringForKey("Name"));
            this.add(host);
        }
    }
}