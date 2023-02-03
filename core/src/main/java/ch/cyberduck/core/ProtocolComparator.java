package ch.cyberduck.core;

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

public final class ProtocolComparator implements Comparable<Protocol> {

    private final Protocol protocol;

    public ProtocolComparator(final Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public int compareTo(final Protocol o) {
        final int byType = protocol.getType().compareTo(o.getType());
        if(0 == byType) {
            return protocol.getDescription().compareTo(o.getDescription());
        }
        return byType;
    }
}
