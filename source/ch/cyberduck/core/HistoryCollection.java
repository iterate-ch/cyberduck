package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.cyberduck.ui.DateFormatterFactory;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * @version $Id$
 */
public class HistoryCollection extends AbstractFolderHostCollection {
    private static Logger log = Logger.getLogger(HistoryCollection.class);

    private static HistoryCollection HISTORY_COLLECTION = new HistoryCollection(
            LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "History")
    );

    public HistoryCollection(Local f) {
        super(f);
    }

    /**
     * @return
     */
    public static HistoryCollection defaultCollection() {
        return HISTORY_COLLECTION;
    }

    @Override
    public Local getFile(Host bookmark) {
        return LocalFactory.createLocal(folder, bookmark.getNickname() + ".duck");
    }

    @Override
    public String getComment(Host host) {
        Date timestamp = host.getTimestamp();
        if(null != timestamp) {
            // Set comment to timestamp when server was last accessed
            return DateFormatterFactory.instance().getLongFormat(timestamp.getTime());
        }
        // There might be files from previous versions that have no timestamp yet.
        return null;
    }

    /**
     * Does not allow duplicate entries.
     *
     * @param row
     * @param bookmark
     */
    @Override
    public void add(int row, Host bookmark) {
        if(this.contains(bookmark)) {
            this.remove(bookmark);
        }
        super.add(row, bookmark);
    }

    /**
     * Does not allow duplicate entries.
     *
     * @param bookmark
     * @return
     */
    @Override
    public boolean add(Host bookmark) {
        if(this.contains(bookmark)) {
            this.remove(bookmark);
        }
        return super.add(bookmark);
    }

    /**
     * Sort by timestamp of bookmark file.
     */
    @Override
    protected void sort() {
        Collections.sort(this, new Comparator<Host>() {
            public int compare(Host o1, Host o2) {
                if(null == o1.getTimestamp()) {
                    return 1;
                }
                if(null == o2.getTimestamp()) {
                    return -1;
                }
                return -o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
    }

    /**
     * Does not allow manual additions
     *
     * @return False
     */
    @Override
    public boolean allowsAdd() {
        return false;
    }

    /**
     * Does not allow editing entries
     *
     * @return False
     */
    @Override
    public boolean allowsEdit() {
        return false;
    }
}