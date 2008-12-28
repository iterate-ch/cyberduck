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

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.cocoa.util.HyperlinkAttributedStringFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.spearce.jgit.transport.OpenSshConfig;

import java.util.HashMap;
import java.util.Map;

import com.enterprisedt.net.ftp.FTPConnectMode;

/**
 * @version $Id$
 */
public class CDConnectionController extends CDSheetController {
    private static Logger log = Logger.getLogger(CDConnectionController.class);

    private NSPopUpButton protocolPopup;

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setTarget(this);
        this.protocolPopup.setAction(new NSSelector("protocolSelectionDidChange", new Class[]{Object.class}));
        this.protocolPopup.removeAllItems();
        this.protocolPopup.addItemsWithTitles(new NSArray(Protocol.getProtocolDescriptions()));
        final Protocol[] protocols = Protocol.getKnownProtocols();
        for(int i = 0; i < protocols.length; i++) {
            final NSMenuItem item = this.protocolPopup.itemWithTitle(protocols[i].getDescription());
            item.setRepresentedObject(protocols[i]);
            item.setImage(CDIconCache.instance().iconForName(protocols[i].icon(), 16));
        }
        final Protocol defaultProtocol
                = Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"));
        this.protocolPopup.selectItemWithTitle(defaultProtocol.getDescription());
    }

    public void protocolSelectionDidChange(final NSPopUpButton sender) {
        log.debug("protocolSelectionDidChange:" + sender);
        final Protocol protocol = (Protocol) protocolPopup.selectedItem().representedObject();
        portField.setIntValue(protocol.getDefaultPort());
        if(!protocol.isConfigurable()) {
            hostField.setEnabled(false);
            portField.setEnabled(false);
            encodingPopup.selectItemWithTitle(DEFAULT);
            hostField.setStringValue(protocol.getDefaultHostname());
            if(protocol.equals(Protocol.S3)) {
                ((NSTextFieldCell) usernameField.cell()).setPlaceholderString(
                        NSBundle.localizedString("Access Key ID", "S3", "")
                );
                ((NSTextFieldCell) passField.cell()).setPlaceholderString(
                        NSBundle.localizedString("Secret Access Key", "S3", "")
                );
            }
            if(protocol.equals(Protocol.MOSSO)) {
                ((NSTextFieldCell) usernameField.cell()).setPlaceholderString("");
                ((NSTextFieldCell) passField.cell()).setPlaceholderString(
                        NSBundle.localizedString("API Access Key", "Mosso", "")
                );
            }
            if(protocol.equals(Protocol.IDISK)) {
                CDDotMacController controller = new CDDotMacController();
                final String member = controller.getAccountName();
                controller.invalidate();
                ((NSTextFieldCell) usernameField.cell()).setPlaceholderString(
                        NSBundle.localizedString("MobileMe Member Name", "IDisk", "")
                );
                if(null != member) {
                    // Account name configured in System Preferences
                    usernameField.setStringValue(member);
                    usernameField.setEnabled(false);
                    pathField.setStringValue(Path.DELIMITER + member);
                    pathField.setEnabled(false);
                }
            }
        }
        else {
            if(!hostField.isEnabled()) {
                // Was previously configured with a static configuration
                hostField.setStringValue("");
            }
            if(!pathField.isEnabled()) {
                // Was previously configured with a static configuration
                pathField.setStringValue("");
            }
            usernameField.setEnabled(true);
            hostField.setEnabled(true);
            portField.setEnabled(true);
            pathField.setEnabled(true);
            ((NSTextFieldCell) usernameField.cell()).setPlaceholderString("");
            ((NSTextFieldCell) passField.cell()).setPlaceholderString("");
        }
        connectmodePopup.setEnabled(protocol.equals(Protocol.FTP)
                || protocol.equals(Protocol.FTP_TLS));
        encodingPopup.setEnabled(protocol.isConfigurable());

        this.updateIdentity();
        this.updateURLLabel();

        this.reachable();
    }

    /**
     * Update Private Key selection
     *
     * @param protocol
     */
    private void updateIdentity() {
        final Protocol protocol = (Protocol) protocolPopup.selectedItem().representedObject();
        pkCheckbox.setEnabled(protocol.equals(Protocol.SFTP));
        if(protocol.equals(Protocol.SFTP)) {
            if(StringUtils.isNotEmpty(hostField.stringValue())) {
                final OpenSshConfig.Host entry = OpenSshConfig.create().lookup(hostField.stringValue());
                if(null != entry.getIdentityFile()) {
                    if(pkCheckbox.state() == NSCell.OffState) {
                        // No previously manually selected key
                        pkLabel.setStringValue(NSPathUtilities.stringByAbbreviatingWithTildeInPath(
                                entry.getIdentityFile().getAbsolutePath()));
                        pkCheckbox.setState(NSCell.OnState);
                    }
                }
                else {
                    pkCheckbox.setState(NSCell.OffState);
                    pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
                }
                if(StringUtils.isNotBlank(entry.getUser())) {
                    usernameField.setStringValue(entry.getUser());
                }
            }
        }
        else {
            pkCheckbox.setState(NSCell.OffState);
            pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
    }

    private NSComboBox hostField;
    private NSObject hostPopupDataSource;

    public void setHostPopup(NSComboBox hostPopup) {
        this.hostField = hostPopup;
        this.hostField.setTarget(this);
        this.hostField.setAction(new NSSelector("hostPopupSelectionDidChange", new Class[]{Object.class}));
        this.hostField.setUsesDataSource(true);
        this.hostField.setDataSource(this.hostPopupDataSource = new NSObject/*NSComboBox.DataSource*/() {
            public int numberOfItemsInComboBox(final NSComboBox sender) {
                return HostCollection.defaultCollection().size();
            }

            public Object comboBoxObjectValueForItemAtIndex(final NSComboBox sender, final int row) {
                if(row < this.numberOfItemsInComboBox(sender)) {
                    return HostCollection.defaultCollection().get(row).getNickname();
                }
                return null;
            }
        });
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("hostFieldTextDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.hostField);
    }

    public void hostPopupSelectionDidChange(final NSControl sender) {
        String input = sender.stringValue();
        if(StringUtils.isBlank(input)) {
            return;
        }
        input = input.trim();
        // First look for equivalent bookmarks
        for(Host h : HostCollection.defaultCollection()) {
            if(h.getNickname().equals(input)) {
                this.hostChanged(new Host(h.getAsDictionary()));
                break;
            }
        }
    }

    public void hostFieldTextDidChange(final NSNotification sender) {
        if(Protocol.isURL(hostField.stringValue())) {
            final Host parsed = Host.parse(hostField.stringValue());
            this.hostChanged(parsed);
        }
        else {
            this.updateURLLabel();
            this.updateIdentity();
            this.reachable();
        }
    }

    /**
     *
     * @param host
     */
    private void hostChanged(final Host host) {
        this.updateField(hostField, host.getHostname());
        protocolPopup.selectItemWithTitle(host.getProtocol().getDescription());
        this.updateField(portField, String.valueOf(host.getPort()));
        this.updateField(usernameField, host.getCredentials().getUsername());
        this.updateField(pathField, host.getDefaultPath());
        anonymousCheckbox.setState(host.getCredentials().isAnonymousLogin() ? NSCell.OnState : NSCell.OffState);
        this.anonymousCheckboxClicked(anonymousCheckbox);
        if(host.getCredentials().isPublicKeyAuthentication()) {
            pkCheckbox.setState(NSCell.OnState);
            pkLabel.setStringValue(host.getCredentials().getIdentity());
        }
        else {
            this.updateIdentity();
        }
        this.updateURLLabel();
        this.readPasswordFromKeychain();
        this.reachable();
    }

    /**
     * Run the connection reachability test in the background
     */
    private void reachable() {
        final String hostname = hostField.stringValue();
        if(StringUtils.isNotBlank(hostname)) {
            this.background(new AbstractBackgroundAction() {
                boolean reachable = false;

                public void run() {
                    reachable = new Host(hostname).isReachable();
                }

                public void cleanup() {
                    alertIcon.setHidden(reachable);
                }
            });
        }
        else {
            alertIcon.setHidden(true);
        }
    }

    private NSButton alertIcon; // IBOutlet

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setHidden(true);
        this.alertIcon.setTarget(this);
        this.alertIcon.setAction(new NSSelector("launchNetworkAssistant", new Class[]{NSButton.class}));
    }

    public void launchNetworkAssistant(final NSButton sender) {
        Host.parse(urlLabel.stringValue()).diagnose();
    }

    private NSTextField pathField;

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("pathInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                this.pathField);
    }

    public void pathInputDidEndEditing(final NSNotification sender) {
        this.updateURLLabel();
        if(StringUtils.isBlank(pathField.stringValue())) {
            return;
        }
        this.pathField.setStringValue(Path.normalize(pathField.stringValue(), false));
    }

    private NSTextField portField;

    public void setPortField(NSTextField portField) {
        this.portField = portField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("portFieldTextDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.portField);
    }

    public void portFieldTextDidChange(final NSNotification sender) {
        if(null == this.portField.stringValue() || this.portField.stringValue().equals("")) {
            final Protocol protocol = (Protocol) protocolPopup.selectedItem().representedObject();
            this.portField.setStringValue(String.valueOf(protocol.getDefaultPort()));
        }
        this.updateURLLabel();
    }

    private NSTextField usernameField;

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
        this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("usernameFieldTextDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.usernameField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("usernameFieldTextDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                this.usernameField);
    }

    public void usernameFieldTextDidChange(final NSNotification sender) {
        this.updateURLLabel();
    }

    public void usernameFieldTextDidEndEditing(final NSNotification sender) {
        this.readPasswordFromKeychain();
    }

    private NSTextField passField;

    public void setPassField(NSTextField passField) {
        this.passField = passField;
    }

    private NSTextField pkLabel;

    public void setPkLabel(NSTextField pkLabel) {
        this.pkLabel = pkLabel;
        this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
    }

    private NSButton keychainCheckbox;

    public void setKeychainCheckbox(NSButton keychainCheckbox) {
        this.keychainCheckbox = keychainCheckbox;
        this.keychainCheckbox.setState(NSCell.OffState);
    }

    private NSButton anonymousCheckbox; //IBOutlet

    public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
        this.anonymousCheckbox = anonymousCheckbox;
        this.anonymousCheckbox.setTarget(this);
        this.anonymousCheckbox.setAction(new NSSelector("anonymousCheckboxClicked", new Class[]{NSButton.class}));
        this.anonymousCheckbox.setState(NSCell.OffState);
    }

    public void anonymousCheckboxClicked(final NSButton sender) {
        if(sender.state() == NSCell.OnState) {
            this.usernameField.setEnabled(false);
            this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.anon.name"));
            this.passField.setEnabled(false);
            this.passField.setStringValue("");
        }
        if(sender.state() == NSCell.OffState) {
            this.usernameField.setEnabled(true);
            this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
            this.passField.setEnabled(true);
        }
        this.updateURLLabel();
    }

    private NSButton pkCheckbox;

    public void setPkCheckbox(NSButton pkCheckbox) {
        this.pkCheckbox = pkCheckbox;
        this.pkCheckbox.setTarget(this);
        this.pkCheckbox.setAction(new NSSelector("pkCheckboxSelectionDidChange", new Class[]{Object.class}));
        this.pkCheckbox.setState(NSCell.OffState);
        this.pkCheckbox.setEnabled(
                Preferences.instance().getProperty("connection.protocol.default").equals(Protocol.SFTP.getIdentifier()));
    }

    private NSOpenPanel publicKeyPanel;

    public void pkCheckboxSelectionDidChange(final NSButton sender) {
        log.debug("pkCheckboxSelectionDidChange");
        if(sender.state() == NSCell.OnState) {
            publicKeyPanel = NSOpenPanel.openPanel();
            publicKeyPanel.setCanChooseDirectories(false);
            publicKeyPanel.setCanChooseFiles(true);
            publicKeyPanel.setAllowsMultipleSelection(false);
            publicKeyPanel.beginSheetForDirectory(NSPathUtilities.stringByExpandingTildeInPath("~/.ssh"),
                    null,
                    null,
                    this.window(),
                    this,
                    new NSSelector("pkSelectionPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                    null);
        }
        else {
            this.passField.setEnabled(true);
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
    }

    public void pkSelectionPanelDidEnd(NSOpenPanel window, int returncode, Object context) {
        if(NSPanel.OKButton == returncode) {
            NSArray selected = window.filenames();
            java.util.Enumeration enumerator = selected.objectEnumerator();
            while(enumerator.hasMoreElements()) {
                String pk = NSPathUtilities.stringByAbbreviatingWithTildeInPath(
                        (String) enumerator.nextElement());
                this.pkLabel.setStringValue(pk);
            }
            this.passField.setEnabled(false);
        }
        if(NSPanel.CancelButton == returncode) {
            this.passField.setEnabled(true);
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
        publicKeyPanel = null;
    }

    private NSTextField urlLabel;

    public void setUrlLabel(NSTextField urlLabel) {
        this.urlLabel = urlLabel;
        this.urlLabel.setAllowsEditingTextAttributes(true);
        this.urlLabel.setSelectable(true);
    }

    private NSPopUpButton encodingPopup; // IBOutlet

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItem(DEFAULT);
        this.encodingPopup.menu().addItem(new NSMenuItem().separatorItem());
        this.encodingPopup.addItemsWithTitles(new NSArray(((CDMainController) NSApplication.sharedApplication().delegate()).availableCharsets()));
        this.encodingPopup.selectItemWithTitle(DEFAULT);
    }

    private NSPopUpButton connectmodePopup; //IBOutlet

    private static final String CONNECTMODE_ACTIVE = NSBundle.localizedString("Active", "");
    private static final String CONNECTMODE_PASSIVE = NSBundle.localizedString("Passive", "");

    public void setConnectmodePopup(NSPopUpButton connectmodePopup) {
        this.connectmodePopup = connectmodePopup;
        this.connectmodePopup.removeAllItems();
        this.connectmodePopup.addItem(DEFAULT);
        this.connectmodePopup.menu().addItem(new NSMenuItem().separatorItem());
        this.connectmodePopup.addItemsWithTitles(new NSArray(new String[]{CONNECTMODE_ACTIVE, CONNECTMODE_PASSIVE}));
        this.connectmodePopup.selectItemWithTitle(DEFAULT);
    }

    private NSButton toggleOptionsButton; //IBOutlet

    public void setToggleOptionsButton(NSButton b) {
        this.toggleOptionsButton = b;
    }

    private static final Map<CDWindowController, CDConnectionController> controllers
            = new HashMap<CDWindowController, CDConnectionController>();

    public static CDConnectionController instance(final CDWindowController parent) {
        if(!controllers.containsKey(parent)) {
            final CDConnectionController controller = new CDConnectionController(parent) {
                protected void invalidate() {
                    controllers.remove(parent);
                    super.invalidate();
                }
            };
            controller.loadBundle("Connection");
            controllers.put(parent, controller);
        }
        final CDConnectionController c = controllers.get(parent);
        c.passField.setStringValue("");
        return c;
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * @param parent
     */
    private CDConnectionController(final CDWindowController parent) {
        super(parent);
    }

    protected String getBundleName() {
        return null;
    }

    public void awakeFromNib() {
        this.protocolSelectionDidChange(null);
        this.setState(this.toggleOptionsButton, Preferences.instance().getBoolean("connection.toggle.options"));

        super.awakeFromNib();
    }

    /**
     * Updating the password field with the actual password if any
     * is avaialble for this hostname
     */
    public void readPasswordFromKeychain() {
        if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
            if(hostField.stringValue() != null && !hostField.stringValue().equals("") &&
                    usernameField.stringValue() != null && !usernameField.stringValue().equals("")) {
                final Protocol protocol = (Protocol) protocolPopup.selectedItem().representedObject();
                Credentials l = new Credentials(usernameField.stringValue(), null);
                this.updateField(this.passField, l.getInternetPasswordFromKeychain(protocol, hostField.stringValue()));
            }
        }
    }

    /**
     */
    private void updateURLLabel() {
        if(StringUtils.isNotBlank(hostField.stringValue())) {
            final Protocol protocol = (Protocol) protocolPopup.selectedItem().representedObject();
            final String url = protocol.getScheme() + "://" + usernameField.stringValue()
                    + "@" + hostField.stringValue() + ":" + portField.stringValue()
                    + Path.normalize(pathField.stringValue());
            urlLabel.setAttributedStringValue(
                    HyperlinkAttributedStringFactory.create(
                            new NSMutableAttributedString(new NSAttributedString(url, TRUNCATE_MIDDLE_ATTRIBUTES)), url)
            );
        }
        else {
            urlLabel.setStringValue(hostField.stringValue());
        }
    }

    public void helpButtonClicked(final Object sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(
                    new java.net.URL(Preferences.instance().getProperty("website.help")
                            + "/" + ((Protocol) protocolPopup.selectedItem().representedObject()).getIdentifier())
            );
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.window().endEditingForObject(null);
            Host host = new Host(
                    (Protocol) protocolPopup.selectedItem().representedObject(),
                    hostField.stringValue(),
                    Integer.parseInt(portField.stringValue()),
                    pathField.stringValue());
            if(protocolPopup.selectedItem().representedObject().equals(Protocol.FTP) ||
                    protocolPopup.selectedItem().representedObject().equals(Protocol.FTP)) {
                if(connectmodePopup.titleOfSelectedItem().equals(DEFAULT)) {
                    host.setFTPConnectMode(null);
                }
                else if(connectmodePopup.titleOfSelectedItem().equals(CONNECTMODE_ACTIVE)) {
                    host.setFTPConnectMode(FTPConnectMode.ACTIVE);
                }
                else if(connectmodePopup.titleOfSelectedItem().equals(CONNECTMODE_PASSIVE)) {
                    host.setFTPConnectMode(FTPConnectMode.PASV);
                }
            }
            final Credentials credentials = host.getCredentials();
            credentials.setUsername(usernameField.stringValue());
            credentials.setPassword(passField.stringValue());
            credentials.setUseKeychain(keychainCheckbox.state() == NSCell.OnState);
            if(protocolPopup.selectedItem().representedObject().equals(Protocol.SFTP)) {
                if(pkCheckbox.state() == NSCell.OnState) {
                    credentials.setIdentity(pkLabel.stringValue());
                }
            }
            if(encodingPopup.titleOfSelectedItem().equals(DEFAULT)) {
                host.setEncoding(null);
            }
            else {
                host.setEncoding(encodingPopup.titleOfSelectedItem());
            }
            ((CDBrowserController) parent).mount(host);
        }
        Preferences.instance().setProperty("connection.toggle.options", this.toggleOptionsButton.state());
    }
}
