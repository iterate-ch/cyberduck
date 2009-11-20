package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.PathFilter;
import ch.cyberduck.ui.cocoa.application.NSComboBox;
import ch.cyberduck.ui.cocoa.application.NSImageView;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.Comparator;

/**
 * @version $Id$
 */
public class CDGotoController extends CDSheetController {
    private static Logger log = Logger.getLogger(CDGotoController.class);

    @Outlet
    protected NSImageView iconView;

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
        this.iconView.setImage(CDIconCache.folderIcon(64));
    }

    @Outlet
    private NSComboBox folderCombobox;
    private ProxyController folderComboboxModel = new FolderComboboxModel();

    public void setFolderCombobox(NSComboBox folderCombobox) {
        this.folderCombobox = folderCombobox;
        this.folderCombobox.setCompletes(true);
        this.folderCombobox.setUsesDataSource(true);
        this.folderCombobox.setDataSource(folderComboboxModel.id());
        this.folderCombobox.setStringValue(((CDBrowserController) this.parent).workdir().getAbsolute());
    }

    private class FolderComboboxModel extends ProxyController implements NSComboBox.DataSource {

        private final Comparator<Path> comparator = new NullComparator<Path>();

        private final PathFilter<Path> filter = new PathFilter<Path>() {
            public boolean accept(Path p) {
                return p.attributes.isDirectory();
            }
        };

        public NSInteger numberOfItemsInComboBox(NSComboBox combo) {
            if(!((CDBrowserController) parent).isMounted()) {
                return new NSInteger(0);
            }
            return new NSInteger(((CDBrowserController) parent).workdir().childs(comparator, filter).size());
        }

        public NSObject comboBox_objectValueForItemAtIndex(final NSComboBox sender, final NSInteger row) {
            return ((CDBrowserController) parent).workdir().childs(comparator, filter).get(row.intValue()).<NSObject>getReference().unique();
        }
    }

    public CDGotoController(final CDWindowController parent) {
        super(parent);
    }

    @Override
    protected void invalidate() {
        folderCombobox.setDelegate(null);
        folderCombobox.setDataSource(null);
        super.invalidate();
    }

    @Override
    public String getBundleName() {
        return "Goto";
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.gotoFolder(((CDBrowserController) parent).workdir(), folderCombobox.stringValue());
        }
    }

    @Override
    protected boolean validateInput() {
        return StringUtils.isNotBlank(folderCombobox.stringValue());
    }

    protected void gotoFolder(final Path workdir, final String filename) {
        final CDBrowserController c = (CDBrowserController) parent;
        final Path dir = PathFactory.createPath(workdir.getSession(), workdir.getAsDictionary());
        if(filename.charAt(0) != '/') {
            dir.setPath(workdir.getAbsolute(), filename);
        }
        else {
            dir.setPath(filename);
        }
        if(workdir.getParent().equals(dir)) {
            c.setWorkdir(dir, workdir);
        }
        else {
            c.setWorkdir(dir);
        }
    }
}