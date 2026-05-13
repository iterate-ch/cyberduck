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

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

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
        return new ProxyLocalAttributes(new LazyInitializer<LocalAttributes>() {
            @Override
            protected LocalAttributes initialize() {
                return LocalFactory.get(file.toString()).attributes();
            }
        });
    }

    private static final class ProxyLocalAttributes extends ProxyPathAttributes {
        private final LazyInitializer<LocalAttributes> proxy;

        public ProxyLocalAttributes(final LazyInitializer<LocalAttributes> proxy) {
            super(new DefaultPathAttributes());
            this.proxy = proxy;
        }

        @Override
        public long getModificationDate() {
            try {
                return proxy.get().getModificationDate();
            }
            catch(ConcurrentException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long getCreationDate() {
            try {
                return proxy.get().getCreationDate();
            }
            catch(ConcurrentException e) {
                return -1L;
            }
        }

        @Override
        public long getAccessedDate() {
            try {
                return proxy.get().getAccessedDate();
            }
            catch(ConcurrentException e) {
                return -1L;
            }
        }

        @Override
        public long getSize() {
            try {
                return proxy.get().getSize();
            }
            catch(ConcurrentException e) {
                return -1L;
            }
        }

        @Override
        public Permission getPermission() {
            try {
                return proxy.get().getPermission();
            }
            catch(ConcurrentException e) {
                return Permission.EMPTY;
            }
        }

        @Override
        public String getOwner() {
            try {
                return proxy.get().getOwner();
            }
            catch(ConcurrentException e) {
                return null;
            }
        }

        @Override
        public String getGroup() {
            try {
                return proxy.get().getGroup();
            }
            catch(ConcurrentException e) {
                return null;
            }
        }
    }
}
