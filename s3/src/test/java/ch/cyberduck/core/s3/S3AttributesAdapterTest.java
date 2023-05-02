package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import org.jets3t.service.model.StorageObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class S3AttributesAdapterTest {

    @Test
    public void testMtime() {
        final StorageObject object = new StorageObject();
        object.addMetadata("ETag", "a43c1b0aa53a0c908810c06ab1ff3967");
        object.addMetadata("Mtime", "1647683127.160620746");
        assertEquals(1647683127L, new S3AttributesAdapter().toAttributes(object).getModificationDate());
    }

    @Test
    public void testMtimeInvalid() {
        final StorageObject object = new StorageObject();
        object.addMetadata("Mtime", "Invalid");
        assertEquals(-1, new S3AttributesAdapter().toAttributes(object).getModificationDate());
    }
}