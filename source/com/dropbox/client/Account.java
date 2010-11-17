package com.dropbox.client;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 *
 * Derived from Official Dropbox API client for Java.
 * http://bitbucket.org/dropboxapi/dropbox-client-java
 */

import org.json.simple.JSONObject;

import java.util.Map;

/**
 * @version $Id$
 */
public class Account extends AbstractResponse {
    private String country;
    private String displayName;
    private long quotaQuota;
    private long quotaNormal;
    private long quotaShared;
    private long uid;

    public Account(JSONObject map) {
        country = map.get("country").toString();
        displayName = map.get("display_name").toString();
        uid = this.getLong(map, "uid");
        Object quotaInfo = map.get("quota_info");
        if(quotaInfo != null && quotaInfo instanceof Map) {
            Map quotamap = (Map) quotaInfo;
            quotaQuota = this.getLong(quotamap, "quota");
            quotaNormal = this.getLong(quotamap, "normal");
            quotaShared = this.getLong(quotamap, "shared");
        }
    }

    public String getCountry() {
        return country;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getQuotaQuota() {
        return quotaQuota;
    }

    public long getQuotaNormal() {
        return quotaNormal;
    }

    public long getQuotaShared() {
        return quotaShared;
    }

    public long getUid() {
        return uid;
    }
}
