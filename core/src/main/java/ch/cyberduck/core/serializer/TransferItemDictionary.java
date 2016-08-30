package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

public class TransferItemDictionary {

    private DeserializerFactory deserializer;

    public TransferItemDictionary() {
        this.deserializer = new DeserializerFactory();
    }

    public TransferItemDictionary(final DeserializerFactory deserializer) {
        this.deserializer = deserializer;
    }

    public <T> TransferItem deserialize(T serialized) {
        final Deserializer dict = deserializer.create(serialized);
        final Path remote = new PathDictionary(deserializer).deserialize(dict.objectForKey("Remote"));
        if(null == remote) {
            return null;
        }
        final Object localObj = dict.objectForKey("Local Dictionary");
        if(localObj != null) {
            return new TransferItem(remote, new LocalDictionary(deserializer).deserialize((localObj)));
        }
        return new TransferItem(remote);
    }
}
