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

/*
<ocs>
 <meta>
  <status>ok</status>
  <statuscode>200</statuscode>
  <message>OK</message>
 </meta>
 <data>
  <id>36</id>
  <share_type>3</share_type>
  <uid_owner>dkocher</uid_owner>
  <displayname_owner>David Kocher</displayname_owner>
  <permissions>1</permissions>
  <stime>1559218292</stime>
  <parent/>
  <expiration/>
  <token>79NKo6JxmsxxGBb</token>
  <uid_file_owner>dkocher</uid_file_owner>
  <note></note>
  <label></label>
  <displayname_file_owner>David Kocher</displayname_file_owner>
  <path>/sandbox/example.png</path>
  <item_type>file</item_type>
  <mimetype>image/png</mimetype>
  <storage_id>home::dkocher</storage_id>
  <storage>3</storage>
  <item_source>36285</item_source>
  <file_source>36285</file_source>
  <file_parent>36275</file_parent>
  <file_target>/Monte Panarotta.png</file_target>
  <share_with/>
  <share_with_displayname/>
  <password/>
  <send_password_by_talk></send_password_by_talk>
  <url>https://example.net/s/67hgsdfjkds67</url>
  <mail_send>1</mail_send>
  <hide_download>0</hide_download>
 </data>
</ocs>
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Share {
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
        public String id;
        public String url;
        public user[] users;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class users {
        public user[] element;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class user {
        public String label;
        public String icon;
        public String shareWithDisplayNameUnique;
        public value value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class value {
        public int shareType;
        public String shareWith;
        public String shareWithAdditionalInfo;
    }
}
