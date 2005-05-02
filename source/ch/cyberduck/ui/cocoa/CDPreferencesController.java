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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionPool;
import ch.cyberduck.ui.cocoa.odb.Editor;

/**
 * @version $Id$
 */
public class CDPreferencesController extends CDWindowController {
	private static Logger log = Logger.getLogger(CDPreferencesController.class);

	private static CDPreferencesController instance;

	private static NSMutableArray instances = new NSMutableArray();

	public static CDPreferencesController instance() {
		log.debug("instance");
		if(null == instance) {
			instance = new CDPreferencesController();
			if(false == NSApplication.loadNibNamed("Preferences", instance)) {
				log.fatal("Couldn't load Preferences.nib");
			}
		}
		return instance;
	}

	private CDPreferencesController() {
		instances.addObject(this);
	}
	
    private NSTabView tabView;

    public void setTabView(NSTabView tabView) {
        this.tabView = tabView;
    }

    private NSView panelGeneral;
    private NSView panelInterface;
    private NSView panelTransfer;
    private NSView panelFTP;
    private NSView panelFTPTLS;
    private NSView panelSFTP;
    private NSView panelAdvanced;

    public void setPanelAdvanced(NSView panelAdvanced) {
        this.panelAdvanced = panelAdvanced;
    }

    public void setPanelSFTP(NSView panelSFTP) {
        this.panelSFTP = panelSFTP;
    }

    public void setPanelFTPTLS(NSView panelFTPTLS) {
        this.panelFTPTLS = panelFTPTLS;
    }

    public void setPanelFTP(NSView panelFTP) {
        this.panelFTP = panelFTP;
    }

    public void setPanelTransfer(NSView panelTransfer) {
        this.panelTransfer = panelTransfer;
    }

    public void setPanelInterface(NSView panelInterface) {
        this.panelInterface = panelInterface;
    }

    public void setPanelGeneral(NSView panelGeneral) {
        this.panelGeneral = panelGeneral;
    }

	public void windowWillClose(NSNotification notification) {
		NSNotificationCenter.defaultCenter().removeObserver(this);
		instances.removeObject(this);
		instance = null;
	}

	public void awakeFromNib() {
        super.awakeFromNib();

		this.window().setReleasedWhenClosed(true);
		this.window().center();
		
		this.transfermodeComboboxClicked(this.transfermodeCombobox);
		{
			Permission p = new Permission(Preferences.instance().getProperty("queue.upload.permissions.default"));
			boolean[] ownerPerm = p.getOwnerPermissions();
			boolean[] groupPerm = p.getGroupPermissions();
			boolean[] otherPerm = p.getOtherPermissions();

			uownerr.setState(ownerPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
			uownerw.setState(ownerPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
			uownerx.setState(ownerPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);

			ugroupr.setState(groupPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
			ugroupw.setState(groupPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
			ugroupx.setState(groupPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);

			uotherr.setState(otherPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
			uotherw.setState(otherPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
			uotherx.setState(otherPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);
		}
		{
			Permission p = new Permission(Preferences.instance().getProperty("queue.download.permissions.default"));
			boolean[] ownerPerm = p.getOwnerPermissions();
			boolean[] groupPerm = p.getGroupPermissions();
			boolean[] otherPerm = p.getOtherPermissions();

			downerr.setState(ownerPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
			downerw.setState(ownerPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
			downerx.setState(ownerPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);

			dgroupr.setState(groupPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
			dgroupw.setState(groupPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
			dgroupx.setState(groupPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);

			dotherr.setState(otherPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
			dotherw.setState(otherPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
			dotherx.setState(otherPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);
		}

        boolean chmodDownloadDefaultEnabled = Preferences.instance().getBoolean("queue.download.changePermissions")
            && Preferences.instance().getBoolean("queue.download.permissions.useDefault");
        this.downerr.setEnabled(chmodDownloadDefaultEnabled);
        this.downerr.setTarget(this);
        this.downerr.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));
        this.downerw.setEnabled(chmodDownloadDefaultEnabled);
        this.downerw.setTarget(this);
        this.downerw.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));
        this.downerx.setEnabled(chmodDownloadDefaultEnabled);
        this.downerx.setTarget(this);
        this.downerx.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));

        this.dgroupr.setEnabled(chmodDownloadDefaultEnabled);
        this.dgroupr.setTarget(this);
        this.dgroupr.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));
        this.dgroupw.setEnabled(chmodDownloadDefaultEnabled);
        this.dgroupw.setTarget(this);
        this.dgroupw.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));
        this.dgroupx.setEnabled(chmodDownloadDefaultEnabled);
        this.dgroupx.setTarget(this);
        this.dgroupx.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));

        this.dotherr.setEnabled(chmodDownloadDefaultEnabled);
        this.dotherr.setTarget(this);
        this.dotherr.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));
        this.dotherw.setEnabled(chmodDownloadDefaultEnabled);
        this.dotherw.setTarget(this);
        this.dotherw.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));
        this.dotherx.setEnabled(chmodDownloadDefaultEnabled);
        this.dotherx.setTarget(this);
        this.dotherx.setAction(new NSSelector("defaultPermissionsDownloadChanged", new Class[]{NSButton.class}));

        boolean chmodUploadDefaultEnabled = Preferences.instance().getBoolean("queue.upload.changePermissions")
            && Preferences.instance().getBoolean("queue.upload.permissions.useDefault");
        this.uownerr.setEnabled(chmodUploadDefaultEnabled);
        this.uownerr.setTarget(this);
        this.uownerr.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));
        this.uownerw.setEnabled(chmodUploadDefaultEnabled);
        this.uownerw.setTarget(this);
        this.uownerw.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));
        this.uownerx.setEnabled(chmodUploadDefaultEnabled);
        this.uownerx.setTarget(this);
        this.uownerx.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));

        this.ugroupr.setEnabled(chmodUploadDefaultEnabled);
        this.ugroupr.setTarget(this);
        this.ugroupr.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));
        this.ugroupw.setEnabled(chmodUploadDefaultEnabled);
        this.ugroupw.setTarget(this);
        this.ugroupw.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));
        this.ugroupx.setEnabled(chmodUploadDefaultEnabled);
        this.ugroupx.setTarget(this);
        this.ugroupx.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));

        this.uotherr.setEnabled(chmodUploadDefaultEnabled);
        this.uotherr.setTarget(this);
        this.uotherr.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));
        this.uotherw.setEnabled(chmodUploadDefaultEnabled);
        this.uotherw.setTarget(this);
        this.uotherw.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));
        this.uotherx.setEnabled(chmodUploadDefaultEnabled);
        this.uotherx.setTarget(this);
        this.uotherx.setAction(new NSSelector("defaultPermissionsUploadChanged", new Class[]{NSButton.class}));

        tabView.tabViewItemAtIndex(0).setView(panelGeneral);
        tabView.tabViewItemAtIndex(1).setView(panelInterface);
        tabView.tabViewItemAtIndex(2).setView(panelTransfer);
        tabView.tabViewItemAtIndex(3).setView(panelFTP);
        tabView.tabViewItemAtIndex(4).setView(panelFTPTLS);
        tabView.tabViewItemAtIndex(5).setView(panelSFTP);
        tabView.tabViewItemAtIndex(6).setView(panelAdvanced);
	}

	private static final String CONNECTMODE_ACTIVE = NSBundle.localizedString("Active", "");
	private static final String CONNECTMODE_PASSIVE = NSBundle.localizedString("Passive", "");

	private static final String TRANSFERMODE_AUTO = NSBundle.localizedString("Auto", "");
	private static final String TRANSFERMODE_BINARY = NSBundle.localizedString("Binary", "");
	private static final String TRANSFERMODE_ASCII = NSBundle.localizedString("ASCII", "");

	private static final String UNIX_LINE_ENDINGS = NSBundle.localizedString("Unix Line Endings (LF)", "");
	private static final String MAC_LINE_ENDINGS = NSBundle.localizedString("Mac Line Endings (CR)", "");
	private static final String WINDOWS_LINE_ENDINGS = NSBundle.localizedString("Windows Line Endings (CRLF)", "");

	private static final String PROTOCOL_FTP = "FTP";
    private static final String PROTOCOL_FTP_TLS = "FTP-TLS";
	private static final String PROTOCOL_SFTP = "SFTP";

	private static final String ASK_ME_WHAT_TO_DO = NSBundle.localizedString("Ask me what to do", "");
	private static final String OVERWRITE_EXISTING_FILE = NSBundle.localizedString("Overwrite existing file", "");
	private static final String TRY_TO_RESUME_TRANSFER = NSBundle.localizedString("Try to resume transfer", "");
	private static final String USE_A_SIMILAR_NAME = NSBundle.localizedString("Use similar name", "");

	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------

	private NSPopUpButton editorCombobox; //IBOutlet

	public void setEditorCombobox(NSPopUpButton editorCombobox) {
		this.editorCombobox = editorCombobox;
		this.editorCombobox.setAutoenablesItems(false);
		this.editorCombobox.setTarget(this);
		this.editorCombobox.setAction(new NSSelector("editorComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.editorCombobox.removeAllItems();
		java.util.Map editors = Editor.SUPPORTED_EDITORS;
		NSSelector absolutePathForAppBundleWithIdentifierSelector =
		    new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
		java.util.Iterator editorNames = editors.keySet().iterator();
		java.util.Iterator editorIdentifiers = editors.values().iterator();
		while(editorNames.hasNext()) {
			String editor = (String)editorNames.next();
            String identifier = (String)editorIdentifiers.next();
			this.editorCombobox.addItem(editor);
			if(absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
                boolean enabled = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                    identifier) != null;
				this.editorCombobox.itemWithTitle(editor).setEnabled(enabled);
                if(enabled) {
                    NSImage icon = NSWorkspace.sharedWorkspace().iconForFile(
                            NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier)
                    );
                    icon.setScalesWhenResized(true);
                    icon.setSize(new NSSize(16f, 16f));
                    this.editorCombobox.itemWithTitle(editor).setImage(icon);
                }
			}
		}
		this.editorCombobox.setTitle(Preferences.instance().getProperty("editor.name"));
	}

	public void editorComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("editor.name", sender.titleOfSelectedItem());
		Preferences.instance().setProperty("editor.bundleIdentifier", (String)Editor.SUPPORTED_EDITORS.get(sender.titleOfSelectedItem()));
	}

	private NSPopUpButton encodingCombobox; //IBOutlet

	public void setEncodingCombobox(NSPopUpButton encodingCombobox) {
		this.encodingCombobox = encodingCombobox;
		this.encodingCombobox.setTarget(this);
		this.encodingCombobox.setAction(new NSSelector("encodingComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.encodingCombobox.removeAllItems();
		java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
		String[] items = new String[charsets.size()];
		java.util.Iterator iterator = charsets.values().iterator();
		int i = 0;
		while(iterator.hasNext()) {
			items[i] = ((java.nio.charset.Charset)iterator.next()).name();
			i++;
		}
		this.encodingCombobox.addItemsWithTitles(new NSArray(items));
		this.encodingCombobox.setTitle(Preferences.instance().getProperty("browser.charset.encoding"));
	}

	public void encodingComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("browser.charset.encoding", sender.titleOfSelectedItem());
	}

	private NSButton listCheckbox; //IBOutlet

	public void setListCheckbox(NSButton listCheckbox) {
		this.listCheckbox = listCheckbox;
		this.listCheckbox.setTarget(this);
		this.listCheckbox.setAction(new NSSelector("listCheckboxClicked", new Class[]{NSButton.class}));
		this.listCheckbox.setState(Preferences.instance().getBoolean("ftp.sendExtendedListCommand") ? NSCell.OnState : NSCell.OffState);
	}

	public void listCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("ftp.sendExtendedListCommand", enabled);
	}

	private NSButton systCheckbox; //IBOutlet

	public void setSystCheckbox(NSButton systCheckbox) {
		this.systCheckbox = systCheckbox;
		this.systCheckbox.setTarget(this);
		this.systCheckbox.setAction(new NSSelector("systCheckboxClicked", new Class[]{NSButton.class}));
		this.systCheckbox.setState(Preferences.instance().getBoolean("ftp.sendSystemCommand") ? NSCell.OnState : NSCell.OffState);
	}

	public void systCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("ftp.sendSystemCommand", enabled);
	}

	private NSButton chmodUploadCheckbox; //IBOutlet

	public void setChmodUploadCheckbox(NSButton chmodUploadCheckbox) {
		this.chmodUploadCheckbox = chmodUploadCheckbox;
		this.chmodUploadCheckbox.setTarget(this);
		this.chmodUploadCheckbox.setAction(new NSSelector("chmodUploadCheckboxClicked", new Class[]{NSButton.class}));
		this.chmodUploadCheckbox.setState(Preferences.instance().getBoolean("queue.upload.changePermissions") ? NSCell.OnState : NSCell.OffState);
	}

	public void chmodUploadCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.upload.changePermissions", enabled);
		this.chmodUploadDefaultCheckbox.setEnabled(enabled);
		boolean chmodUploadDefaultChecked = this.chmodUploadDefaultCheckbox.state() == NSCell.OnState;
		this.uownerr.setEnabled(enabled && chmodUploadDefaultChecked);
		this.uownerw.setEnabled(enabled && chmodUploadDefaultChecked);
		this.uownerx.setEnabled(enabled && chmodUploadDefaultChecked);
		this.ugroupr.setEnabled(enabled && chmodUploadDefaultChecked);
		this.ugroupw.setEnabled(enabled && chmodUploadDefaultChecked);
		this.ugroupx.setEnabled(enabled && chmodUploadDefaultChecked);
		this.uotherr.setEnabled(enabled && chmodUploadDefaultChecked);
		this.uotherw.setEnabled(enabled && chmodUploadDefaultChecked);
		this.uotherx.setEnabled(enabled && chmodUploadDefaultChecked);
	}
	
	private NSButton chmodUploadDefaultCheckbox; //IBOutlet

	public void setChmodUploadDefaultCheckbox(NSButton chmodUploadDefaultCheckbox) {
		this.chmodUploadDefaultCheckbox = chmodUploadDefaultCheckbox;
		this.chmodUploadDefaultCheckbox.setTarget(this);
		this.chmodUploadDefaultCheckbox.setAction(new NSSelector("chmodUploadDefaultCheckboxClicked", new Class[]{NSButton.class}));
		this.chmodUploadDefaultCheckbox.setState(Preferences.instance().getBoolean("queue.upload.permissions.useDefault") ? NSCell.OnState : NSCell.OffState);
		this.chmodUploadDefaultCheckbox.setEnabled(Preferences.instance().getBoolean("queue.upload.changePermissions"));
	}

	public void chmodUploadDefaultCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.upload.permissions.useDefault", enabled);
		this.uownerr.setEnabled(enabled);
		this.uownerw.setEnabled(enabled);
		this.uownerx.setEnabled(enabled);
		this.ugroupr.setEnabled(enabled);
		this.ugroupw.setEnabled(enabled);
		this.ugroupx.setEnabled(enabled);
		this.uotherr.setEnabled(enabled);
		this.uotherw.setEnabled(enabled);
		this.uotherx.setEnabled(enabled);
	}
	
	private NSButton chmodDownloadCheckbox; //IBOutlet

	public void setChmodDownloadCheckbox(NSButton chmodDownloadCheckbox) {
		this.chmodDownloadCheckbox = chmodDownloadCheckbox;
		this.chmodDownloadCheckbox.setTarget(this);
		this.chmodDownloadCheckbox.setAction(new NSSelector("chmodDownloadCheckboxClicked", new Class[]{NSButton.class}));
		this.chmodDownloadCheckbox.setState(Preferences.instance().getBoolean("queue.download.changePermissions") ? NSCell.OnState : NSCell.OffState);
	}

	public void chmodDownloadCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.download.changePermissions", enabled);
		this.chmodDownloadDefaultCheckbox.setEnabled(enabled);
		boolean chmodDownloadDefaultChecked = this.chmodDownloadDefaultCheckbox.state() == NSCell.OnState;
		this.downerr.setEnabled(enabled && chmodDownloadDefaultChecked);
		this.downerw.setEnabled(enabled && chmodDownloadDefaultChecked);
		this.downerx.setEnabled(enabled && chmodDownloadDefaultChecked);
		this.dgroupr.setEnabled(enabled && chmodDownloadDefaultChecked);
		this.dgroupw.setEnabled(enabled && chmodDownloadDefaultChecked);
		this.dgroupx.setEnabled(enabled && chmodDownloadDefaultChecked);
		this.dotherr.setEnabled(enabled && chmodDownloadDefaultChecked);
		this.dotherw.setEnabled(enabled && chmodDownloadDefaultChecked);
		this.dotherx.setEnabled(enabled && chmodDownloadDefaultChecked);
	}
	
	private NSButton chmodDownloadDefaultCheckbox; //IBOutlet

	public void setChmodDownloadDefaultCheckbox(NSButton chmodDownloadDefaultCheckbox) {
		this.chmodDownloadDefaultCheckbox = chmodDownloadDefaultCheckbox;
		this.chmodDownloadDefaultCheckbox.setTarget(this);
		this.chmodDownloadDefaultCheckbox.setAction(new NSSelector("chmodDownloadDefaultCheckboxClicked", new Class[]{NSButton.class}));
		this.chmodDownloadDefaultCheckbox.setState(Preferences.instance().getBoolean("queue.download.permissions.useDefault") ? NSCell.OnState : NSCell.OffState);
		this.chmodDownloadDefaultCheckbox.setEnabled(Preferences.instance().getBoolean("queue.download.changePermissions"));
	}

	public void chmodDownloadDefaultCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.download.permissions.useDefault", enabled);
		this.downerr.setEnabled(enabled);
		this.downerw.setEnabled(enabled);
		this.downerx.setEnabled(enabled);
		this.dgroupr.setEnabled(enabled);
		this.dgroupw.setEnabled(enabled);
		this.dgroupx.setEnabled(enabled);
		this.dotherr.setEnabled(enabled);
		this.dotherw.setEnabled(enabled);
		this.dotherx.setEnabled(enabled);
	}
	
	public NSButton downerr; //IBOutlet
	public NSButton downerw; //IBOutlet
	public NSButton downerx; //IBOutlet
	public NSButton dgroupr; //IBOutlet
	public NSButton dgroupw; //IBOutlet
	public NSButton dgroupx; //IBOutlet
	public NSButton dotherr; //IBOutlet
	public NSButton dotherw; //IBOutlet
	public NSButton dotherx; //IBOutlet

	public void defaultPermissionsDownloadChanged(Object sender) {
		boolean[][] p = new boolean[3][3];

		p[Permission.OWNER][Permission.READ] = (downerr.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.WRITE] = (downerw.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.EXECUTE] = (downerx.state() == NSCell.OnState);

		p[Permission.GROUP][Permission.READ] = (dgroupr.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.WRITE] = (dgroupw.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.EXECUTE] = (dgroupx.state() == NSCell.OnState);

		p[Permission.OTHER][Permission.READ] = (dotherr.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.WRITE] = (dotherw.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.EXECUTE] = (dotherx.state() == NSCell.OnState);

		Permission permission = new Permission(p);
		Preferences.instance().setProperty("queue.download.permissions.default", permission.getMask());
	}

	public NSButton uownerr; //IBOutlet
	public NSButton uownerw; //IBOutlet
	public NSButton uownerx; //IBOutlet
	public NSButton ugroupr; //IBOutlet
	public NSButton ugroupw; //IBOutlet
	public NSButton ugroupx; //IBOutlet
	public NSButton uotherr; //IBOutlet
	public NSButton uotherw; //IBOutlet
	public NSButton uotherx; //IBOutlet

	public void defaultPermissionsUploadChanged(Object sender) {
		boolean[][] p = new boolean[3][3];

		p[Permission.OWNER][Permission.READ] = (uownerr.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.WRITE] = (uownerw.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.EXECUTE] = (uownerx.state() == NSCell.OnState);

		p[Permission.GROUP][Permission.READ] = (ugroupr.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.WRITE] = (ugroupw.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.EXECUTE] = (ugroupx.state() == NSCell.OnState);

		p[Permission.OTHER][Permission.READ] = (uotherr.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.WRITE] = (uotherw.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.EXECUTE] = (uotherx.state() == NSCell.OnState);

		Permission permission = new Permission(p);
		Preferences.instance().setProperty("queue.upload.permissions.default", permission.getMask());
	}

	private NSButton preserveModificationDownloadCheckbox; //IBOutlet

	public void setPreserveModificationDownloadCheckbox(NSButton preserveModificationDownloadCheckbox) {
		this.preserveModificationDownloadCheckbox = preserveModificationDownloadCheckbox;
		this.preserveModificationDownloadCheckbox.setTarget(this);
		this.preserveModificationDownloadCheckbox.setAction(new NSSelector("preserveModificationDownloadCheckboxClicked", new Class[]{NSButton.class}));
		this.preserveModificationDownloadCheckbox.setState(Preferences.instance().getBoolean("queue.download.preserveDate") ? NSCell.OnState : NSCell.OffState);
	}

	public void preserveModificationDownloadCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.download.preserveDate", enabled);
	}

	private NSButton preserveModificationUploadCheckbox; //IBOutlet

	public void setPreserveModificationUploadCheckbox(NSButton preserveModificationUploadCheckbox) {
		this.preserveModificationUploadCheckbox = preserveModificationUploadCheckbox;
		this.preserveModificationUploadCheckbox.setTarget(this);
		this.preserveModificationUploadCheckbox.setAction(new NSSelector("preserveModificationUploadCheckboxClicked", new Class[]{NSButton.class}));
		this.preserveModificationUploadCheckbox.setState(Preferences.instance().getBoolean("queue.upload.preserveDate") ? NSCell.OnState : NSCell.OffState);
	}

	public void preserveModificationUploadCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.upload.preserveDate", enabled);
	}

	private NSButton horizontalLinesCheckbox; //IBOutlet

	public void setHorizontalLinesCheckbox(NSButton horizontalLinesCheckbox) {
		this.horizontalLinesCheckbox = horizontalLinesCheckbox;
		this.horizontalLinesCheckbox.setTarget(this);
		this.horizontalLinesCheckbox.setAction(new NSSelector("horizontalLinesCheckboxClicked", new Class[]{NSButton.class}));
		this.horizontalLinesCheckbox.setState(Preferences.instance().getBoolean("browser.horizontalLines") ? NSCell.OnState : NSCell.OffState);
	}

	public void horizontalLinesCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.horizontalLines", enabled);
		CDBrowserController.updateBrowserTableAttributes();
	}

	private NSButton verticalLinesCheckbox; //IBOutlet

	public void setVerticalLinesCheckbox(NSButton verticalLinesCheckbox) {
		this.verticalLinesCheckbox = verticalLinesCheckbox;
		this.verticalLinesCheckbox.setTarget(this);
		this.verticalLinesCheckbox.setAction(new NSSelector("verticalLinesCheckboxClicked", new Class[]{NSButton.class}));
		this.verticalLinesCheckbox.setState(Preferences.instance().getBoolean("browser.verticalLines") ? NSCell.OnState : NSCell.OffState);
	}

	public void verticalLinesCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.verticalLines", enabled);
		CDBrowserController.updateBrowserTableAttributes();
	}

	private NSButton alternatingRowBackgroundCheckbox; //IBOutlet

	public void setAlternatingRowBackgroundCheckbox(NSButton alternatingRowBackgroundCheckbox) {
		this.alternatingRowBackgroundCheckbox = alternatingRowBackgroundCheckbox;
		this.alternatingRowBackgroundCheckbox.setTarget(this);
		this.alternatingRowBackgroundCheckbox.setAction(new NSSelector("alternatingRowBackgroundCheckboxClicked", new Class[]{NSButton.class}));
		this.alternatingRowBackgroundCheckbox.setState(Preferences.instance().getBoolean("browser.alternatingRows") ? NSCell.OnState : NSCell.OffState);
	}

	public void alternatingRowBackgroundCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.alternatingRows", enabled);
		CDBrowserController.updateBrowserTableAttributes();
	}

	private NSButton infoWindowAsInspectorCheckbox; //IBOutlet
	
	public void setInfoWindowAsInspectorCheckbox(NSButton infoWindowAsInspectorCheckbox) {
		this.infoWindowAsInspectorCheckbox = infoWindowAsInspectorCheckbox;
		this.infoWindowAsInspectorCheckbox.setTarget(this);
		this.infoWindowAsInspectorCheckbox.setAction(new NSSelector("infoWindowAsInspectorCheckboxClicked", new Class[]{NSButton.class}));
		this.infoWindowAsInspectorCheckbox.setState(Preferences.instance().getBoolean("browser.info.isInspector") ? NSCell.OnState : NSCell.OffState);
	}
	
	public void infoWindowAsInspectorCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.info.isInspector", enabled);
	}
	
	private NSButton columnModificationCheckbox; //IBOutlet

	public void setColumnModificationCheckbox(NSButton columnModificationCheckbox) {
		this.columnModificationCheckbox = columnModificationCheckbox;
		this.columnModificationCheckbox.setTarget(this);
		this.columnModificationCheckbox.setAction(new NSSelector("columnModificationCheckboxClicked", new Class[]{NSButton.class}));
		this.columnModificationCheckbox.setState(Preferences.instance().getBoolean("browser.columnModification") ? NSCell.OnState : NSCell.OffState);
	}

	public void columnModificationCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.columnModification", enabled);
		CDBrowserController.updateBrowserTableColumns();
	}

	private NSButton columnOwnerCheckbox; //IBOutlet

	public void setColumnOwnerCheckbox(NSButton columnOwnerCheckbox) {
		this.columnOwnerCheckbox = columnOwnerCheckbox;
		this.columnOwnerCheckbox.setTarget(this);
		this.columnOwnerCheckbox.setAction(new NSSelector("columnOwnerCheckboxClicked", new Class[]{NSButton.class}));
		this.columnOwnerCheckbox.setState(Preferences.instance().getBoolean("browser.columnOwner") ? NSCell.OnState : NSCell.OffState);
	}

	public void columnOwnerCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.columnOwner", enabled);
		CDBrowserController.updateBrowserTableColumns();
	}

	private NSButton columnPermissionsCheckbox; //IBOutlet

	public void setColumnPermissionsCheckbox(NSButton columnPermissionsCheckbox) {
		this.columnPermissionsCheckbox = columnPermissionsCheckbox;
		this.columnPermissionsCheckbox.setTarget(this);
		this.columnPermissionsCheckbox.setAction(new NSSelector("columnPermissionsCheckboxClicked", new Class[]{NSButton.class}));
		this.columnPermissionsCheckbox.setState(Preferences.instance().getBoolean("browser.columnPermissions") ? NSCell.OnState : NSCell.OffState);
	}

	public void columnPermissionsCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.columnPermissions", enabled);
		CDBrowserController.updateBrowserTableColumns();
	}

	private NSButton columnSizeCheckbox; //IBOutlet

	public void setColumnSizeCheckbox(NSButton columnSizeCheckbox) {
		this.columnSizeCheckbox = columnSizeCheckbox;
		this.columnSizeCheckbox.setTarget(this);
		this.columnSizeCheckbox.setAction(new NSSelector("columnSizeCheckboxClicked", new Class[]{NSButton.class}));
		this.columnSizeCheckbox.setState(Preferences.instance().getBoolean("browser.columnSize") ? NSCell.OnState : NSCell.OffState);
	}

	public void columnSizeCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.columnSize", enabled);
		CDBrowserController.updateBrowserTableColumns();
	}

	// public-key algorithms
	private static final String SSH_DSS = "ssh-dss";
	private static final String SSH_RSA = "ssh-rsa";

	private NSPopUpButton publickeyCombobox; //IBOutlet

	public void setPublickeyCombobox(NSPopUpButton publickeyCombobox) {
		this.publickeyCombobox = publickeyCombobox;
		this.publickeyCombobox.setTarget(this);
		this.publickeyCombobox.setAction(new NSSelector("publickeyComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.publickeyCombobox.removeAllItems();
		this.publickeyCombobox.addItemsWithTitles(new NSArray(new String[]{
			//NSBundle.localizedString("Default", ""),
			SSH_DSS,
			SSH_RSA
		}));

		publickeyCombobox.setTitle(Preferences.instance().getProperty("ssh.publickey"));
	}

	public void publickeyComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.publickey", sender.titleOfSelectedItem());
	}

	//encryption ciphers
	private static final String des_cbc = "3des-cbc";
	private static final String blowfish_cbc = "blowfish-cbc";
	private static final String twofish256_cbc = "twofish256-cbc";
	private static final String twofish192_cbc = "twofish192-cbc";
	private static final String twofish128_cbc = "twofish128-cbc";
	private static final String aes256_cbc = "aes256-cbc";
	private static final String aes192_cbc = "aes192-cbc";
	private static final String aes128_cbc = "aes128-cbc";
	private static final String cast128_cbc = "cast128-cbc";

	private NSPopUpButton csEncryptionCombobox; //IBOutlet

	public void setCsEncryptionCombobox(NSPopUpButton csEncryptionCombobox) {
		this.csEncryptionCombobox = csEncryptionCombobox;
		this.csEncryptionCombobox.setTarget(this);
		this.csEncryptionCombobox.setAction(new NSSelector("csEncryptionComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.csEncryptionCombobox.removeAllItems();
		this.csEncryptionCombobox.addItemsWithTitles(new NSArray(new String[]{
			des_cbc,
			blowfish_cbc,
			twofish256_cbc,
			twofish192_cbc,
			twofish128_cbc,
			aes256_cbc,
			aes192_cbc,
			aes128_cbc,
			cast128_cbc
		}));
		this.csEncryptionCombobox.setTitle(Preferences.instance().getProperty("ssh.CSEncryption"));
	}

	public void csEncryptionComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.CSEncryption", sender.titleOfSelectedItem());
	}

	private NSPopUpButton scEncryptionCombobox; //IBOutlet

	public void setScEncryptionCombobox(NSPopUpButton scEncryptionCombobox) {
		this.scEncryptionCombobox = scEncryptionCombobox;
		this.scEncryptionCombobox.setTarget(this);
		this.scEncryptionCombobox.setAction(new NSSelector("scEncryptionComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.scEncryptionCombobox.removeAllItems();
		this.scEncryptionCombobox.addItemsWithTitles(new NSArray(new String[]{
			des_cbc,
			blowfish_cbc,
			twofish256_cbc,
			twofish192_cbc,
			twofish128_cbc,
			aes256_cbc,
			aes192_cbc,
			aes128_cbc,
			cast128_cbc
		}));

		this.scEncryptionCombobox.setTitle(Preferences.instance().getProperty("ssh.SCEncryption"));
	}

	public void scEncryptionComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.SCEncryption", sender.titleOfSelectedItem());
	}


	//authentication algorithms
	private static final String hmac_sha1 = "hmac-sha1";
	private static final String hmac_sha1_96 = "hmac-sha1-96";
	private static final String hmac_md5 = "hmac-md5";
	private static final String hmac_md5_96 = "hmac-md5-96";

	private NSPopUpButton scAuthenticationCombobox; //IBOutlet

	public void setScAuthenticationCombobox(NSPopUpButton scAuthenticationCombobox) {
		this.scAuthenticationCombobox = scAuthenticationCombobox;
		this.scAuthenticationCombobox.setTarget(this);
		this.scAuthenticationCombobox.setAction(new NSSelector("scAuthenticationComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.scAuthenticationCombobox.removeAllItems();
		this.scAuthenticationCombobox.addItemsWithTitles(new NSArray(new String[]{
			hmac_sha1,
			hmac_sha1_96,
			hmac_md5,
			hmac_md5_96
		}));

		this.scAuthenticationCombobox.setTitle(Preferences.instance().getProperty("ssh.SCAuthentication"));
	}

	public void scAuthenticationComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.SCAuthentication", sender.titleOfSelectedItem());
	}


	private NSPopUpButton csAuthenticationCombobox; //IBOutlet

	public void setCsAuthenticationCombobox(NSPopUpButton csAuthenticationCombobox) {
		this.csAuthenticationCombobox = csAuthenticationCombobox;
		this.csAuthenticationCombobox.setTarget(this);
		this.csAuthenticationCombobox.setAction(new NSSelector("csAuthenticationComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.csAuthenticationCombobox.removeAllItems();
		this.csAuthenticationCombobox.addItemsWithTitles(new NSArray(new String[]{
			hmac_sha1,
			hmac_sha1_96,
			hmac_md5,
			hmac_md5_96
		}));

		this.csAuthenticationCombobox.setTitle(Preferences.instance().getProperty("ssh.CSAuthentication"));
	}

	public void csAuthenticationComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.CSAuthentication", sender.titleOfSelectedItem());
	}
	
	private static final String ZLIB = "zlib";

	private NSPopUpButton compressionCombobox; //IBOutlet

	public void setCompressionCombobox(NSPopUpButton compressionCombobox) {
		this.compressionCombobox = compressionCombobox;
		this.compressionCombobox.setTarget(this);
		this.compressionCombobox.setAction(new NSSelector("compressionComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.compressionCombobox.removeAllItems();
		this.compressionCombobox.addItemsWithTitles(new NSArray(new String[]{
			NSBundle.localizedString("None", ""),
			ZLIB
		}));
		
		this.compressionCombobox.setTitle(Preferences.instance().getProperty("ssh.compression"));
	}
	
	public void compressionComboboxClicked(NSPopUpButton sender) {
		if(sender.titleOfSelectedItem().equals(ZLIB))
			Preferences.instance().setProperty("ssh.compression", ZLIB);
		else
			Preferences.instance().setProperty("ssh.compression", "none");
	}
	
	private NSButton downloadPathButton; //IBOutlet

	public void setDownloadPathButton(NSButton downloadPathButton) {
		this.downloadPathButton = downloadPathButton;
		this.downloadPathButton.setTarget(this);
		this.downloadPathButton.setAction(new NSSelector("downloadPathButtonClicked", new Class[]{NSButton.class}));
	}

	public void downloadPathButtonClicked(NSButton sender) {
		NSOpenPanel panel = NSOpenPanel.openPanel();
		panel.setCanChooseFiles(false);
		panel.setCanChooseDirectories(true);
		panel.setAllowsMultipleSelection(false);
		panel.beginSheetForDirectory(null, null, null, this.window(), this, new NSSelector("openPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
	}
	
	public void openPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
		switch(returnCode) {
			case (NSAlertPanel.DefaultReturn): {
				NSArray selected = sheet.filenames();
				String filename;
				if((filename = (String)selected.lastObject()) != null) {
					Preferences.instance().setProperty("queue.download.folder", filename);
					this.downloadPathField.setStringValue(Preferences.instance().getProperty("queue.download.folder"));
				}
				break;
			}
			case (NSAlertPanel.AlternateReturn): {
				break;
			}
		}
	}

	private NSButton defaultBufferButton; //IBOutlet

	public void setDefaultBufferButton(NSButton defaultBufferButton) {
		this.defaultBufferButton = defaultBufferButton;
		this.defaultBufferButton.setTarget(this);
		this.defaultBufferButton.setAction(new NSSelector("defaultBufferButtonClicked", new Class[]{NSButton.class}));
	}

	public void defaultBufferButtonClicked(NSButton sender) {
		Preferences.instance().setProperty("connection.buffer", Preferences.instance().getProperty("connection.buffer.default"));
		try {
			int bytes = Preferences.instance().getInteger("connection.buffer");
			int kbit = bytes/1024*8;
			this.bufferField.setStringValue(""+kbit);
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSTextField concurrentConnectionsField; //IBOutlet

	public void setConcurrentConnectionsField(NSTextField concurrentConnectionsField) {
		this.concurrentConnectionsField = concurrentConnectionsField;
		this.concurrentConnectionsField.setStringValue(Preferences.instance().getProperty("connection.pool.max"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("concurrentConnectionsFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.concurrentConnectionsField);
	}

	public void concurrentConnectionsFieldDidChange(NSNotification sender) {
		try {
			int max = Integer.parseInt(this.concurrentConnectionsField.stringValue());
			Preferences.instance().setProperty("connection.pool.max", max);
			synchronized(SessionPool.instance()) {
				SessionPool.instance().notifyAll();
			}
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSTextField concurrentConnectionsTimeoutField; //IBOutlet

	public void setConcurrentConnectionsTimeoutField(NSTextField concurrentConnectionsTimeoutField) {
		this.concurrentConnectionsTimeoutField = concurrentConnectionsTimeoutField;
		this.concurrentConnectionsTimeoutField.setStringValue(Preferences.instance().getProperty("connection.pool.timeout"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("concurrentConnectionsTimeoutFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.concurrentConnectionsTimeoutField);
	}

	public void concurrentConnectionsTimeoutFieldDidChange(NSNotification sender) {
		try {
			int timeout = Integer.parseInt(this.concurrentConnectionsTimeoutField.stringValue());
			Preferences.instance().setProperty("connection.pool.timeout", timeout);
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSButton concurrentConnectionForceDisconnectCheckbox; //IBOutlet

	public void setConcurrentConnectionForceDisconnectCheckbox(NSButton concurrentConnectionForceDisconnectCheckbox) {
		this.concurrentConnectionForceDisconnectCheckbox = concurrentConnectionForceDisconnectCheckbox;
		this.concurrentConnectionForceDisconnectCheckbox.setTarget(this);
		this.concurrentConnectionForceDisconnectCheckbox.setAction(new NSSelector("concurrentConnectionForceDisconnectCheckboxClicked", new Class[]{NSButton.class}));
		this.concurrentConnectionForceDisconnectCheckbox.setState(Preferences.instance().getBoolean("connection.pool.force") ? NSCell.OnState : NSCell.OffState);
	}

	public void concurrentConnectionForceDisconnectCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("connection.pool.force", enabled);
	}

	private NSTextField bufferField; //IBOutlet

	public void setBufferField(NSTextField bufferField) {
		this.bufferField = bufferField;
		try {
			int bytes = Preferences.instance().getInteger("connection.buffer");
			int kbit = bytes/1024*8;
			this.bufferField.setStringValue(""+kbit);
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("bufferFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.bufferField);
	}

	public void bufferFieldDidChange(NSNotification sender) {
		try {
			int kbit = Integer.parseInt(this.bufferField.stringValue());
			Preferences.instance().setProperty("connection.buffer", (int)kbit/8*1024); //Bytes
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSButton keepAliveCheckbox; //IBOutlet

	public void setKeepAliveCheckbox(NSButton keepAliveCheckbox) {
		this.keepAliveCheckbox = keepAliveCheckbox;
		this.keepAliveCheckbox.setTarget(this);
		this.keepAliveCheckbox.setAction(new NSSelector("keepAliveCheckboxClicked", new Class[]{NSButton.class}));
		this.keepAliveCheckbox.setState(Preferences.instance().getBoolean("connection.keepalive") ? NSCell.OnState : NSCell.OffState);
	}

	public void keepAliveCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("connection.keepalive", enabled);
	}

	private NSTextField keepAliveIntervalField; //IBOutlet

	public void setKeepAliveIntervalField(NSTextField keepAliveIntervalField) {
		this.keepAliveIntervalField = keepAliveIntervalField;
		try {
			int i = Preferences.instance().getInteger("connection.keepalive.interval");
			this.keepAliveIntervalField.setStringValue(""+i);
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("keepAliveIntervalFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.keepAliveIntervalField);
	}

	public void keepAliveIntervalFieldDidChange(NSNotification sender) {
		try {
			int i = Integer.parseInt(this.keepAliveIntervalField.stringValue());
			Preferences.instance().setProperty("connection.keepalive.interval", (int)i);
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSTextField anonymousField; //IBOutlet

	public void setAnonymousField(NSTextField anonymousField) {
		this.anonymousField = anonymousField;
		this.anonymousField.setStringValue(Preferences.instance().getProperty("ftp.anonymous.pass"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("anonymousFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.anonymousField);
	}

	public void anonymousFieldDidChange(NSNotification sender) {
		Preferences.instance().setProperty("ftp.anonymous.pass", this.anonymousField.stringValue());
	}

	private NSTextField downloadPathField; //IBOutlet

	public void setDownloadPathField(NSTextField downloadPathField) {
		this.downloadPathField = downloadPathField;
		this.downloadPathField.setStringValue(Preferences.instance().getProperty("queue.download.folder"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("downloadPathFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.downloadPathField);
	}

	public void downloadPathFieldDidChange(NSNotification sender) {
		Preferences.instance().setProperty("queue.download.folder", this.downloadPathField.stringValue());
	}

	private NSTextField extensionsField; //IBOutlet

	public void setExtensionsField(NSTextField extensionsField) {
		this.extensionsField = extensionsField;
		this.extensionsField.setStringValue(Preferences.instance().getProperty("ftp.transfermode.ascii.extensions"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("extensionsFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.extensionsField);
	}

	public void extensionsFieldDidChange(NSNotification sender) {
		Preferences.instance().setProperty("ftp.transfermode.ascii.extensions", this.extensionsField.stringValue());
	}

	private NSTextField loginField; //IBOutlet

	public void setLoginField(NSTextField loginField) {
		this.loginField = loginField;
		this.loginField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("loginFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.loginField);
	}

	public void loginFieldDidChange(NSNotification sender) {
		Preferences.instance().setProperty("connection.login.name", this.loginField.stringValue());
	}

	private NSButton keychainCheckbox; //IBOutlet

	public void setKeychainCheckbox(NSButton keychainCheckbox) {
		this.keychainCheckbox = keychainCheckbox;
		this.keychainCheckbox.setTarget(this);
		this.keychainCheckbox.setAction(new NSSelector("keychainCheckboxClicked", new Class[]{NSButton.class}));
		this.keychainCheckbox.setState(Preferences.instance().getBoolean("connection.login.useKeychain") ? NSCell.OnState : NSCell.OffState);
	}

	public void keychainCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("connection.login.useKeychain", enabled);
	}

	private NSButton doubleClickCheckbox; //IBOutlet

	public void setDoubleClickCheckbox(NSButton doubleClickCheckbox) {
		this.doubleClickCheckbox = doubleClickCheckbox;
		this.doubleClickCheckbox.setTarget(this);
		this.doubleClickCheckbox.setAction(new NSSelector("doubleClickCheckboxClicked", new Class[]{NSButton.class}));
		this.doubleClickCheckbox.setState(Preferences.instance().getBoolean("browser.doubleclick.edit") ? NSCell.OnState : NSCell.OffState);
	}

	public void doubleClickCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.doubleclick.edit", enabled);
	}

	private NSButton showHiddenCheckbox; //IBOutlet

	public void setShowHiddenCheckbox(NSButton showHiddenCheckbox) {
		this.showHiddenCheckbox = showHiddenCheckbox;
		this.showHiddenCheckbox.setTarget(this);
		this.showHiddenCheckbox.setAction(new NSSelector("showHiddenCheckboxClicked", new Class[]{NSButton.class}));
		this.showHiddenCheckbox.setState(Preferences.instance().getBoolean("browser.showHidden") ? NSCell.OnState : NSCell.OffState);
	}

	public void showHiddenCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.showHidden", enabled);
	}

	private NSButton newBrowserCheckbox; //IBOutlet

	public void setNewBrowserCheckbox(NSButton newBrowserCheckbox) {
		this.newBrowserCheckbox = newBrowserCheckbox;
		this.newBrowserCheckbox.setTarget(this);
		this.newBrowserCheckbox.setAction(new NSSelector("newBrowserCheckboxClicked", new Class[]{NSButton.class}));
		this.newBrowserCheckbox.setState(Preferences.instance().getBoolean("browser.openByDefault") ? NSCell.OnState : NSCell.OffState);
	}

	public void newBrowserCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.openByDefault", enabled);
		//				this.defaultHostCombobox.setEnabled(enabled);
	}

	private NSButton bringQueueToFrontCheckbox; //IBOutlet

	public void setBringQueueToFrontCheckbox(NSButton bringQueueToFrontCheckbox) {
		this.bringQueueToFrontCheckbox = bringQueueToFrontCheckbox;
		this.bringQueueToFrontCheckbox.setTarget(this);
		this.bringQueueToFrontCheckbox.setAction(new NSSelector("bringQueueToFrontCheckboxClicked", new Class[]{NSButton.class}));
		this.bringQueueToFrontCheckbox.setState(Preferences.instance().getBoolean("queue.orderFrontOnTransfer") ? NSCell.OnState : NSCell.OffState);
	}

	public void bringQueueToFrontCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.orderFrontOnTransfer", enabled);
	}

	private NSButton removeFromQueueCheckbox; //IBOutlet

	public void setRemoveFromQueueCheckbox(NSButton removeFromQueueCheckbox) {
		this.removeFromQueueCheckbox = removeFromQueueCheckbox;
		this.removeFromQueueCheckbox.setTarget(this);
		this.removeFromQueueCheckbox.setAction(new NSSelector("removeFromQueueCheckboxClicked", new Class[]{NSButton.class}));
		this.removeFromQueueCheckbox.setState(Preferences.instance().getBoolean("queue.removeItemWhenComplete") ? NSCell.OnState : NSCell.OffState);
	}

	public void removeFromQueueCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.removeItemWhenComplete", enabled);
	}

	private NSButton processCheckbox; //IBOutlet

	public void setProcessCheckbox(NSButton processCheckbox) {
		this.processCheckbox = processCheckbox;
		this.processCheckbox.setTarget(this);
		this.processCheckbox.setAction(new NSSelector("processCheckboxClicked", new Class[]{NSButton.class}));
		this.processCheckbox.setState(Preferences.instance().getBoolean("queue.postProcessItemWhenComplete") ? NSCell.OnState : NSCell.OffState);
	}

	public void processCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("queue.postProcessItemWhenComplete", enabled);
	}

	private NSPopUpButton duplicateCombobox; //IBOutlet

	public void setDuplicateCombobox(NSPopUpButton duplicateCombobox) {
		this.duplicateCombobox = duplicateCombobox;
		this.duplicateCombobox.setTarget(this);
		this.duplicateCombobox.setAction(new NSSelector("duplicateComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.duplicateCombobox.removeAllItems();
		this.duplicateCombobox.addItemsWithTitles(new NSArray(new String[]{ASK_ME_WHAT_TO_DO, OVERWRITE_EXISTING_FILE, TRY_TO_RESUME_TRANSFER, USE_A_SIMILAR_NAME}));
		if(Preferences.instance().getProperty("queue.fileExists").equals("ask")) {
			this.duplicateCombobox.setTitle(ASK_ME_WHAT_TO_DO);
		}
		if(Preferences.instance().getProperty("queue.fileExists").equals("overwrite")) {
			this.duplicateCombobox.setTitle(OVERWRITE_EXISTING_FILE);
		}
		else if(Preferences.instance().getProperty("queue.fileExists").equals("resume")) {
			this.duplicateCombobox.setTitle(TRY_TO_RESUME_TRANSFER);
		}
		else if(Preferences.instance().getProperty("queue.fileExists").equals("similar")) {
			this.duplicateCombobox.setTitle(USE_A_SIMILAR_NAME);
		}
	}

	public void duplicateComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(ASK_ME_WHAT_TO_DO)) {
			Preferences.instance().setProperty("queue.fileExists", "ask");
		}
		if(sender.selectedItem().title().equals(OVERWRITE_EXISTING_FILE)) {
			Preferences.instance().setProperty("queue.fileExists", "overwrite");
		}
		else if(sender.selectedItem().title().equals(TRY_TO_RESUME_TRANSFER)) {
			Preferences.instance().setProperty("queue.fileExists", "resume");
		}
		else if(sender.selectedItem().title().equals(USE_A_SIMILAR_NAME)) {
			Preferences.instance().setProperty("queue.fileExists", "similar");
		}
	}

	private NSPopUpButton lineEndingCombobox; //IBOutlet

	public void setLineEndingCombobox(NSPopUpButton lineEndingCombobox) {
		this.lineEndingCombobox = lineEndingCombobox;
		this.lineEndingCombobox.setTarget(this);
		this.lineEndingCombobox.setAction(new NSSelector("lineEndingComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.lineEndingCombobox.removeAllItems();
		this.lineEndingCombobox.addItemsWithTitles(new NSArray(new String[]{UNIX_LINE_ENDINGS, MAC_LINE_ENDINGS, WINDOWS_LINE_ENDINGS}));
		if(Preferences.instance().getProperty("ftp.line.separator").equals("unix")) {
			this.lineEndingCombobox.setTitle(UNIX_LINE_ENDINGS);
		}
		else if(Preferences.instance().getProperty("ftp.line.separator").equals("mac")) {
			this.lineEndingCombobox.setTitle(MAC_LINE_ENDINGS);
		}
		else if(Preferences.instance().getProperty("ftp.line.separator").equals("win")) {
			this.lineEndingCombobox.setTitle(WINDOWS_LINE_ENDINGS);
		}
	}

	public void lineEndingComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(UNIX_LINE_ENDINGS)) {
			Preferences.instance().setProperty("ftp.line.separator", "unix");
		}
		else if(sender.selectedItem().title().equals(MAC_LINE_ENDINGS)) {
			Preferences.instance().setProperty("ftp.line.separator", "mac");
		}
		else if(sender.selectedItem().title().equals(WINDOWS_LINE_ENDINGS)) {
			Preferences.instance().setProperty("ftp.line.separator", "win");
		}
	}


	private NSPopUpButton transfermodeCombobox; //IBOutlet

	public void setTransfermodeCombobox(NSPopUpButton transfermodeCombobox) {
		this.transfermodeCombobox = transfermodeCombobox;
		this.transfermodeCombobox.setTarget(this);
		this.transfermodeCombobox.setAction(new NSSelector("transfermodeComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.transfermodeCombobox.removeAllItems();
		this.transfermodeCombobox.addItemsWithTitles(new NSArray(new String[]{TRANSFERMODE_AUTO, TRANSFERMODE_BINARY, TRANSFERMODE_ASCII}));
		if(Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
			this.transfermodeCombobox.setTitle(TRANSFERMODE_BINARY);
		}
		else if(Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
			this.transfermodeCombobox.setTitle(TRANSFERMODE_ASCII);
		}
		else if(Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
			this.transfermodeCombobox.setTitle(TRANSFERMODE_AUTO);
		}
	}

	public void transfermodeComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(TRANSFERMODE_BINARY)) {
			Preferences.instance().setProperty("ftp.transfermode", "binary");
			this.lineEndingCombobox.setEnabled(false);
			this.extensionsField.setEnabled(false);
		}
		else if(sender.selectedItem().title().equals(TRANSFERMODE_ASCII)) {
			Preferences.instance().setProperty("ftp.transfermode", "ascii");
			this.lineEndingCombobox.setEnabled(true);
			this.extensionsField.setEnabled(false);
		}
		else if(sender.selectedItem().title().equals(TRANSFERMODE_AUTO)) {
			Preferences.instance().setProperty("ftp.transfermode", "auto");
			this.lineEndingCombobox.setEnabled(true);
			this.extensionsField.setEnabled(true);
		}
	}

	private NSPopUpButton connectmodeCombobox; //IBOutlet

	public void setConnectmodeCombobox(NSPopUpButton connectmodeCombobox) {
		this.connectmodeCombobox = connectmodeCombobox;
		this.connectmodeCombobox.setTarget(this);
		this.connectmodeCombobox.setAction(new NSSelector("connectmodeComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.connectmodeCombobox.removeAllItems();
		this.connectmodeCombobox.addItemsWithTitles(new NSArray(new String[]{CONNECTMODE_ACTIVE, CONNECTMODE_PASSIVE}));
		if(Preferences.instance().getProperty("ftp.connectmode").equals("passive")) {
			this.connectmodeCombobox.setTitle(CONNECTMODE_PASSIVE);
		}
		else {
			this.connectmodeCombobox.setTitle(CONNECTMODE_ACTIVE);
		}
	}

	public void connectmodeComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(CONNECTMODE_ACTIVE)) {
			Preferences.instance().setProperty("ftp.connectmode", "active");
		}
		else if(sender.selectedItem().title().equals(CONNECTMODE_PASSIVE)) {
			Preferences.instance().setProperty("ftp.connectmode", "passive");
		}
	}

	private NSPopUpButton protocolCombobox; //IBOutlet

	public void setProtocolCombobox(NSPopUpButton protocolCombobox) {
		this.protocolCombobox = protocolCombobox;
		this.protocolCombobox.setTarget(this);
		this.protocolCombobox.setAction(new NSSelector("protocolComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.protocolCombobox.removeAllItems();
		this.protocolCombobox.addItemsWithTitles(new NSArray(new String[]{PROTOCOL_FTP,
                                                                          PROTOCOL_FTP_TLS,
                                                                          PROTOCOL_SFTP}));
		if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP)) {
			this.protocolCombobox.setTitle(PROTOCOL_FTP);
		}
        if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP_TLS)) {
            this.protocolCombobox.setTitle(PROTOCOL_FTP_TLS);
        }
        if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.SFTP)) {
            this.protocolCombobox.setTitle(PROTOCOL_SFTP);
        }
	}

	public void protocolComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(PROTOCOL_FTP)) {
			Preferences.instance().setProperty("connection.protocol.default", Session.FTP);
			Preferences.instance().setProperty("connection.port.default", Session.FTP_PORT);
		}
        if(sender.selectedItem().title().equals(PROTOCOL_FTP_TLS)) {
            Preferences.instance().setProperty("connection.protocol.default", Session.FTP_TLS);
            Preferences.instance().setProperty("connection.port.default", Session.FTP_PORT);
        }
        if(sender.selectedItem().title().equals(PROTOCOL_SFTP)) {
			Preferences.instance().setProperty("connection.protocol.default", Session.SFTP);
			Preferences.instance().setProperty("connection.port.default", Session.SSH_PORT);
		}
	}

	private NSButton confirmDisconnectCheckbox; //IBOutlet

	public void setConfirmDisconnectCheckbox(NSButton confirmDisconnectCheckbox) {
		this.confirmDisconnectCheckbox = confirmDisconnectCheckbox;
		this.confirmDisconnectCheckbox.setTarget(this);
		this.confirmDisconnectCheckbox.setAction(new NSSelector("confirmDisconnectCheckboxClicked", new Class[]{NSButton.class}));
		this.confirmDisconnectCheckbox.setState(Preferences.instance().getBoolean("browser.confirmDisconnect") ? NSCell.OnState : NSCell.OffState);
	}
	
	public void confirmDisconnectCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("browser.confirmDisconnect", enabled);
	}
	
	private NSButton autoUpdateCheckbox; //IBOutlet

	public void setAutoUpdateCheckbox(NSButton autoUpdateCheckbox) {
		this.autoUpdateCheckbox = autoUpdateCheckbox;
		this.autoUpdateCheckbox.setTarget(this);
		this.autoUpdateCheckbox.setAction(new NSSelector("autoUpdateCheckboxClicked", new Class[]{NSButton.class}));
		this.autoUpdateCheckbox.setState(
                Preferences.instance().getBoolean("update.check") ? NSCell.OnState : NSCell.OffState);
	}

	public void autoUpdateCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("update.check", enabled);
	}

    private NSButton acceptAnyCertificateCheckbox; //IBOutlet

    public void setAcceptAnyCertificateCheckbox(NSButton acceptAnyCertificateCheckbox) {
        this.acceptAnyCertificateCheckbox = acceptAnyCertificateCheckbox;
        this.acceptAnyCertificateCheckbox.setTarget(this);
        this.acceptAnyCertificateCheckbox.setAction(new NSSelector("acceptAnyCertificateCheckboxClicked", new Class[]{NSButton.class}));
        this.acceptAnyCertificateCheckbox.setState(
                Preferences.instance().getBoolean("ftp.tls.acceptAnyCertificate") ? NSCell.OnState : NSCell.OffState);
    }

    public void acceptAnyCertificateCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("ftp.tls.acceptAnyCertificate", enabled);
    }

	private NSButton secureDataChannelCheckbox; //IBOutlet

	public void setSecureDataChannelCheckbox(NSButton secureDataChannelCheckbox) {
		this.secureDataChannelCheckbox = secureDataChannelCheckbox;
		this.secureDataChannelCheckbox.setTarget(this);
		this.secureDataChannelCheckbox.setAction(new NSSelector("secureDataChannelCheckboxClicked", new Class[]{NSButton.class}));
		this.secureDataChannelCheckbox.setState(
                Preferences.instance().getProperty("ftp.tls.datachannel").equals("P") ? NSCell.OnState : NSCell.OffState);
	}

	public void secureDataChannelCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("ftp.tls.datachannel", "P");
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("ftp.tls.datachannel", "C");
				break;
		}
	}

    private NSButton failInsecureDataChannelCheckbox; //IBOutlet

    public void setFailInsecureDataChannelCheckbox(NSButton failInsecureDataChannelCheckbox) {
        this.failInsecureDataChannelCheckbox = failInsecureDataChannelCheckbox;
        this.failInsecureDataChannelCheckbox.setTarget(this);
        this.failInsecureDataChannelCheckbox.setAction(new NSSelector("failInsecureDataChannelCheckboxClicked", new Class[]{NSButton.class}));
        this.failInsecureDataChannelCheckbox.setState(
                Preferences.instance().getBoolean("ftp.tls.datachannel.failOnError") ? NSCell.OffState : NSCell.OnState);
    }

    public void failInsecureDataChannelCheckboxClicked(NSButton sender) {
		boolean enabled = sender.state() == NSCell.OnState;
		Preferences.instance().setProperty("ftp.tls.datachannel.failOnError", !enabled);
    }
}