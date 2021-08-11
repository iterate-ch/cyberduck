package ch.cyberduck.core.freenet;

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
 "cid": 720497664,
 "hash": "eyJhbGciOiJIUzUxMi...",
 "urls": {
     "check": "https://webmail.freenet.de/api/v2.0/hash/check/eyJhbGciOiJIUzUxMi.."
     "login": "https://webmail.freenet.de/api/v2.0/hash/login/eyJhbGciOiJIUzUxMi.."
  }
}
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FreenetTemporaryLoginResponse {
    public String cid;
    public String hash;
    public FreenetTemporaryLoginUrlsResponse urls;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public final class FreenetTemporaryLoginUrlsResponse {
        public String check;
        public String login;
    }
}

