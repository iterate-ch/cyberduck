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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.WebUrlProvider;

import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
public class FreenetProtocol extends AbstractProtocol {

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.implicit;
    }

    @Override
    public Type getType() {
        return Type.freenet;
    }

    @Override
    public String getIdentifier() {
        return Type.freenet.name();
    }

    @Override
    public String getDescription() {
        return "Freenet";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == WebUrlProvider.class) {
            return (T) new FreenetAuthenticatedUrlProvider();
        }
        return super.getFeature(type);
    }
}
