package ch.cyberduck.ui.model;

/*
 *  ch.cyberduck.ui.model.ExtendedTableModel.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import javax.swing.table.TableModel;
import java.util.Iterator;
import java.util.List;

/**
* @version $Id$
 */
public interface ExtendedTableModel extends TableModel {
    
    public void sort(int columnIndex, boolean ascending);
    public boolean contains(Object o);
    public void setData(List data);
    public void addEntry(Object newEntry);
    public void addEntry(Object newEntry, int atRow);
    public Object getEntry(int row);
    public void clear();
    public Iterator iterator();
}
