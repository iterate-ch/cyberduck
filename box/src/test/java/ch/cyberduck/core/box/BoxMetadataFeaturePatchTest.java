package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.Folder;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoxMetadataFeaturePatchTest {

    @Test
    public void testNoChange() {
        final Map<String, String> metadata = Collections.singletonMap("k", "v");
        assertTrue(BoxMetadataFeature.toPatch(metadata, metadata).isEmpty());
    }

    @Test
    public void testOperations() {
        final Map<String, String> current = new HashMap<>();
        current.put("keep", "v");
        current.put("change", "v");
        current.put("delete", "v");
        final Map<String, String> update = new LinkedHashMap<>();
        update.put("keep", "v");
        update.put("change", "v2");
        update.put("new", "v");
        final List<Map<String, String>> operations = BoxMetadataFeature.toPatch(current, update);
        assertEquals(3, operations.size());
        assertEquals("remove", operations.get(0).get("op"));
        assertEquals("/delete", operations.get(0).get("path"));
        assertEquals("replace", operations.get(1).get("op"));
        assertEquals("/change", operations.get(1).get("path"));
        assertEquals("v2", operations.get(1).get("value"));
        assertEquals("add", operations.get(2).get("op"));
        assertEquals("/new", operations.get(2).get("path"));
    }

    @Test
    public void testEscapePath() {
        final List<Map<String, String>> operations = BoxMetadataFeature.toPatch(Collections.emptyMap(), Collections.singletonMap("a/b~c", "v"));
        assertEquals("/a~1b~0c", operations.get(0).get("path"));
    }

    @Test
    public void testFromInstances() throws Exception {
        final File file = new JSON().getContext(null).readValue("{\"type\":\"file\",\"id\":\"1\",\"metadata\":{\"global\":{\"properties\":" +
                "{\"$id\":\"i\",\"$type\":\"properties-6bcba49f-ca6d-4d2a-a758-57fe6edf44d0\",\"$parent\":\"file_1\",\"touch\":\"t\"}}}}", File.class);
        assertEquals(Collections.singletonMap("touch", "t"), new BoxAttributesFinderFeature(null, null).toAttributes(file).getMetadata());
        final Folder folder = new JSON().getContext(null).readValue("{\"type\":\"folder\",\"id\":\"2\",\"size\":0,\"metadata\":{\"global\":{}}}", Folder.class);
        assertEquals(Collections.emptyMap(), new BoxAttributesFinderFeature(null, null).toAttributes(folder).getMetadata());
        assertEquals(Collections.singletonMap("k", "v"), BoxMetadataFeature.fromInstances(BoxMetadataFeature.toInstance(Collections.singletonMap("k", "v"))));
    }
}
