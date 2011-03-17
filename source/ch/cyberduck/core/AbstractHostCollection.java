package ch.cyberduck.core;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class AbstractHostCollection extends Collection<Host> implements EditableCollection {
    private static Logger log = Logger.getLogger(AbstractHostCollection.class);

    private static final AbstractHostCollection EMPTY = new AbstractHostCollection() {
        @Override
        public String getName() {
            return Locale.localizedString("None");
        }
    };

    public static AbstractHostCollection empty() {
        return EMPTY;
    }

    public AbstractHostCollection() {
        super();
    }

    public AbstractHostCollection(java.util.Collection<Host> c) {
        super(c);
    }

    /**
     * @return Group label
     */
    public abstract String getName();

    /**
     *
     * @param h
     * @return
     */
    public String getComment(Host h) {
        if(StringUtils.isNotBlank(h.getComment())) {
            return StringUtils.remove(StringUtils.remove(h.getComment(), CharUtils.LF), CharUtils.CR);
        }
        return null;
    }

    @Override
    public boolean addAll(java.util.Collection<? extends Host> c) {
        List<Host> temporary = new ArrayList<Host>();
        for(Host host : c) {
            if(temporary.contains(host)) {
                log.warn("Reset UUID of duplicate in collection:" + host);
                host.setUuid(null);
            }
            temporary.add(host);
        }
        return super.addAll(temporary);
    }

    @Override
    public boolean add(Host host) {
        if(this.contains(host)) {
            log.warn("Reset UUID of duplicate in collection:" + host);
            host.setUuid(null);
        }
        return super.add(host);
    }

    @Override
    public void add(int row, Host host) {
        if(this.contains(host)) {
            log.warn("Reset UUID of duplicate in collection:" + host);
            host.setUuid(null);
        }
        super.add(row, host);
    }

    @Override
    public void collectionItemAdded(Host item) {
        this.sort();
        super.collectionItemAdded(item);
    }

    @Override
    public void collectionItemRemoved(Host item) {
        this.sort();
        super.collectionItemRemoved(item);
    }

    protected void sort() {
        ;//
    }

    /**
     * Lookup bookmark by UUID
     *
     * @param uuid Identifier of bookmark
     * @return Null if not found
     */
    public Host lookup(String uuid) {
        for(Host bookmark : this) {
            if(bookmark.getUuid().equals(uuid)) {
                return bookmark;
            }
        }
        return null;
    }

    /**
     * Add new bookmark to the collection
     *
     * @return
     */
    public boolean allowsAdd() {
        return true;
    }

    /**
     * Remove a bookmark from the collection
     *
     * @return
     */
    public boolean allowsDelete() {
        return true;
    }

    /**
     * Edit the bookmark configuration
     *
     * @return
     */
    public boolean allowsEdit() {
        return true;
    }

    public void save() {
        ; // Not persistent by default
    }

    public void load(Collection<Host> c) {
        this.addAll(c);
        this.collectionLoaded();
    }
}
