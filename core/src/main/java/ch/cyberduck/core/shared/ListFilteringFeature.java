package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CaseInsensitivePathPredicate;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public abstract class ListFilteringFeature {

    private final Session<?> session;

    private Cache<Path> cache
            = PathCache.empty();

    public ListFilteringFeature(final Session<?> session) {
        this.session = session;
    }

    protected Path search(final Path file) throws BackgroundException {
        final AttributedList<Path> list;
        if(!cache.isCached(file.getParent())) {
            list = session.list(file.getParent(), new DisabledListProgressListener());
            cache.put(file.getParent(), list);
        }
        else {
            list = cache.get(file.getParent());
        }
        final Predicate<Path> predicate;
        final Predicate<Path> simple = session.getCase() == Session.Case.insensitive ? new CaseInsensitivePathPredicate(file) : new SimplePathPredicate(file);
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            // Look for exact match
            predicate = new PredicateChain<Path>(
                    simple, new DefaultPathPredicate(file));
        }
        else {
            predicate = simple;
        }
        return list.filter(new NullFilter<>()).find(predicate);
    }

    public ListFilteringFeature withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

    private final class PredicateChain<T> implements Predicate<T> {
        private final Predicate<T> predicates[];

        @SafeVarargs
        private PredicateChain(final Predicate<T>... predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean test(final T t) {
            for(Predicate<T> p : predicates) {
                if(!p.test(t)) {
                    return false;
                }
            }
            return true;
        }
    }
}
