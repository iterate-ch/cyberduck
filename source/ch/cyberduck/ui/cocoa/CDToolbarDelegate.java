package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

/**
* @version $Id$
 */
public class CDToolbarDelegate {
	
	public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
		
		NSToolbarItem item = new NSToolbarItem(itemIdentifier);
		
		if (itemIdentifier.equals(NSBundle.localizedString("New Connection"))) {
			item.setLabel(NSBundle.localizedString("New Connection"));
			item.setPaletteLabel(NSBundle.localizedString("New Connection"));
			item.setToolTip(NSBundle.localizedString("Connect to remote host"));
			item.setImage(NSImage.imageNamed("connect.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("connectButtonClicked", new Class[] {Object.class}));
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("Favorites"))) {
			item.setView(showFavoriteButton);
			item.setMinSize(showFavoriteButton.frame().size());
			item.setMaxSize(showFavoriteButton.frame().size());
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("Quick Connect"))) {
			item.setLabel(NSBundle.localizedString("Quick Connect"));
			item.setPaletteLabel(NSBundle.localizedString("Quick Connect"));
			item.setToolTip(NSBundle.localizedString("Connect to host"));
			item.setView(quickConnectPopup);
			item.setMinSize(quickConnectPopup.frame().size());
			item.setMaxSize(quickConnectPopup.frame().size());
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("Refresh"))) {
			item.setLabel(NSBundle.localizedString("Refresh"));
			item.setPaletteLabel(NSBundle.localizedString("Refresh"));
			item.setToolTip(NSBundle.localizedString("Refresh directory listing"));
			item.setImage(NSImage.imageNamed("refresh.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("refreshButtonClicked", new Class[] {Object.class}));
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("Download"))) {
			item.setLabel(NSBundle.localizedString("Download"));
			item.setPaletteLabel(NSBundle.localizedString("Download"));
			item.setToolTip(NSBundle.localizedString("Download file"));
			item.setImage(NSImage.imageNamed("download.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("downloadButtonClicked", new Class[] {Object.class}));
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("Upload"))) {
			item.setLabel(NSBundle.localizedString("Upload"));
			item.setPaletteLabel(NSBundle.localizedString("Upload"));
			item.setToolTip(NSBundle.localizedString("Upload local file to the remote host"));
			item.setImage(NSImage.imageNamed("upload.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("uploadButtonClicked", new Class[] {Object.class}));
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("Get Info"))) {
			item.setLabel(NSBundle.localizedString("Get Info"));
			item.setPaletteLabel(NSBundle.localizedString("Get Info"));
			item.setToolTip(NSBundle.localizedString("Show file attributes"));
			item.setImage(NSImage.imageNamed("info.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("infoButtonClicked", new Class[] {Object.class}));
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("Delete"))) {
			item.setLabel(NSBundle.localizedString("Delete"));
			item.setPaletteLabel(NSBundle.localizedString("Delete"));
			item.setToolTip(NSBundle.localizedString("Delete file"));
			item.setImage(NSImage.imageNamed("delete.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("deleteButtonClicked", new Class[] {Object.class}));
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("New Folder"))) {
			item.setLabel(NSBundle.localizedString("New Folder"));
			item.setPaletteLabel(NSBundle.localizedString("New Folder"));
			item.setToolTip(NSBundle.localizedString("Create New Folder"));
			item.setImage(NSImage.imageNamed("newfolder.icns"));
			item.setTarget(this);
			item.setAction(new NSSelector("folderButtonClicked", new Class[] {Object.class}));
		}
		else if (itemIdentifier.equals(NSBundle.localizedString("Disconnect"))) {
			item.setLabel(NSBundle.localizedString("Disconnect"));
			item.setPaletteLabel(NSBundle.localizedString("Disconnect"));
			item.setToolTip(NSBundle.localizedString("Disconnect"));
			item.setImage(NSImage.imageNamed("disconnect.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("disconnectButtonClicked", new Class[] {Object.class}));
		}
		else {
			// itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
   // Returning null will inform the toolbar this kind of item is not supported.
			item = null;
		}
		return item;
	}
	
	
	public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[] {
			NSBundle.localizedString("New Connection"), 
			NSToolbarItem.SeparatorItemIdentifier, 
			NSBundle.localizedString("Favorites"), 
			NSBundle.localizedString("Quick Connect"), 
			NSBundle.localizedString("Refresh"), 
			NSBundle.localizedString("Get Info"), 
			NSToolbarItem.FlexibleSpaceItemIdentifier, 
			NSBundle.localizedString("Download"), 
			NSBundle.localizedString("Upload"), 
			NSBundle.localizedString("Disconnect")
		});
	}
	
	public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[] {
			NSBundle.localizedString("New Connection"), 
			NSBundle.localizedString("Favorites"), 
			NSBundle.localizedString("Quick Connect"), 
			NSBundle.localizedString("Refresh"), 
			NSBundle.localizedString("Download"), 
			NSBundle.localizedString("Upload"), 
			NSBundle.localizedString("Delete"), 
			NSBundle.localizedString("New Folder"), 
			NSBundle.localizedString("Get Info"), 
			NSBundle.localizedString("Disconnect"), 
			NSToolbarItem.CustomizeToolbarItemIdentifier, 
			NSToolbarItem.SpaceItemIdentifier, 
			NSToolbarItem.SeparatorItemIdentifier, 
			NSToolbarItem.FlexibleSpaceItemIdentifier
		});
	}
}