package ch.cyberduck.core.ocs.model;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
<ocs>
    <meta>
        <status>ok</status>
        <statuscode>100</statuscode>
        <message>OK</message>
        <totalitems></totalitems>
        <itemsperpage></itemsperpage>
    </meta>
    <data>
        <id>admin</id>
        <display-name>admin</display-name>
        <email>admin@example.com</email>
    </data>
</ocs>
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public final class User {
    public meta meta;
    public data data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class meta {
        public String status;
        public String statuscode;
        public String message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class data {
        /**
         * User identifier as expected in WebDAV paths
         */
        public String id;
        @JsonProperty("display-name")
        public String displayname;
        public String email;
    }
}
