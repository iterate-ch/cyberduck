package ch.cyberduck.ui.dnd;

/*
 *  ch.cyberduck.ui.dnd.StringTransferable.java
 *  Cyberduck
 *
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://dewww.epfl.ch/~dkocher/
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
 *  dkocher@mac.com
 */

import java.awt.datatransfer.StringSelection;
    
public class StringTransferable extends StringSelection {
    
    public StringTransferable(String s) {
        super(s);
    }
}
