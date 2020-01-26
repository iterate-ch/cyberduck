package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.AccessDeniedException;

public class FilterHostCollection extends AbstractHostCollection {
    private static final long serialVersionUID = -2154002477046004380L;

    private final AbstractHostCollection source;

    public FilterHostCollection(final AbstractHostCollection source, final HostFilter filter) {
        this.source = source;
        for(final Host bookmark : source) {
            if(filter.accept(bookmark)) {
                this.add(bookmark);
            }
        }
        this.addListener(new ProxyHostCollectionListener(source));
    }

    @Override
    public boolean allowsAdd() {
        return source.allowsAdd();
    }

    @Override
    public boolean allowsDelete() {
        return source.allowsDelete();
    }

    @Override
    public boolean allowsEdit() {
        return source.allowsEdit();
    }

    @Override
    public void save() {
        source.save();
    }

    @Override
    public void load() throws AccessDeniedException {
        source.load();
    }

    private static final class ProxyHostCollectionListener implements CollectionListener<Host> {
        private final AbstractHostCollection source;

        public ProxyHostCollectionListener(final AbstractHostCollection source) {
            this.source = source;
        }

        @Override
        public void collectionLoaded() {
            source.collectionLoaded();
        }

        @Override
        public void collectionItemAdded(final Host item) {
            source.add(item);
        }

        @Override
        public void collectionItemRemoved(final Host item) {
            source.remove(item);
        }

        @Override
        public void collectionItemChanged(final Host item) {
            source.collectionItemChanged(item);
        }
    }
}
