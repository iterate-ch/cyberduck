package ch.cyberduck.core.eue.io.swagger.client.model;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.eue.EueExceptionMappingService;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.JSON;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResourceCreationResponseEntriesTest {

    @Test
    public void testDeserialize() throws Exception {
        final ResourceCreationResponseEntries entries = new JSON().getContext(ResourceCreationResponseEntries.class).readValue(
                new StringReader("{\"Uy6v3wzF\":{\"statusCode\":201,\"headers\":{\"Location\":\"../../resource/1031646835329996173\"}}}"), ResourceCreationResponseEntries.class);
        assertTrue(entries.containsKey("Uy6v3wzF"));
        assertEquals(201, entries.get("Uy6v3wzF").getStatusCode(), 0);
    }

    @Test
    public void testInsufficientStorage() throws Exception {
        final ResourceCreationResponseEntries entries = new JSON().getContext(ResourceCreationResponseEntries.class).readValue(
                new StringReader("{\"qJoMj8qv\":{\"statusCode\":507,\"reason\":\"INSUFFICIENT_STORAGE\",\"entity\":\"LIMIT_MAX_CONTENT_SIZE\"}}"), ResourceCreationResponseEntries.class);
        assertTrue(entries.containsKey("qJoMj8qv"));
    }

    @Test
    public void testInvalidName() throws Exception {
        final ResourceCreationResponseEntries entries = new JSON().getContext(ResourceCreationResponseEntries.class).readValue(
                new StringReader("{\"Go5YUk43.\":{\"statusCode\":400,\"reason\":\"paths may not end with a .\"}}"), ResourceCreationResponseEntries.class);
        assertTrue(entries.containsKey("Go5YUk43."));
    }

    @Test
    public void testLimitMaxFolderCount() throws Exception {
        final ResourceCreationResponseEntries entries = new JSON().getContext(ResourceCreationResponseEntries.class).readValue(
                new StringReader("{\n" +
                        "  \"folder_1\": {\n" +
                        "    \"statusCode\": 507,\n" +
                        "    \"reason\": \"INSUFFICIENT_STORAGE\",\n" +
                        "    \"entity\": \"LIMIT_MAX_FOLDER_COUNT,LIMIT_MAX_RESOURCE_COUNT\"\n" +
                        "  }\n" +
                        "}"
                ), ResourceCreationResponseEntries.class);
        assertTrue(entries.containsKey("folder_1"));
        assertEquals("LIMIT_MAX_FOLDER_COUNT,LIMIT_MAX_RESOURCE_COUNT", entries.get("folder_1").getEntity().getError());
        assertEquals("LIMIT_MAX_FOLDER_COUNT. LIMIT_MAX_RESOURCE_COUNT. Please contact your web hosting service provider for assistance.",
                new EueExceptionMappingService().map(new ApiException(entries.get("folder_1").getEntity().getError(), null, entries.get("folder_1").getStatusCode(), null)).getDetail());
    }
}