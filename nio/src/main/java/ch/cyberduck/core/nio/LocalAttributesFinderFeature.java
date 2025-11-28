package ch.cyberduck.core.nio;

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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProxyPathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;

public class LocalAttributesFinderFeature implements AttributesFinder, AttributesAdapter<java.nio.file.Path> {

    private final LocalSession session;

    public LocalAttributesFinderFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        return this.toAttributes(session.toPath(file));
    }

    @Override
    public PathAttributes toAttributes(final java.nio.file.Path file) {
        return new ProxyLocalAttributes(LocalFactory.get(file.toString()).attributes());
    }

    private static final class ProxyLocalAttributes extends ProxyPathAttributes {
        private final LocalAttributes proxy;

        public ProxyLocalAttributes(final LocalAttributes proxy) {
            super(new DefaultPathAttributes());
            this.proxy = proxy;
        }

        @Override
        public long getModificationDate() {
            return proxy.getModificationDate();
        }

        @Override
        public long getCreationDate() {
            return proxy.getCreationDate();
        }

        @Override
        public long getAccessedDate() {
            return proxy.getAccessedDate();
        }

        @Override
        public long getSize() {
            return proxy.getSize();
        }

        @Override
        public Permission getPermission() {
            return proxy.getPermission();
        }

        @Override
        public String getOwner() {
            return proxy.getOwner();
        }

        @Override
        public String getGroup() {
            return proxy.getGroup();
        }
    }
}
