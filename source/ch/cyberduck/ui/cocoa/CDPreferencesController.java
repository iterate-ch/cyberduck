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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;

/**
 * @version $Id$
 */
public class CDPreferencesController {
    private static Logger log = Logger.getLogger(CDPreferencesController.class);

    private static CDPreferencesController instance;

    private static NSMutableArray instances = new NSMutableArray();

    private NSWindow window; //IBOutlet

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }

    public static CDPreferencesController instance() {
        log.debug("instance");
        if (null == instance) {
            instance = new CDPreferencesController();
            if (false == NSApplication.loadNibNamed("Preferences", instance)) {
                log.fatal("Couldn't load Preferences.nib");
            }
            instance.window().makeKeyAndOrderFront(null);
        }
        return instance;
    }

    private CDPreferencesController() {
        log.debug("CDPreferencesController");
        instances.addObject(this);
    }

    public void awakeFromNib() {
        log.debug("awakeFromNib");
        this.window.center();
    }

    public NSWindow window() {
        return this.window;
    }

    public void windowWillClose(NSNotification notification) {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        instances.removeObject(this);
        instance = null;
    }

    private static final String CONNECTMODE_ACTIVE = NSBundle.localizedString("Active", "");
    private static final String CONNECTMODE_PASSIVE = NSBundle.localizedString("Passive", "");

    private static final String TRANSFERMODE_BINARY = NSBundle.localizedString("Binary", "");
    private static final String TRANSFERMODE_ASCII = NSBundle.localizedString("ASCII", "");

    private static final String PROTOCOL_FTP = "FTP";
    private static final String PROTOCOL_SFTP = "SFTP";

    private static final String ASK_ME_WHAT_TO_DO = NSBundle.localizedString("Ask me what to do", "");
    private static final String OVERWRITE_EXISTING_FILE = NSBundle.localizedString("Overwrite existing file", "");
    private static final String TRY_TO_RESUME_TRANSFER = NSBundle.localizedString("Try to resume transfer", "");
    private static final String USE_A_SIMILAR_NAME = NSBundle.localizedString("Use similar name", "");

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSPopUpButton encodingCombobox;

    public void setEncodingCombobox(NSPopUpButton encodingCombobox) {
        this.encodingCombobox = encodingCombobox;
        this.encodingCombobox.setTarget(this);
        this.encodingCombobox.setAction(new NSSelector("encodingComboboxClicked", new Class[]{NSPopUpButton.class}));
        this.encodingCombobox.removeAllItems();
        java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
        String[] items = new String[charsets.size()];
        java.util.Iterator iterator = charsets.values().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            items[i] = ((java.nio.charset.Charset) iterator.next()).name();
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
        this.listCheckbox.setState(Preferences.instance().getProperty("ftp.sendExtendedListCommand").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void listCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("ftp.sendExtendedListCommand", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("ftp.sendExtendedListCommand", false);
                break;
        }
    }

    private NSButton systCheckbox; //IBOutlet

    public void setSystCheckbox(NSButton systCheckbox) {
        this.systCheckbox = systCheckbox;
        this.systCheckbox.setTarget(this);
        this.systCheckbox.setAction(new NSSelector("systCheckboxClicked", new Class[]{NSButton.class}));
        this.systCheckbox.setState(Preferences.instance().getProperty("ftp.sendSystemCommand").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void systCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("ftp.sendSystemCommand", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("ftp.sendSystemCommand", false);
                break;
        }
    }

    private NSButton chmodCheckbox; //IBOutlet

    public void setChmodCheckbox(NSButton chmodCheckbox) {
        this.chmodCheckbox = chmodCheckbox;
        this.chmodCheckbox.setTarget(this);
        this.chmodCheckbox.setAction(new NSSelector("chmodCheckboxClicked", new Class[]{NSButton.class}));
        this.chmodCheckbox.setState(Preferences.instance().getProperty("queue.upload.changePermissions").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void chmodCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("queue.upload.changePermissions", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("queue.upload.changePermissions", false);
                break;
        }
    }

    private NSButton horizontalLinesCheckbox; //IBOutlet

    public void setHorizontalLinesCheckbox(NSButton horizontalLinesCheckbox) {
        this.horizontalLinesCheckbox = horizontalLinesCheckbox;
        this.horizontalLinesCheckbox.setTarget(this);
        this.horizontalLinesCheckbox.setAction(new NSSelector("horizontalLinesCheckboxClicked", new Class[]{NSButton.class}));
        this.horizontalLinesCheckbox.setState(Preferences.instance().getProperty("browser.horizontalLines").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void horizontalLinesCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.horizontalLines", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.horizontalLines", false);
                break;
        }
    }

    private NSButton verticalLinesCheckbox; //IBOutlet

    public void setVerticalLinesCheckbox(NSButton verticalLinesCheckbox) {
        this.verticalLinesCheckbox = verticalLinesCheckbox;
        this.verticalLinesCheckbox.setTarget(this);
        this.verticalLinesCheckbox.setAction(new NSSelector("verticalLinesCheckboxClicked", new Class[]{NSButton.class}));
        this.verticalLinesCheckbox.setState(Preferences.instance().getProperty("browser.verticalLines").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void verticalLinesCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.verticalLines", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.verticalLines", false);
                break;
        }
    }

    private NSButton alternatingRowBackgroundCheckbox; //IBOutlet

    public void setAlternatingRowBackgroundCheckbox(NSButton alternatingRowBackgroundCheckbox) {
        this.alternatingRowBackgroundCheckbox = alternatingRowBackgroundCheckbox;
        this.alternatingRowBackgroundCheckbox.setTarget(this);
        this.alternatingRowBackgroundCheckbox.setAction(new NSSelector("alternatingRowBackgroundCheckboxClicked", new Class[]{NSButton.class}));
        this.alternatingRowBackgroundCheckbox.setState(Preferences.instance().getProperty("browser.alternatingRows").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void alternatingRowBackgroundCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.alternatingRows", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.alternatingRows", false);
                break;
        }
    }

    private NSButton columnModificationCheckbox; //IBOutlet

    public void setColumnModificationCheckbox(NSButton columnModificationCheckbox) {
        this.columnModificationCheckbox = columnModificationCheckbox;
        this.columnModificationCheckbox.setTarget(this);
        this.columnModificationCheckbox.setAction(new NSSelector("columnModificationCheckboxClicked", new Class[]{NSButton.class}));
        this.columnModificationCheckbox.setState(Preferences.instance().getProperty("browser.columnModification").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void columnModificationCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.columnModification", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.columnModification", false);
                break;
        }
    }

    private NSButton columnOwnerCheckbox; //IBOutlet

    public void setColumnOwnerCheckbox(NSButton columnOwnerCheckbox) {
        this.columnOwnerCheckbox = columnOwnerCheckbox;
        this.columnOwnerCheckbox.setTarget(this);
        this.columnOwnerCheckbox.setAction(new NSSelector("columnOwnerCheckboxClicked", new Class[]{NSButton.class}));
        this.columnOwnerCheckbox.setState(Preferences.instance().getProperty("browser.columnOwner").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void columnOwnerCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.columnOwner", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.columnOwner", false);
                break;
        }
    }

    private NSButton columnPermissionsCheckbox; //IBOutlet

    public void setColumnPermissionsCheckbox(NSButton columnPermissionsCheckbox) {
        this.columnPermissionsCheckbox = columnPermissionsCheckbox;
        this.columnPermissionsCheckbox.setTarget(this);
        this.columnPermissionsCheckbox.setAction(new NSSelector("columnPermissionsCheckboxClicked", new Class[]{NSButton.class}));
        this.columnPermissionsCheckbox.setState(Preferences.instance().getProperty("browser.columnPermissions").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void columnPermissionsCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.columnPermissions", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.columnPermissions", false);
                break;
        }
    }

    private NSButton columnSizeCheckbox; //IBOutlet

    public void setColumnSizeCheckbox(NSButton columnSizeCheckbox) {
        this.columnSizeCheckbox = columnSizeCheckbox;
        this.columnSizeCheckbox.setTarget(this);
        this.columnSizeCheckbox.setAction(new NSSelector("columnSizeCheckboxClicked", new Class[]{NSButton.class}));
        this.columnSizeCheckbox.setState(Preferences.instance().getProperty("browser.columnSize").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void columnSizeCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.columnSize", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.columnSize", false);
                break;
        }
    }

    // public-key algorithms
    private static final String SSH_DSS = "ssh-dss";
    private static final String SSH_RSA = "ssh-rsa";

    private NSPopUpButton publickeyCombobox;

    public void setPublickeyCombobox(NSPopUpButton publickeyCombobox) {
        this.publickeyCombobox = publickeyCombobox;
        this.publickeyCombobox.setTarget(this);
        this.publickeyCombobox.setAction(new NSSelector("publickeyComboboxClicked", new Class[]{NSPopUpButton.class}));
        this.publickeyCombobox.removeAllItems();
        this.publickeyCombobox.addItemsWithTitles(new NSArray(new String[]{
            NSBundle.localizedString("Default", ""),
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
    private static final String twofish196_cbc = "twofish196-cbc";
    private static final String twofish128_cbc = "twofish128-cbc";
    private static final String aes256_cbc = "aes256-cbc";
    private static final String aes196_cbc = "aes196-cbc";
    private static final String aes128_cbc = "aes128-cbc";
    private static final String cast128_cbc = "cast128-cbc";

    private NSPopUpButton csEncryptionCombobox; //IBOutlet

    public void setCsEncryptionCombobox(NSPopUpButton csEncryptionCombobox) {
        this.csEncryptionCombobox = csEncryptionCombobox;
        this.csEncryptionCombobox.setTarget(this);
        this.csEncryptionCombobox.setAction(new NSSelector("csEncryptionComboboxClicked", new Class[]{NSPopUpButton.class}));
        this.csEncryptionCombobox.removeAllItems();
        this.csEncryptionCombobox.addItemsWithTitles(new NSArray(new String[]{
            NSBundle.localizedString("Default", ""),
            des_cbc,
            blowfish_cbc,
            twofish256_cbc,
            twofish196_cbc,
            twofish128_cbc,
            aes256_cbc,
            aes196_cbc,
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
            NSBundle.localizedString("Default", ""),
            des_cbc,
            blowfish_cbc,
            twofish256_cbc,
            twofish196_cbc,
            twofish128_cbc,
            aes256_cbc,
            aes196_cbc,
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
            NSBundle.localizedString("Default", ""),
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
            NSBundle.localizedString("Default", ""),
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

    private NSButton downloadPathButton; //IBOutlet

    public void setDownloadPathButton(NSButton downloadPathButton) {
        this.downloadPathButton = downloadPathButton;
        this.downloadPathButton.setTarget(this);
        this.downloadPathButton.setAction(new NSSelector("downloadPathButtonClicked", new Class[]{NSButton.class}));
    }

    public void downloadPathButtonClicked(NSButton sender) {
        NSOpenPanel panel = new NSOpenPanel();
        panel.setCanChooseFiles(false);
        panel.setCanChooseDirectories(true);
        panel.setAllowsMultipleSelection(false);
        panel.beginSheetForDirectory(System.getProperty("user.home"), null, null, this.window, this, new NSSelector("openPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
    }

    public void openPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn):
                {
                    NSArray selected = sheet.filenames();
                    String filename;
                    if ((filename = (String) selected.lastObject()) != null) {
                        Preferences.instance().setProperty("queue.download.folder", filename);
                        this.downloadPathField.setStringValue(Preferences.instance().getProperty("queue.download.folder"));
                    }
                    break;
                }
            case (NSAlertPanel.AlternateReturn):
                {
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
            int bytes = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
            int kbit = bytes / 1024 * 8;
            this.bufferField.setStringValue("" + kbit);
        }
        catch (NumberFormatException e) {
            log.error(e.getMessage());
        }
    }

    private NSTextField bufferField; //IBOutlet

    public void setBufferField(NSTextField bufferField) {
        this.bufferField = bufferField;
        try {
            int bytes = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
            int kbit = bytes / 1024 * 8;
            this.bufferField.setStringValue("" + kbit);
        }
        catch (NumberFormatException e) {
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
            Preferences.instance().setProperty("connection.buffer", (int) kbit / 8 * 1024); //Bytes
        }
        catch (NumberFormatException e) {
            log.error(e.getMessage());
        }
    }

    private NSTextField userAgentField; //IBOutlet

    public void setUserAgentField(NSTextField userAgentField) {
        this.userAgentField = userAgentField;
        this.userAgentField.setStringValue(Preferences.instance().getProperty("http.agent"));
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

    private NSTextField historyField; //IBOutlet

    public void setHistoryField(NSTextField historyField) {
        this.historyField = historyField;
        this.historyField.setStringValue(Preferences.instance().getProperty("history.size"));
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("historyFieldDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                this.historyField);
    }

    public void historyFieldDidChange(NSNotification sender) {
        if (this.historyField.stringValue() != null && !this.historyField.stringValue().equals("")) {
            int size = Integer.parseInt(this.historyField.stringValue());
            Preferences.instance().setProperty("history.size", size);
            while (CDHistoryImpl.instance().size() > size) {
                CDHistoryImpl.instance().removeItem(CDHistoryImpl.instance().size() - 1);
            }
        }
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
        this.keychainCheckbox.setState(Preferences.instance().getProperty("connection.login.useKeychain").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void keychainCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("connection.login.useKeychain", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("connection.login.useKeychain", false);
                break;
        }
    }

    private NSButton showHiddenCheckbox; //IBOutlet

    public void setShowHiddenCheckbox(NSButton showHiddenCheckbox) {
        this.showHiddenCheckbox = showHiddenCheckbox;
        this.showHiddenCheckbox.setTarget(this);
        this.showHiddenCheckbox.setAction(new NSSelector("showHiddenCheckboxClicked", new Class[]{NSButton.class}));
        this.showHiddenCheckbox.setState(Preferences.instance().getProperty("browser.showHidden").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void showHiddenCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.showHidden", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.showHidden", false);
                break;
        }
    }

    private NSButton newBrowserCheckbox; //IBOutlet

    public void setNewBrowserCheckbox(NSButton newBrowserCheckbox) {
        this.newBrowserCheckbox = newBrowserCheckbox;
        this.newBrowserCheckbox.setTarget(this);
        this.newBrowserCheckbox.setAction(new NSSelector("newBrowserCheckboxClicked", new Class[]{NSButton.class}));
        this.newBrowserCheckbox.setState(Preferences.instance().getProperty("browser.openByDefault").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void newBrowserCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("browser.openByDefault", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("browser.openByDefault", false);
                break;
        }
    }

    private NSButton bringQueueToFrontCheckbox; //IBOutlet

    public void setBringQueueToFrontCheckbox(NSButton bringQueueToFrontCheckbox) {
        this.bringQueueToFrontCheckbox = bringQueueToFrontCheckbox;
        this.bringQueueToFrontCheckbox.setTarget(this);
        this.bringQueueToFrontCheckbox.setAction(new NSSelector("bringQueueToFrontCheckboxClicked", new Class[]{NSButton.class}));
        this.bringQueueToFrontCheckbox.setState(Preferences.instance().getProperty("queue.orderFrontOnTransfer").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void bringQueueToFrontCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("queue.orderFrontOnTransfer", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("queue.orderFrontOnTransfer", false);
                break;
        }
    }

    private NSButton removeFromQueueCheckbox; //IBOutlet

    public void setRemoveFromQueueCheckbox(NSButton removeFromQueueCheckbox) {
        this.removeFromQueueCheckbox = removeFromQueueCheckbox;
        this.removeFromQueueCheckbox.setTarget(this);
        this.removeFromQueueCheckbox.setAction(new NSSelector("removeFromQueueCheckboxClicked", new Class[]{NSButton.class}));
        this.removeFromQueueCheckbox.setState(Preferences.instance().getProperty("queue.removeItemWhenComplete").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void removeFromQueueCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("queue.removeItemWhenComplete", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("queue.removeItemWhenComplete", false);
                break;
        }
    }

    private NSButton processCheckbox; //IBOutlet

    public void setProcessCheckbox(NSButton processCheckbox) {
        this.processCheckbox = processCheckbox;
        this.processCheckbox.setTarget(this);
        this.processCheckbox.setAction(new NSSelector("processCheckboxClicked", new Class[]{NSButton.class}));
        this.processCheckbox.setState(Preferences.instance().getProperty("queue.postProcessItemWhenComplete").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void processCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("queue.postProcessItemWhenComplete", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("queue.postProcessItemWhenComplete", false);
                break;
        }
    }

    private NSPopUpButton duplicateCombobox; //IBOutlet

    public void setDuplicateCombobox(NSPopUpButton duplicateCombobox) {
        this.duplicateCombobox = duplicateCombobox;
        this.duplicateCombobox.setTarget(this);
        this.duplicateCombobox.setAction(new NSSelector("duplicateComboboxClicked", new Class[]{NSPopUpButton.class}));
        this.duplicateCombobox.removeAllItems();
        this.duplicateCombobox.addItemsWithTitles(new NSArray(new String[]{ASK_ME_WHAT_TO_DO, OVERWRITE_EXISTING_FILE, TRY_TO_RESUME_TRANSFER, USE_A_SIMILAR_NAME}));
        if (Preferences.instance().getProperty("queue.download.duplicate").equals("ask")) {
            this.duplicateCombobox.setTitle(ASK_ME_WHAT_TO_DO);
        }
        if (Preferences.instance().getProperty("queue.download.duplicate").equals("overwrite")) {
            this.duplicateCombobox.setTitle(OVERWRITE_EXISTING_FILE);
        }
        else if (Preferences.instance().getProperty("queue.download.duplicate").equals("resume")) {
            this.duplicateCombobox.setTitle(TRY_TO_RESUME_TRANSFER);
        }
        else if (Preferences.instance().getProperty("queue.download.duplicate").equals("similar")) {
            this.duplicateCombobox.setTitle(USE_A_SIMILAR_NAME);
        }
    }

    public void duplicateComboboxClicked(NSPopUpButton sender) {
        if (sender.selectedItem().title().equals(ASK_ME_WHAT_TO_DO)) {
            Preferences.instance().setProperty("queue.download.duplicate", "ask");
        }
        if (sender.selectedItem().title().equals(OVERWRITE_EXISTING_FILE)) {
            Preferences.instance().setProperty("queue.download.duplicate", "overwrite");
        }
        else if (sender.selectedItem().title().equals(TRY_TO_RESUME_TRANSFER)) {
            Preferences.instance().setProperty("queue.download.duplicate", "resume");
        }
        else if (sender.selectedItem().title().equals(USE_A_SIMILAR_NAME)) {
            Preferences.instance().setProperty("queue.download.duplicate", "similar");
        }
    }

    private NSPopUpButton transfermodeCombobox; //IBOutlet

    public void setTransfermodeCombobox(NSPopUpButton transfermodeCombobox) {
        this.transfermodeCombobox = transfermodeCombobox;
        this.transfermodeCombobox.setTarget(this);
        this.transfermodeCombobox.setAction(new NSSelector("transfermodeComboboxClicked", new Class[]{NSPopUpButton.class}));
        this.transfermodeCombobox.removeAllItems();
        this.transfermodeCombobox.addItemsWithTitles(new NSArray(new String[]{TRANSFERMODE_BINARY, TRANSFERMODE_ASCII}));
        if (Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
            this.transfermodeCombobox.setTitle(TRANSFERMODE_BINARY);
        }
        else {
            this.transfermodeCombobox.setTitle(TRANSFERMODE_ASCII);
        }
    }

    public void transfermodeComboboxClicked(NSPopUpButton sender) {
        if (sender.selectedItem().title().equals(TRANSFERMODE_ASCII)) {
            Preferences.instance().setProperty("ftp.transfermode", "ascii");
        }
        else {
            Preferences.instance().setProperty("ftp.transfermode", "binary");
        }
    }

    private NSPopUpButton connectmodeCombobox; //IBOutlet

    public void setConnectmodeCombobox(NSPopUpButton connectmodeCombobox) {
        this.connectmodeCombobox = connectmodeCombobox;
        this.connectmodeCombobox.setTarget(this);
        this.connectmodeCombobox.setAction(new NSSelector("connectmodeComboboxClicked", new Class[]{NSPopUpButton.class}));
        this.connectmodeCombobox.removeAllItems();
        this.connectmodeCombobox.addItemsWithTitles(new NSArray(new String[]{CONNECTMODE_ACTIVE, CONNECTMODE_PASSIVE}));
        if (Preferences.instance().getProperty("ftp.connectmode").equals("passive")) {
            this.connectmodeCombobox.setTitle(CONNECTMODE_PASSIVE);
        }
        else {
            this.connectmodeCombobox.setTitle(CONNECTMODE_ACTIVE);
        }
    }

    public void connectmodeComboboxClicked(NSPopUpButton sender) {
        if (sender.selectedItem().title().equals(CONNECTMODE_ACTIVE)) {
            Preferences.instance().setProperty("ftp.connectmode", "active");
        }
        else {
            Preferences.instance().setProperty("ftp.connectmode", "passive");
        }
    }

    private NSPopUpButton protocolCombobox; //IBOutlet

    public void setProtocolCombobox(NSPopUpButton protocolCombobox) {
        this.protocolCombobox = protocolCombobox;
        this.protocolCombobox.setTarget(this);
        this.protocolCombobox.setAction(new NSSelector("protocolComboboxClicked", new Class[]{NSPopUpButton.class}));
        this.protocolCombobox.removeAllItems();
        this.protocolCombobox.addItemsWithTitles(new NSArray(new String[]{PROTOCOL_FTP, PROTOCOL_SFTP}));
        if (Preferences.instance().getProperty("connection.protocol.default").equals("ftp")) {
            this.protocolCombobox.setTitle(PROTOCOL_FTP);
        }
        else {
            this.protocolCombobox.setTitle(PROTOCOL_SFTP);
        }
    }

    public void protocolComboboxClicked(NSPopUpButton sender) {
        if (sender.selectedItem().title().equals(PROTOCOL_FTP)) {
            Preferences.instance().setProperty("connection.protocol.default", Session.FTP);
            Preferences.instance().setProperty("connection.port.default", Session.FTP_PORT);
        }
        else {
            Preferences.instance().setProperty("connection.protocol.default", Session.SFTP);
            Preferences.instance().setProperty("connection.port.default", Session.SSH_PORT);
        }
    }
}
