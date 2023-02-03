package ch.cyberduck.core.ctera.model;

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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;

public class PortalSessionTest {

    @Test
    public void testParse() throws Exception {
        final String response = "{\"$class\":\"PortalSession\",\"username\": \"mountainduck@cterasendbox1.onmicrosoft.com\",\"userRef\": \"objs\\/5986\\/mountainduck\\/PortalUser\\/mountainduck@cterasendbox1.onmicrosoft.com\",\"portalFirstTimeConfiguration\": false,\"enableResellerProvisioning\": true,\"idleTimeout\": 15,\"folder\": \"objs\\/5988\\/mountainduck\\/CloudDrive\\/My Files\\/5986\",\"cloudFileSharingEnabled\": true,\"maxLinkPermission\": \"ReadWrite\",\"disableProjectControls\": false,\"disablePersonalFoldersControls\": false,\"disableSharingFolderControls\": false,\"language\": \"en\",\"remoteIpAddr\": \"83.76.246.174\",\"role\": \"ReadWriteAdmin\",\"isRemoteWipeEnabled\": true,\"enableBilling\": false,\"displayInvitations\": true,\"hidePassword\": true,\"canUploadPortalSoftware\": false,\"canUploadSkin\": false,\"canValidateCurrentLoggedInPassword\": false,\"showSharedByMe\": true,\"showSharedByMyUsers\": true}";
        ObjectMapper mapper = new ObjectMapper();
        final PortalSession api = mapper.readValue(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)), PortalSession.class);
        assertEquals("mountainduck@cterasendbox1.onmicrosoft.com", api.username);
    }
}
