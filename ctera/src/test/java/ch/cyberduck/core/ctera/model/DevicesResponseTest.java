package ch.cyberduck.core.ctera.model;

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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;

public class DevicesResponseTest {

    @Test
    public void testParse() throws Exception {
        final String response = "[\n" +
                "    {\n" +
                "        \"$class\": \"ManagedDevice\",\n" +
                "        \"accountStatus\": \"OK\",\n" +
                "        \"backup\": [],\n" +
                "        \"backupEnabled\": true,\n" +
                "        \"baseObjectRef\": \"objs\\/10316\\/mountainduck\\/ManagedDevice\\/matt\",\n" +
                "        \"createDate\": \"2021-06-23T16:17:56\",\n" +
                "        \"deviceDnsName\": \"matt.mountainduck.ctera.me\",\n" +
                "        \"deviceType\": \"DriveConnect\",\n" +
                "        \"disabled\": false,\n" +
                "        \"installedTemplateTimestamp\": \"2021-06-23T16:41:07\",\n" +
                "        \"mac\": \"f018980dedbd\",\n" +
                "        \"modifiedDate\": \"2021-06-23T16:17:56\",\n" +
                "        \"name\": \"matt\",\n" +
                "        \"owner\": \"objs\\/10314\\/mountainduck\\/PortalUser\\/matt\",\n" +
                "        \"portal\": \"objs\\/10295\\/\\/TeamPortal\\/mountainduck\",\n" +
                "        \"remoteAccessUrl\": \"https:\\/\\/mountainduck.ctera.me\\/device\\/devices\\/matt\",\n" +
                "        \"sharedSecret\": \"*****DON'T CHANGE*****\",\n" +
                "        \"simplifiedConnectMode\": false,\n" +
                "        \"template\": \"objs\\/-1\",\n" +
                "        \"transientDevice\": false,\n" +
                "        \"uid\": 10316,\n" +
                "        \"wipeState\": \"normal\",\n" +
                "        \"zones\": {\n" +
                "            \"$class\": \"ZonesForDevice\",\n" +
                "            \"topZones\": [],\n" +
                "            \"totalZonesCount\": 0\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"$class\": \"ManagedDevice\",\n" +
                "        \"accountStatus\": \"OK\",\n" +
                "        \"backup\": [],\n" +
                "        \"backupEnabled\": true,\n" +
                "        \"baseObjectRef\": \"objs\\/11221\\/mountainduck\\/ManagedDevice\\/mountainduck10\",\n" +
                "        \"createDate\": \"2022-06-21T16:48:28\",\n" +
                "        \"deviceDnsName\": \"mountainduck10.mountainduck.ctera.me\",\n" +
                "        \"deviceType\": \"DriveConnect\",\n" +
                "        \"disabled\": false,\n" +
                "        \"installedTemplateTimestamp\": \"2022-07-05T06:00:34\",\n" +
                "        \"mac\": \"f01898b19975\",\n" +
                "        \"modifiedDate\": \"2022-06-21T16:48:28\",\n" +
                "        \"name\": \"mountainduck10\",\n" +
                "        \"owner\": \"objs\\/10303\\/mountainduck\\/PortalUser\\/mountainduck\",\n" +
                "        \"portal\": \"objs\\/10295\\/\\/TeamPortal\\/mountainduck\",\n" +
                "        \"remoteAccessUrl\": \"https:\\/\\/mountainduck.ctera.me\\/device\\/devices\\/mountainduck10\",\n" +
                "        \"sharedSecret\": \"*****DON'T CHANGE*****\",\n" +
                "        \"simplifiedConnectMode\": false,\n" +
                "        \"template\": \"objs\\/-1\",\n" +
                "        \"transientDevice\": false,\n" +
                "        \"uid\": 11221,\n" +
                "        \"wipeState\": \"normal\",\n" +
                "        \"zones\": {\n" +
                "            \"$class\": \"ZonesForDevice\",\n" +
                "            \"topZones\": [],\n" +
                "            \"totalZonesCount\": 0\n" +
                "        }\n" +
                "    }\n" +
                "]";
        ObjectMapper mapper = new ObjectMapper();
        final Device[] devices = mapper.readValue(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)), Device[].class);
        assertEquals(2, devices.length);
        assertEquals("10316", devices[0].uid);
        assertEquals("matt", devices[0].name);
        assertEquals("11221", devices[1].uid);
        assertEquals("mountainduck10", devices[1].name);
    }
}