package ch.cyberduck.core.owncloud;

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

import ch.cyberduck.core.owncloud.model.Drives;

import org.apache.http.HttpEntity;
import org.apache.http.impl.client.AbstractResponseHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OwncloudDrivesResponseHandler extends AbstractResponseHandler<List<Drives.Drive>> {

    @Override
    public List<Drives.Drive> handleEntity(final HttpEntity entity) throws IOException {
        final Drives drives = new ObjectMapper().readValue(entity.getContent(), Drives.class);
        if(null == drives.value) {
            return Collections.emptyList();
        }
        return drives.value;
    }
}
