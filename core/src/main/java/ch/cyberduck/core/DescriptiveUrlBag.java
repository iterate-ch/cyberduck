package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import java.util.Iterator;
import java.util.LinkedHashSet;

public class DescriptiveUrlBag extends LinkedHashSet<DescriptiveUrl> {
    private static final long serialVersionUID = 6056908616987839368L;

    public DescriptiveUrlBag() {
        //
    }

    public DescriptiveUrlBag(final java.util.Collection<? extends DescriptiveUrl> c) {
        super(c);
    }

    public static DescriptiveUrlBag empty() {
        return new DescriptiveUrlBag();
    }

    @Override
    public boolean add(final DescriptiveUrl url) {
        if(url.equals(DescriptiveUrl.EMPTY)) {
            return false;
        }
        return super.add(url);
    }

    /**
     * @param types Include
     * @return Copy of modified list
     */
    public DescriptiveUrlBag filter(DescriptiveUrl.Type... types) {
        final DescriptiveUrlBag filtered = new DescriptiveUrlBag(this);
        for(Iterator<DescriptiveUrl> iter = filtered.iterator(); iter.hasNext(); ) {
            final DescriptiveUrl url = iter.next();
            boolean remove = true;
            for(DescriptiveUrl.Type type : types) {
                if(url.getType().equals(type)) {
                    remove = false;
                    break;
                }
            }
            if(remove) {
                iter.remove();
            }
        }
        return filtered;
    }

    /**
     * @return Empty if no such type
     * @see DescriptiveUrl#EMPTY
     */
    public DescriptiveUrl find(DescriptiveUrl.Type type) {
        for(DescriptiveUrl url : this) {
            if(url.getType().equals(type)) {
                return url;
            }
        }
        return DescriptiveUrl.EMPTY;
    }
}
