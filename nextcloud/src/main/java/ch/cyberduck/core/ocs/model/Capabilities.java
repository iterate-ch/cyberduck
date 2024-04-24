package ch.cyberduck.core.ocs.model;

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
        <version>
            <major>17</major>
            <minor>0</minor>
            <micro>2</micro>
            <string>17.0.2</string>
            <edition></edition>
            <extendedSupport></extendedSupport>
        </version>
        <capabilities>
            <core>
                <pollinterval>60</pollinterval>
                <webdav-root>remote.php/webdav</webdav-root>
            </core>
        </capabilities>
    </data>
</ocs>*/
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Capabilities {
    public meta meta;
    public data data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class meta {
        public String status;
        public String statuscode;
        public String message;
        public int itemsperpage;
        public int totalitems;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class data {
        public capabilities capabilities;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class capabilities {
        public core core;
        public files files;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class core {
        @JsonProperty("webdav-root")
        public String webdav;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class files {
        public String locking;
        public String versioning;
    }
}
