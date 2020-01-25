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

import ch.cyberduck.core.text.NaturalOrderComparator;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractHostCollection extends Collection<Host> implements EditableCollection {
    private static final Logger log = Logger.getLogger(AbstractHostCollection.class);

    private static final AbstractHostCollection EMPTY = new AbstractHostCollection() {
        // Empty
    };

    public static AbstractHostCollection empty() {
        return EMPTY;
    }

    public AbstractHostCollection() {
        super();
    }

    public AbstractHostCollection(final java.util.Collection<Host> c) {
        super(c);
    }

    /**
     * Default ordering using natural order of bookmark name
     */
    public void sort() {
        this.sort(new Comparator<Host>() {
            @Override
            public int compare(final Host o1, final Host o2) {
                return new NaturalOrderComparator().compare(BookmarkNameProvider.toString(o1),
                    BookmarkNameProvider.toString(o2));
            }
        });
    }

    /**
     * A bookmark may be member of multiple groups
     *
     * @return Map of bookmarks grouped by labels
     */
    public Map<String, List<Host>> groups(final HostFilter filter) {
        final Map<String, List<Host>> labels = new HashMap<>();
        for(Host host : this.stream().filter(filter::accept).collect(Collectors.toList())) {
            if(host.getLabels().isEmpty()) {
                final List<Host> list = labels.getOrDefault(StringUtils.EMPTY, new ArrayList<>());
                list.add(host);
                labels.put(StringUtils.EMPTY, list);
            }
            else {
                for(String label : host.getLabels()) {
                    final List<Host> list = labels.getOrDefault(label, new ArrayList<>());
                    list.add(host);
                    labels.put(label, list);
                }
            }
        }
        return labels;
    }

    /**
     * Add new bookmark to the collection
     *
     * @return True if bookmark collection can be extended
     */
    @Override
    public boolean allowsAdd() {
        return true;
    }

    /**
     * Remove a bookmark from the collection
     *
     * @return True if bookmarks can be removed
     */
    @Override
    public boolean allowsDelete() {
        return true;
    }

    /**
     * Edit the bookmark configuration
     *
     * @return True if bookmarks can be edited
     */
    @Override
    public boolean allowsEdit() {
        return true;
    }

    /**
     * Search using comparator
     *
     * @param bookmark Bookmark to find that matches comparison
     */
    public boolean find(final Host bookmark) {
        return this.stream().anyMatch(h -> h.compareTo(bookmark) == 0);
    }

    /**
     * Lookup bookmark by UUID
     *
     * @param uuid Identifier of bookmark
     * @return Null if not found
     */
    public Host lookup(final String uuid) {
        return this.stream().filter(h -> h.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * @param h Bookmark
     * @return User comment for bookmark or null
     */
    public String getComment(final Host h) {
        if(StringUtils.isNotBlank(h.getComment())) {
            return StringUtils.remove(StringUtils.remove(h.getComment(), CharUtils.LF), CharUtils.CR);
        }
        return null;
    }
}
