package ch.cyberduck.core.serializer.impl.dd;

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

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.serializer.Reader;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.XMLPropertyListParser;

public abstract class PlistReader<S extends Serializable> implements Reader<S> {
    private static Logger log = Logger.getLogger(PlistReader.class);

    @Override
    public Collection<S> readCollection(final Local file) throws AccessDeniedException {
        final Collection<S> c = new Collection<S>();
        final NSArray list = (NSArray) this.parse(file);
        if(null == list) {
            log.error(String.format("Invalid bookmark file %s", file));
            return c;
        }
        for(int i = 0; i < list.count(); i++) {
            NSObject next = list.objectAtIndex(i);
            if(next instanceof NSDictionary) {
                final NSDictionary dict = (NSDictionary) next;
                final S object = this.deserialize(dict);
                if(null == object) {
                    continue;
                }
                c.add(object);
            }
        }
        return c;
    }

    /**
     * @param file A valid bookmark dictionary
     * @return Null if the file cannot be deserialized
     * @throws AccessDeniedException If the file is not readable
     */
    @Override
    public S read(final Local file) throws AccessDeniedException {
        if(!file.exists()) {
            throw new LocalAccessDeniedException(file.getAbsolute());
        }
        if(!file.isFile()) {
            throw new LocalAccessDeniedException(file.getAbsolute());
        }
        final NSDictionary dict = (NSDictionary) this.parse(file);
        if(null == dict) {
            log.error(String.format("Invalid bookmark file %s", file));
            return null;
        }
        return this.deserialize(dict);
    }

    private NSObject parse(final Local file) throws AccessDeniedException {
        try {
            return XMLPropertyListParser.parse(file.getInputStream());
        }
        catch(ParserConfigurationException | IOException | SAXException | ParseException | PropertyListFormatException e) {
            log.error(String.format("Invalid bookmark file %s", file));
            return null;
        }
    }

    public abstract S deserialize(NSDictionary dict);
}