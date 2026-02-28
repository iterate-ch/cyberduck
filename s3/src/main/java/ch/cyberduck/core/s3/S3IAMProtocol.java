package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.Protocol;

import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
public class S3IAMProtocol extends S3Protocol {

    @Override
    public String getIdentifier() {
        return "s3-iam";
    }

    @Override
    public Type getType() {
        return Type.s3;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "s3");
    }
}
