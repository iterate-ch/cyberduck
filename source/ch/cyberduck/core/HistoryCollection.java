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

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.io.IOException;

/**
 * @version $Id:$
 */
public class HistoryCollection extends HostCollection {
    private static Logger log = Logger.getLogger(HistoryCollection.class);

    private static HistoryCollection HISTORY_COLLECTION = new HistoryCollection(
            new Local(Preferences.instance().getProperty("application.support.path"), "History")
    );

    /**
     *
     * @return
     */
    public static Collection defaultCollection() {
        return HISTORY_COLLECTION;
    }

    /**
     * Reading bookmarks from this folder
     *
     * @param folder Parent directory to look for bookmarks
     */
    public HistoryCollection(Local folder) {
        super(folder);
        folder.mkdir(true);
    }

    public synchronized void add(int row, Object bookmark) {
        final Host h = (Host) bookmark;
        h.setFile(new Local(file, h.getNickname() + ".duck"));
        try {
            h.write();
        }
        catch(IOException e) {
            log.error(e.getMessage());
            return;
        }
        if(this.contains(bookmark)) {
            super.add(row, h);
        }
        else {
            this.sort();
        }
    }

    /**
     * @param row
     * @return the element that was removed from the list.
     */
    public synchronized Object remove(int row) {
        final Host bookmark = (Host) this.get(row);
        bookmark.getFile().delete();
        return super.remove(row);
    }

    protected void load() {
        log.info("Reloading " + file);
        final AttributedList bookmarks = file.childs(new NullComparator(),
                new PathFilter() {
                    public boolean accept(AbstractPath file) {
                        return file.getName().endsWith(".duck");
                    }
                }
        );
        for(Iterator iter = bookmarks.iterator(); iter.hasNext();) {
            try {
                this.add(new Host((Local) iter.next()));
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    protected void sort() {
        Collections.sort(this, new Comparator() {
            public int compare(Object o1, Object o2) {
                Local f1 = ((Host) o1).getFile();
                Local f2 = ((Host) o2).getFile();
                if(f1.attributes.getModificationDate() < f2.attributes.getModificationDate()) {
                    return 1;
                }
                if(f1.attributes.getModificationDate() > f2.attributes.getModificationDate()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    public synchronized void clear() {
        log.debug("Removing all bookmarks from " + file);
        for(Iterator iter = this.iterator(); iter.hasNext();) {
            ((Host) iter.next()).getFile().delete();
        }
        super.clear();
    }

    public void save() {
        // Do not save collection
    }

    protected Host unique(Host bookmark) {
        return bookmark;
    }
}