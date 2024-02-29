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

import ch.cyberduck.core.text.DefaultLexicographicOrderComparator;
import ch.cyberduck.core.text.NaturalOrderComparator;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractHostCollection extends Collection<Host> implements EditableCollection {

    public static final Comparator<Host> SORT_BY_NICKNAME = new Comparator<Host>() {
        @Override
        public int compare(Host o1, Host o2) {
            return new NaturalOrderComparator().compare(
                    BookmarkNameProvider.toString(o1), BookmarkNameProvider.toString(o2)
            );
        }
    };

    public static final Comparator<Host> SORT_BY_HOSTNAME = new Comparator<Host>() {
        @Override
        public int compare(Host o1, Host o2) {
            return new DefaultLexicographicOrderComparator().compare(o1.getHostname(), o2.getHostname());
        }
    };

    public static final Comparator<Host> SORT_BY_PROTOCOL = new Comparator<Host>() {
        @Override
        public int compare(Host o1, Host o2) {
            return new DefaultLexicographicOrderComparator().compare(o1.getProtocol().getIdentifier(), o2.getProtocol().getIdentifier());
        }
    };

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
        this.sort(SORT_BY_NICKNAME);
    }

    public Map<String, List<Host>> groups() {
        return this.groups(HostFilter.NONE);
    }

    public Map<String, List<Host>> groups(final HostFilter filter) {
        return this.groups(HostGroups.LABELS, filter);
    }

    /**
     * A bookmark may be member of multiple groups
     *
     * @param groups Defines group criteria for host
     * @param filter Filter to apply to result set
     * @return Map of bookmarks grouped by labels
     */
    public Map<String, List<Host>> groups(final HostGroups groups, final HostFilter filter) {
        return this.groups(groups, filter, SORT_BY_NICKNAME);
    }

    public Map<String, List<Host>> groups(final HostGroups groups, final HostFilter filter, final Comparator<Host> comparator) {
        final Map<String, List<Host>> labels = new TreeMap<>(new NaturalOrderComparator());
        for(Host host : this.stream().filter(filter::accept).collect(Collectors.toList())) {
            if(groups.groups(host).isEmpty()) {
                final List<Host> list = labels.getOrDefault(StringUtils.EMPTY, new ArrayList<>());
                list.add(host);
                labels.put(StringUtils.EMPTY, list);
            }
            else {
                for(String label : groups.groups(host)) {
                    final List<Host> list = labels.getOrDefault(label, new ArrayList<>());
                    list.add(host);
                    labels.put(label, list);
                }
            }
        }
        labels.forEach((String label, List<Host> hosts) -> hosts.sort(comparator));
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
     * Find matching bookmark with fuzzy logic
     *
     * @param input Search
     * @return Matching optional bookmark in collection
     */
    public Optional<Host> find(final Host input) {
        // Iterate over all bookmarks trying exact match
        return Optional.ofNullable(this.find(new HostComparePredicate(input))
                .orElse(this.find(new DefaultPathPredicate(input))
                        .orElse(this.find(new ProfilePredicate(input))
                                .orElse(this.find(new ProtocolIdentifierPredicate(input)).orElse(null)))));
    }

    public Optional<Host> find(final Predicate<Host> predicate) {
        return this.stream().filter(predicate).findAny();
    }

    public static final class HostComparePredicate implements Predicate<Host> {
        private final Host input;

        public HostComparePredicate(final Host input) {
            this.input = input;
        }

        @Override
        public boolean test(final Host h) {
            return h.compareTo(input) == 0;
        }
    }

    public static class HostnamePredicate implements Predicate<Host> {
        private final Host input;

        private HostnamePredicate(final Host input) {
            this.input = input;
        }

        @Override
        public boolean test(final Host h) {
            if(StringUtils.isNotBlank(input.getCredentials().getUsername())) {
                if(!Objects.equals(h.getCredentials().getUsername(), input.getCredentials().getUsername())) {
                    return false;
                }
            }
            return Objects.equals(h.getHostname(), input.getHostname());
        }
    }

    public static final class DefaultPathPredicate extends HostnamePredicate {
        private final Host input;

        private DefaultPathPredicate(final Host input) {
            super(input);
            this.input = input;
        }

        @Override
        public boolean test(final Host h) {
            return super.test(h) &&
                    StringUtils.isNotBlank(input.getDefaultPath()) &&
                    StringUtils.startsWith(input.getDefaultPath(), h.getDefaultPath());
        }
    }

    /**
     * Tests for same protocol, hostname and username
     */
    public static final class ProfilePredicate extends HostnamePredicate {
        private final Host input;

        public ProfilePredicate(final Host input) {
            super(input);
            this.input = input;
        }

        @Override
        public boolean test(final Host h) {
            return super.test(h) && Objects.equals(h.getProtocol(), input.getProtocol());
        }
    }

    public static final class ProtocolIdentifierPredicate extends HostnamePredicate {
        private final Host input;

        public ProtocolIdentifierPredicate(final Host input) {
            super(input);
            this.input = input;
        }

        @Override
        public boolean test(final Host h) {
            return super.test(h) && Objects.equals(h.getProtocol().getIdentifier(), input.getProtocol().getIdentifier());
        }
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
