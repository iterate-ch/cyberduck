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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class HistoryCollection extends AbstractFolderHostCollection {
    private static final long serialVersionUID = 2270155702956300755L;

    private static final HistoryCollection HISTORY_COLLECTION = new HistoryCollection(
            LocalFactory.get(PreferencesFactory.get().getProperty("application.support.path"), "History")
    );

    public HistoryCollection(final Local f) {
        super(f);
    }

    /**
     * @return Singleton instance
     */
    public static HistoryCollection defaultCollection() {
        return HISTORY_COLLECTION;
    }

    @Override
    public Local getFile(final Host bookmark) {
        return LocalFactory.get(folder, String.format("%s.duck",
                StringUtils.replace(BookmarkNameProvider.toString(bookmark), "/", ":")));
    }

    @Override
    public String getComment(final Host host) {
        Date timestamp = host.getTimestamp();
        if(null != timestamp) {
            // Set comment to timestamp when server was last accessed
            return UserDateFormatterFactory.get().getLongFormat(timestamp.getTime());
        }
        // There might be files from previous versions that have no timestamp yet.
        return null;
    }

    /**
     * Does not allow duplicate entries.
     *
     * @param row      Row number
     * @param bookmark Bookmark
     */
    @Override
    public void add(final int row, final Host bookmark) {
        if(this.contains(bookmark)) {
            this.remove(bookmark);
        }
        super.add(row, bookmark);
    }

    /**
     * Does not allow duplicate entries.
     *
     * @param bookmark Bookmark
     * @return Always true
     */
    @Override
    public boolean add(final Host bookmark) {
        if(this.contains(bookmark)) {
            this.remove(bookmark);
        }
        return super.add(bookmark);
    }

    /**
     * Sort by timestamp of bookmark file.
     */
    @Override
    protected synchronized void sort() {
        Collections.sort(this, new Comparator<Host>() {
            @Override
            public int compare(Host o1, Host o2) {
                if(null == o1.getTimestamp() && null == o2.getTimestamp()) {
                    return 0;
                }
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
