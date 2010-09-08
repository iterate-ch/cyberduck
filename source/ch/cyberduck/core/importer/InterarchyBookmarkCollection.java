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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @version $Id$
 */
public class InterarchyBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(InterarchyBookmarkCollection.class);

    @Override
    public String getBundleIdentifier() {
        return "com.nolobe.interarchy";
    }

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.interarchy.location"));
    }

    @Override
    protected void parse(Local file) {
        NSDictionary serialized = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        if(null == serialized) {
            log.error("Invalid bookmark file:" + file);
            return;
        }
        List<NSDictionary> items = new PlistDeserializer(serialized).listForKey("Children");
        if(null == items) {
            log.error("Invalid bookmark file:" + file);
            return;
        }
        for(NSDictionary item : items) {
            this.parse(item);
        }
    }

    private void parse(NSDictionary item) {
        final PlistDeserializer bookmark = new PlistDeserializer(item);
        List<NSDictionary> children = bookmark.listForKey("Children");
        if(null != children) {
            for(NSDictionary child : children) {
                this.parse(child);
            }
            return;
        }
        String url = bookmark.stringForKey("URL");
        if(StringUtils.isBlank(url)) {
            // Possibly a folder
            return;
        }
        Host host = Host.parse(url);
        if(StringUtils.isBlank(host.getHostname())) {
            // Possibly file://
            return;
        }
        String title = bookmark.stringForKey("Title");
        if(StringUtils.isNotBlank(title)) {
            host.setNickname(title);
        }
        this.add(host);
    }
}