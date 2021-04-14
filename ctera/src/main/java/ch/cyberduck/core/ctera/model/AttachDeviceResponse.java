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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
{
    "$class":"AttachDeviceRespond",
    "deviceName": "Test-device",
    "deviceUID": 00000000000,
    "lastLogin": "2021-02-03T12:03:00",
    "sharedSecret": "********************************"
 }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AttachDeviceResponse {
    public String deviceUID;
    public String sharedSecret;
}
