package ch.cyberduck.core.gmxcloud;/*
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

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;

public class Util {

    private Util() {
    }

    public static String getResourceIdFromResourceUri(String uri) {
        final String[] split = StringUtils.split(uri, "/");
        return split[split.length - 1];
    }

    public static byte[] intToBytes(final int i, final int capacity) {
        final ByteBuffer bb = ByteBuffer.allocate(capacity);
        bb.putInt(i);
        return bb.array();
    }

}
