package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.SerializerFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AclDictionaryTest {

    @Test
    public void testSerialize() {
        Acl attributes = new Acl(new Acl.UserAndRole(new Acl.CanonicalUser(), new Acl.Role("w")));
        Acl clone = new AclDictionary<>().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(attributes.get(new Acl.CanonicalUser()), clone.get(new Acl.CanonicalUser()));
        assertNotEquals(attributes, new Acl(new Acl.UserAndRole(new Acl.CanonicalUser("t"), new Acl.Role("w"))));
        assertEquals(attributes.get(new Acl.CanonicalUser()), clone.get(new Acl.CanonicalUser()));
        assertNotEquals(attributes.get(new Acl.CanonicalUser()), new Acl(new Acl.UserAndRole(new Acl.CanonicalUser(""), new Acl.Role("r"))).get(new Acl.CanonicalUser()));
    }

    @Test
    public void testSerializeCanonicaluserNoRoles() {
        Acl acl = new Acl(new Acl.CanonicalUser());
        Acl clone = new AclDictionary<>().deserialize(acl.serialize(SerializerFactory.get()));
        assertEquals(acl, clone);
        assertEquals(acl.get(new Acl.CanonicalUser()), clone.get(new Acl.CanonicalUser()));
    }
}
