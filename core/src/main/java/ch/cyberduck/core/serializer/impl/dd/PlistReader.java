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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.serializer.Reader;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;

public abstract class PlistReader<S extends Serializable> implements Reader<S> {

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
        final S deserialized = this.read(file.getInputStream());
        if(null == deserialized) {
            throw new AccessDeniedException(String.format("Failure parsing file %s", file.getName()));
        }
        return deserialized;
    }

    @Override
    public S read(final InputStream in) throws AccessDeniedException {
        final NSDictionary dict = (NSDictionary) this.parse(in);
        return dict != null ? this.deserialize(dict) : null;
    }

    private NSObject parse(final InputStream in) throws AccessDeniedException {
        try {
            return PropertyListParser.parse(in);
        }
        catch(ParserConfigurationException | IOException | SAXException | ParseException |
              PropertyListFormatException e) {
            throw new AccessDeniedException("Failure parsing XML property list", e);
        }
    }

    /**
     * @param dict Serialized format
     * @return Null if deserialization fails
     */
    public abstract S deserialize(NSDictionary dict);
}
