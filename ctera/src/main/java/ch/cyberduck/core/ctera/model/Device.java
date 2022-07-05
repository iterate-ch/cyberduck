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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
    {
        "$class": "ManagedDevice",
        "accountStatus": "OK",
        "backup": [],
        "backupEnabled": true,
        "baseObjectRef": "objs\/10316\/mountainduck\/ManagedDevice\/matt",
        "createDate": "2021-06-23T16:17:56",
        "deviceDnsName": "matt.mountainduck.ctera.me",
        "deviceType": "DriveConnect",
        "disabled": false,
        "installedTemplateTimestamp": "2021-06-23T16:41:07",
        "mac": "f018980dedbd",
        "modifiedDate": "2021-06-23T16:17:56",
        "name": "matt",
        "owner": "objs\/10314\/mountainduck\/PortalUser\/matt",
        "portal": "objs\/10295\/\/TeamPortal\/mountainduck",
        "remoteAccessUrl": "https:\/\/mountainduck.ctera.me\/device\/devices\/matt",
        "sharedSecret": "*****DON'T CHANGE*****",
        "simplifiedConnectMode": false,
        "template": "objs\/-1",
        "transientDevice": false,
        "uid": 10316,
        "wipeState": "normal",
        "zones": {
            "$class": "ZonesForDevice",
            "topZones": [],
            "totalZonesCount": 0
        }
    }
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Device {
    public String uid;
    public String name;
}