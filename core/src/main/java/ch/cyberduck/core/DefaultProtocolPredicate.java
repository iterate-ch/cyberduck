package ch.cyberduck.core;

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

import java.util.EnumSet;
import java.util.function.Predicate;

public class DefaultProtocolPredicate implements Predicate<Protocol> {
    private final EnumSet<Protocol.Type> types;

    public DefaultProtocolPredicate(final EnumSet<Protocol.Type> types) {
        this.types = types;
    }

    @Override
    public boolean test(final Protocol protocol) {
        if(types.contains(protocol.getType())) {
            return protocol.isEnabled() && protocol.isBundled();
        }
        return false;
    }
}
