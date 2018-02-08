package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;

import com.github.sardine.impl.io.ContentLengthInputStream;

public class ContentLengthStatusInputStream extends ContentLengthInputStream {

    private final Integer code;

    public ContentLengthStatusInputStream(final HttpResponse response) throws IOException {
        super(response);
        this.code = response.getStatusLine().getStatusCode();
    }

    public ContentLengthStatusInputStream(final InputStream in, final Long length, final Integer code) {
        super(in, length);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
