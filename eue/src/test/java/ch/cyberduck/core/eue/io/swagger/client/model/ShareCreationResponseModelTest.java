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

import ch.cyberduck.core.eue.io.swagger.client.JSON;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class ShareCreationResponseModelTest {

    @Test
    public void testInvalidPin() throws Exception {
        final ShareCreationResponseModel entries = new JSON().getContext(ShareCreationResponseModel.class).readValue(
                new StringReader("{\"!ano\":{\"statusCode\":400,\"reason\":\"PRECONDITION_FAILED\",\"entity\":\"Pin does not match pin policy\"}}"), ShareCreationResponseModel.class);
        assertTrue(entries.containsKey("!ano"));
        assertEquals(400, entries.get("!ano").getStatusCode(), 0);
    }

    @Test
    public void testSuccess() throws Exception {
        final ShareCreationResponseModel entries = new JSON().getContext(ShareCreationResponseModel.class).readValue(
                new StringReader("{\"!ano\":{\"statusCode\":201,\"reason\":\"Created\",\"entity\":{\"resourceURI\":\".\",\"shareURI\":\"share/nUOEA68XRlu2UVwe3cVsog\",\"creationMillis\":1634502683071,\"guestEMail\":\"!ano\",\"pin\":\"*****\",\"unmountable\":true,\"guestURI\":\"../../../../guest/%401015156902205593160/share/nUOEA68XRlu2UVwe3cVsog/resourceAlias/ROOT\",\"name\":\"gXVKL8mr\",\"resource\":{\"ui:fs\":{\"creationMillis\":1634502682547,\"size\":0,\"metaETag\":\"AAABfI_zqgMAAAF8j_Onsw\",\"lastResourceOperation\":\"CREATE\",\"name\":\"VCvAe6bs\",\"resourceURI\":\".\",\"version\":267627180,\"modificationMillis\":1634502683139,\"resourceType\":\"container\",\"lastResourceOperationClient\":\"MAMCLOUDSYNCMAC\",\"parents\":[{\"ui:fs\":{\"resourceURI\":\"../../resourceAlias/ROOT\"}}]}},\"permission\":{\"readable\":true,\"writable\":false,\"deletable\":false,\"notificationEnabled\":false},\"displayName\":\"@1015156902205593160\",\"hasPin\":true}}}"), ShareCreationResponseModel.class);
        assertTrue(entries.containsKey("!ano"));
        assertEquals(201, entries.get("!ano").getStatusCode(), 0);
    }
}