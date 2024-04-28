package ch.cyberduck.core.ocs;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;

public class OcsCapabilitiesRequest extends HttpGet {

    public OcsCapabilitiesRequest(final Host host) {
        super(new StringBuilder(String.format("https://%s/ocs/v1.php/cloud/capabilities",
                host.getHostname()
        )).toString());
        this.setHeader("OCS-APIRequest", "true");
        this.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
    }
}
