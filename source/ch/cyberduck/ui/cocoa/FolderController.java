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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.ui.browser.UploadTargetFinder;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.application.NSPopUpButton;
import ch.cyberduck.ui.cocoa.application.NSView;
import ch.cyberduck.ui.cocoa.threading.BrowserControllerBackgroundAction;
import ch.cyberduck.ui.resources.IconCacheFactory;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @version $Id$
 */
public class FolderController extends FileController {

    @Outlet
    private NSPopUpButton regionPopup;

    public void setRegionPopup(final NSPopUpButton regionPopup) {
        this.regionPopup = regionPopup;
    }

    @Outlet
    private NSView view;

    public void setView(final NSView view) {
        this.view = view;
    }

    private Set<Location.Name> regions;

    public FolderController(final WindowController parent, final Cache cache, final Set<Location.Name> regions) {
        super(parent, cache, NSAlert.alert(
                LocaleFactory.localizedString("Create new folder", "Folder"),
                LocaleFactory.localizedString("Enter the name for the new folder:", "Folder"),
                LocaleFactory.localizedString("Create", "Folder"),
                null,
                LocaleFactory.localizedString("Cancel", "Folder")
        ));
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed("newfolder.tiff", 64));
        this.regions = regions;
    }

    @Override
    public void setAccessoryView(final NSView input) {
        if(this.hasLocation()) {
            // Override accessory view with location menu added
            this.loadBundle("Folder");
            for(Location.Name region : regions) {
                regionPopup.addItemWithTitle(region.toString());
                regionPopup.itemWithTitle(region.toString()).setRepresentedObject(region.getIdentifier());
                if(region.getIdentifier().equals(Preferences.instance().getProperty("s3.location"))) {
                    regionPopup.selectItem(regionPopup.lastItem());
                }
            }
            super.setAccessoryView(view);
        }
        else {
            super.setAccessoryView(input);
        }
    }

    private boolean hasLocation() {
        return this.getWorkdir().isRoot() && !regions.isEmpty();
    }

    @Override
    public void callback(int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.run(new UploadTargetFinder(this.getWorkdir()).find(this.getSelected()), inputField.stringValue());
        }
    }

    protected void run(final Path parent, final String filename) {
        final BrowserController c = (BrowserController) this.parent;
        final Path folder = new Path(parent, filename, EnumSet.of(Path.Type.directory));
        c.background(new BrowserControllerBackgroundAction<Path>(c) {
            @Override
            public Path run() throws BackgroundException {
                final Directory feature = c.getSession().getFeature(Directory.class);
                if(hasLocation()) {
                    feature.mkdir(folder, regionPopup.selectedItem().representedObject());
                }
                else {
                    feature.mkdir(folder);
                }
                return folder;
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"),
                        folder.getName());
            }

            @Override
            public void cleanup() {
                super.cleanup();
                if(filename.charAt(0) == '.') {
                    c.setShowHiddenFiles(true);
                }
                c.reload(Collections.singletonList(folder), Collections.singletonList(folder));
            }
        });
    }
}
