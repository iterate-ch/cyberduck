package ch.cyberduck.ui.cocoa.serializer;

/*
 * Copyright (c) 2009 David Kocher. All rights reserved.
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
import ch.cyberduck.core.serializer.HostWriterFactory;
import ch.cyberduck.core.serializer.TransferWriterFactory;
import ch.cyberduck.core.serializer.Writer;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSMutableArray;

import java.util.Collection;

/**
 * @version $Id$
 */
public class PlistWriter<S extends Serializable> implements Writer<S> {

    public static void register() {
        HostWriterFactory.addFactory(Factory.NATIVE_PLATFORM, new HostFactory());
        TransferWriterFactory.addFactory(Factory.NATIVE_PLATFORM, new TransferFactory());
    }

    private static class HostFactory extends HostWriterFactory {
        @Override
        public Writer<Host> create() {
            return new PlistWriter<Host>();
        }
    }

    private static class TransferFactory extends TransferWriterFactory {
        @Override
        public Writer<Transfer> create() {
            return new PlistWriter<Transfer>();
        }
    }

    public void write(Collection<S> collection, Local file) {
        NSMutableArray list = NSMutableArray.array();
        for(S bookmark : collection) {
            list.addObject(bookmark.<NSDictionary>getAsDictionary());
        }
        list.writeToFile(file.getAbsolute());
    }

    public void write(S bookmark, Local file) {
        bookmark.<NSDictionary>getAsDictionary().writeToFile(file.getAbsolute());
    }
}