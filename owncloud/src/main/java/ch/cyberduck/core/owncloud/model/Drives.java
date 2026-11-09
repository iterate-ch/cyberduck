package ch.cyberduck.core.owncloud.model;

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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
{
  "value": [
    {
      "driveAlias": "personal/jane",
      "driveType": "personal",
      "id": "storage-users-1$00000000-1111-2222-3333-444444444444",
      "name": "Jane Doe",
      "root": {
        "id": "storage-users-1$00000000-1111-2222-3333-444444444444",
        "webDavUrl": "https://example.com/dav/spaces/storage-users-1$00000000-1111-2222-3333-444444444444"
      }
    }
  ]
}
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Drives {
    public List<Drive> value;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Drive {
        public String id;
        public String name;
        public String driveType;
        public Root root;

        @Override
        public String toString() {
            final String sb = "Drive{" + "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", driveType='" + driveType + '\'' +
                    '}';
            return sb;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Root {
        public String id;
        public String webDavUrl;
        /**
         * Set for disabled space
         */
        public Deleted deleted;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Deleted {
        public String state;
    }
}
