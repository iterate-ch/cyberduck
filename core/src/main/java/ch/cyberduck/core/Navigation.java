package ch.cyberduck.core;

/*
 *  Copyright (c) 2013 David Kocher. All rights reserved.
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

import java.util.List;

public class Navigation {

    /**
     * Keeps a ordered backward history of previously visited paths
     */
    private final List<Path> back = new Collection<Path>();

    /**
     * Keeps a ordered forward history of previously visited paths
     */
    private final List<Path> forward = new Collection<Path>();

    /**
     * @param p Directory
     */
    public void add(final Path p) {
        if(back.size() > 0) {
            // Do not add if this was a reload
            if(p.equals(back.get(back.size() - 1))) {
                return;
            }
        }
        back.add(p);
    }

    /**
     * Returns the prevously browsed path and moves it to the forward history
     *
     * @return The previously browsed path or null if there is none
     */
    public Path back() {
        int size = back.size();
        if(size > 1) {
            forward.add(back.get(size - 1));
            Path p = back.get(size - 2);
            //delete the fetched path - otherwise we produce a loop
            back.remove(size - 1);
            back.remove(size - 2);
            return p;
        }
        else if(1 == size) {
            forward.add(back.get(size - 1));
            return back.get(size - 1);
        }
        return null;
    }

    /**
     * @return The last path browsed before #back was called
     * @see #back()
     */
    public Path forward() {
        int size = forward.size();
        if(size > 0) {
            Path p = forward.get(size - 1);
            forward.remove(size - 1);
            return p;
        }
        return null;
    }

    /**
     * @return The ordered array of previously visited directories
     */
    public List<Path> getBack() {
        return back;
    }

    /**
     * @return The ordered array of previously visited directories
     */
    public List<Path> getForward() {
        return forward;
    }

    public void clear() {
        back.clear();
        forward.clear();
    }
}
