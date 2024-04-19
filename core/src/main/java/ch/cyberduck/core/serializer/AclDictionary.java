package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DeserializerFactory;

import java.util.List;

public class AclDictionary<T> {

    private final DeserializerFactory<T> deserializer;

    public AclDictionary() {
        this.deserializer = new DeserializerFactory<>();
    }

    public AclDictionary(final DeserializerFactory<T> deserializer) {
        this.deserializer = deserializer;
    }

    public Acl deserialize(T serialized) {
        final Deserializer<?> dict = deserializer.create(serialized);
        final Acl acl = new Acl();
        final List<String> keys = dict.keys();
        for(String key : keys) {
            final Acl.CanonicalUser user = new Acl.CanonicalUser(key);
            acl.addAll(user);
            final List<Object> rolesObj = dict.listForKey(key);
            for(Object roleObj : rolesObj) {
                acl.get(user).add(new Acl.RoleDictionary(deserializer).deserialize(roleObj));
            }
        }
        return acl;
    }
}
