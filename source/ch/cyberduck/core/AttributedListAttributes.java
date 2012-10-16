package ch.cyberduck.core;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Container for file listing attributes, such as a sorting comparator and filter
 *
 * @see ch.cyberduck.core.PathFilter
 * @see ch.cyberduck.ui.BrowserComparator
 */
public class AttributedListAttributes<E extends AbstractPath> {

    /**
     * Sort the file listing using this comparator.
     */
    private Comparator<E> comparator;

    /**
     * The filter to apply to the directory listing excluding files from display.
     */
    private PathFilter filter;

    /**
     * Hidden attribute holds a list of hidden files.
     */
    private List<E> hidden = new ArrayList<E>();

    /**
     * The cached version should be superseded
     * with an updated listing.
     */
    private boolean invalid = false;

    /**
     * File listing is not readable; permission issue
     */
    private boolean readable = true;

    /**
     * Initialize with default values
     *
     * @see ch.cyberduck.core.NullComparator
     * @see ch.cyberduck.core.NullPathFilter
     */
    public AttributedListAttributes() {
        this(new NullComparator<E>(), new NullPathFilter());
    }

    /**
     * @param comparator Sorting comparator
     * @param filter     Collection filter
     */
    public AttributedListAttributes(final Comparator<E> comparator, final PathFilter filter) {
        this.comparator = comparator;
        this.filter = filter;
    }

    public Comparator<E> getComparator() {
        return comparator;
    }

    public void setComparator(final Comparator<E> comparator) {
        this.comparator = comparator;
    }

    public PathFilter getFilter() {
        return filter;
    }

    public void setFilter(final PathFilter filter) {
        this.filter = filter;
    }

    /**
     * @param child Hidden element
     */
    public void addHidden(final E child) {
        hidden.add(child);
    }

    /**
     * @return Hidden elements
     */
    public List<E> getHidden() {
        return hidden;
    }

    public void setReadable(final boolean readable) {
        this.readable = readable;
    }

    /**
     * @return True if the readable attribute is set to <code>Boolean.TRUE</code>.
     */
    public boolean isReadable() {
        return readable;
    }

    /**
     * Mark cached listing as superseded
     *
     * @param invalid Flag
     */
    public void setInvalid(final boolean invalid) {
        this.invalid = invalid;
        if(invalid) {
            // Reset readable attribute.
            readable = true;
        }
    }

    /**
     * @return true if the listing should be superseded
     */
    public boolean isInvalid() {
        return invalid;
    }

    public void clear() {
        hidden.clear();
    }
}
