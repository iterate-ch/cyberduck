package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import java.util.Date;

import com.joyent.manta.client.MantaMetadata;
import com.joyent.manta.client.MantaObject;
import com.joyent.manta.http.MantaHttpHeaders;

/**
 * Created by tomascelaya on 5/25/17.
 */
public class StubMantaObject implements MantaObject {

    private final String path;

    /**
     * Build a fake MantaObject we can use to verify path behavior.
     *
     * @param path a path which includes the account owner username like "/accountName/public/file.mpg"
     */
    public StubMantaObject(final String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getEtag() {
        return null;
    }

    @Override
    public byte[] getMd5Bytes() {
        return new byte[0];
    }

    @Override
    public Date getLastModifiedTime() {
        return null;
    }

    @Override
    public String getMtime() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public MantaHttpHeaders getHttpHeaders() {
        return null;
    }

    @Override
    public Object getHeader(final String fieldName) {
        return null;
    }

    @Override
    public String getHeaderAsString(final String fieldName) {
        return null;
    }

    @Override
    public MantaMetadata getMetadata() {
        return null;
    }

    @Override
    public String getRequestId() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }
}
