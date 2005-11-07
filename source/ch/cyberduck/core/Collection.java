package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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


import java.util.ArrayList;

/**
 * @version $Id$
 */
public class Collection extends ArrayList {

    public int indexOf(Object elem) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).equals(elem))
                return i;
        }
        return -1;
    }

    public int lastIndexOf(Object elem) {
        for (int i = this.size() - 1; i >= 0; i--) {
            if (this.get(i).equals(elem))
                return i;
        }
        return -1;
    }

}
