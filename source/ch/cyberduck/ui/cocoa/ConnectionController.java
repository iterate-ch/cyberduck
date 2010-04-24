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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;
import org.spearce.jgit.transport.OpenSshConfig;

import java.util.HashMap;
import java.util.Map;

import com.enterprisedt.net.ftp.FTPConnectMode;

/**
 * @version $Id$
 */
public class ConnectionController extends SheetController {
    private static Logger log = Logger.getLogger(ConnectionController.class);

    @Outlet
    private NSPopUpButton protocolPopup;

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionDidChange:"));
        this.protocolPopup.removeAllItems();
        for(Protocol protocol : Protocol.getKnownProtocols()) {
            final String title = protocol.getDescription();
            this.protocolPopup.addItemWithTitle(title);
            final NSMenuItem item = this.protocolPopup.itemWithTitle(title);
            item.setRepresentedObject(protocol.getIdentifier());
            item.setImage(IconCache.iconNamed(protocol.icon(), 16));
        }
        final Protocol defaultProtocol
                = Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"));
        this.protocolPopup.selectItemWithTitle(defaultProtocol.getDescription());
    }

    public void protocolSelectionDidChange(final NSPopUpButton sender) {
        log.debug("protocolSelectionDidChange:" + sender);
        final Protocol protocol = Protocol.forName(protocolPopup.selectedItem().representedObject());
        portField.setIntValue(protocol.getDefaultPort());
        if(!protocol.isHostnameConfigurable()) {
            hostField.setStringValue(protocol.getDefaultHostname());
            hostField.setEnabled(false);
            portField.setEnabled(false);
            pathField.setEnabled(true);
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
            usernameField.cell().setPlaceholderString("");
            passField.cell().setPlaceholderString("");
        }
        hostField.setStringValue(protocol.getDefaultHostname());
        Rococoa.cast(usernameField.cell(), NSTextFieldCell.class).setPlaceholderString(
                protocol.getUsernamePlaceholder()
        );
        Rococoa.cast(passField.cell(), NSTextFieldCell.class).setPlaceholderString(
                protocol.getPasswordPlaceholder()
        );
        if(protocol.equals(Protocol.IDISK)) {
            Rococoa.cast(usernameField.cell(), NSTextFieldCell.class).setPlaceholderString(
                    Locale.localizedString("MobileMe Member Name", "IDisk")
            );
            final String member = Preferences.instance().getProperty("iToolsMember");
            if(StringUtils.isNotEmpty(member)) {
                // Account name configured in System Preferences
                usernameField.setStringValue(member);
                usernameField.setEnabled(false);
                pathField.setStringValue(Path.DELIMITER + member);
                pathField.setEnabled(false);
            }
        }
        connectmodePopup.setEnabled(protocol.equals(Protocol.FTP)
                || protocol.equals(Protocol.FTP_TLS));

        final boolean supportsCustomEncoding = protocol.equals(Protocol.FTP)
                || protocol.equals(Protocol.FTP_TLS) || protocol.equals(Protocol.SFTP);
        if(!supportsCustomEncoding) {
            encodingPopup.selectItemWithTitle(DEFAULT);
        }
        encodingPopup.setEnabled(supportsCustomEncoding);

        this.updateIdentity();
        this.updateURLLabel();

        this.reachable();
    }

    /**
     * Update Private Key selection
     */
    private void updateIdentity() {
        final Protocol protocol = Protocol.forName(protocolPopup.selectedItem().representedObject());
        pkCheckbox.setEnabled(protocol.equals(Protocol.SFTP));
        if(protocol.equals(Protocol.SFTP)) {
            if(StringUtils.isNotEmpty(hostField.stringValue())) {
                final OpenSshConfig.Host entry = OpenSshConfig.create().lookup(hostField.stringValue());
                if(null != entry.getIdentityFile()) {
                    if(pkCheckbox.state() == NSCell.NSOffState) {
                        // No previously manually selected key
                        pkLabel.setStringValue(LocalFactory.createLocal(
                                entry.getIdentityFile().getAbsolutePath()).getAbbreviatedPath());
                        pkCheckbox.setState(NSCell.NSOnState);
                    }
                }
                else {
                    pkCheckbox.setState(NSCell.NSOffState);
                    pkLabel.setStringValue(Locale.localizedString("No private key selected"));
                }
                if(StringUtils.isNotBlank(entry.getUser())) {
                    usernameField.setStringValue(entry.getUser());
                }
            }
        }
        else {
            pkCheckbox.setState(NSCell.NSOffState);
            pkLabel.setStringValue(Locale.localizedString("No private key selected"));
        }
    }

    private NSComboBox hostField;
    private ProxyController hostFieldModel = new HostFieldModel();

    public void setHostPopup(NSComboBox hostPopup) {
        this.hostField = hostPopup;
        this.hostField.setTarget(this.id());
        this.hostField.setAction(Foundation.selector("hostPopupSelectionDidChange:"));
        this.hostField.setUsesDataSource(true);
        this.hostField.setDataSource(hostFieldModel.id());
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("hostFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.hostField);
    }

    private static class HostFieldModel extends ProxyController implements NSComboBox.DataSource {
        public NSInteger numberOfItemsInComboBox(final NSComboBox sender) {
            return new NSInteger(HostCollection.defaultCollection().size());
        }

        public NSObject comboBox_objectValueForItemAtIndex(final NSComboBox sender, final NSInteger row) {
            return NSString.stringWithString(HostCollection.defaultCollection().get(row.intValue()).getNickname());
        }
    }

    @Action
    public void hostPopupSelectionDidChange(final NSControl sender) {
        String input = sender.stringValue();
        if(StringUtils.isBlank(input)) {
            return;
        }
        input = input.trim();
        // First look for equivalent bookmarks
        for(Host h : HostCollection.defaultCollection()) {
            if(h.getNickname().equals(input)) {
                this.hostChanged(h);
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
     * @param host
     */
    private void hostChanged(final Host host) {
        this.updateField(hostField, host.getHostname());
        protocolPopup.selectItemWithTitle(host.getProtocol().getDescription());
        this.updateField(portField, String.valueOf(host.getPort()));
        this.updateField(usernameField, host.getCredentials().getUsername());
        this.updateField(pathField, host.getDefaultPath());
        anonymousCheckbox.setState(host.getCredentials().isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
        this.anonymousCheckboxClicked(anonymousCheckbox);
        if(host.getCredentials().isPublicKeyAuthentication()) {
            pkCheckbox.setState(NSCell.NSOnState);
            pkLabel.setStringValue(host.getCredentials().getIdentity().toURL());
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

                @Override
                public void cleanup() {
                    alertIcon.setEnabled(!reachable);
                    alertIcon.setImage(reachable ? null : IconCache.iconNamed("alert.tiff"));
                }
            });
        }
        else {
            alertIcon.setImage(IconCache.iconNamed("alert.tiff"));
            alertIcon.setEnabled(false);
        }
    }

    @Outlet
    private NSButton alertIcon;

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setTarget(this.id());
        this.alertIcon.setAction(Foundation.selector("launchNetworkAssistant:"));
    }

    @Action
    public void launchNetworkAssistant(final NSButton sender) {
        Host.parse(urlLabel.stringValue()).diagnose();
    }

    @Outlet
    private NSTextField pathField;

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("pathInputDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                this.pathField);
    }

    public void pathInputDidEndEditing(final NSNotification sender) {
        this.updateURLLabel();
        if(StringUtils.isBlank(pathField.stringValue())) {
            return;
        }
        this.pathField.setStringValue(Path.normalize(pathField.stringValue(), false));
    }

    @Outlet
    private NSTextField portField;

    public void setPortField(NSTextField portField) {
        this.portField = portField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("portFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.portField);
    }

    public void portFieldTextDidChange(final NSNotification sender) {
        if(null == this.portField.stringValue() || this.portField.stringValue().equals("")) {
            final Protocol protocol = Protocol.forName(protocolPopup.selectedItem().representedObject());
            this.portField.setStringValue(String.valueOf(protocol.getDefaultPort()));
        }
        this.updateURLLabel();
        this.reachable();
    }

    @Outlet
    private NSTextField usernameField;

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
        this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("usernameFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.usernameField);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("usernameFieldTextDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                this.usernameField);
    }

    public void usernameFieldTextDidChange(final NSNotification sender) {
        this.updateURLLabel();
    }

    public void usernameFieldTextDidEndEditing(final NSNotification sender) {
        this.readPasswordFromKeychain();
    }

    @Outlet
    private NSTextField passField;

    public void setPassField(NSTextField passField) {
        this.passField = passField;
    }

    @Outlet
    private NSTextField pkLabel;

    public void setPkLabel(NSTextField pkLabel) {
        this.pkLabel = pkLabel;
        this.pkLabel.setStringValue(Locale.localizedString("No private key selected"));
        this.pkLabel.setTextColor(NSColor.disabledControlTextColor());
    }

    @Outlet
    private NSButton keychainCheckbox;

    public void setKeychainCheckbox(NSButton keychainCheckbox) {
        this.keychainCheckbox = keychainCheckbox;
        this.keychainCheckbox.setState(Preferences.instance().getBoolean("connection.login.useKeychain")
                        && Preferences.instance().getBoolean("connection.login.addKeychain") ? NSCell.NSOnState : NSCell.NSOffState);
        this.keychainCheckbox.setTarget(this.id());
        this.keychainCheckbox.setAction(Foundation.selector("keychainCheckboxClicked:"));
    }

    public void keychainCheckboxClicked(final NSButton sender) {
        final boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("connection.login.addKeychain", enabled);
    }

    @Outlet
    private NSButton anonymousCheckbox;

    public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
        this.anonymousCheckbox = anonymousCheckbox;
        this.anonymousCheckbox.setTarget(this.id());
        this.anonymousCheckbox.setAction(Foundation.selector("anonymousCheckboxClicked:"));
        this.anonymousCheckbox.setState(NSCell.NSOffState);
    }

    @Action
    public void anonymousCheckboxClicked(final NSButton sender) {
        if(sender.state() == NSCell.NSOnState) {
            this.usernameField.setEnabled(false);
            this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.anon.name"));
            this.passField.setEnabled(false);
            this.passField.setStringValue("");
        }
        if(sender.state() == NSCell.NSOffState) {
            this.usernameField.setEnabled(true);
            this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
            this.passField.setEnabled(true);
        }
        this.updateURLLabel();
    }

    @Outlet
    private NSButton pkCheckbox;

    public void setPkCheckbox(NSButton pkCheckbox) {
        this.pkCheckbox = pkCheckbox;
        this.pkCheckbox.setTarget(this.id());
        this.pkCheckbox.setAction(Foundation.selector("pkCheckboxSelectionDidChange:"));
        this.pkCheckbox.setState(NSCell.NSOffState);
    }

    private NSOpenPanel publicKeyPanel;

    @Action
    public void pkCheckboxSelectionDidChange(final NSButton sender) {
        log.debug("pkCheckboxSelectionDidChange");
        if(sender.state() == NSCell.NSOnState) {
            publicKeyPanel = NSOpenPanel.openPanel();
            publicKeyPanel.setCanChooseDirectories(false);
            publicKeyPanel.setCanChooseFiles(true);
            publicKeyPanel.setAllowsMultipleSelection(false);
            publicKeyPanel.setMessage(Locale.localizedString("Select the private key in PEM format", "Credentials"));
            publicKeyPanel.setPrompt(Locale.localizedString("Choose"));
            publicKeyPanel.beginSheetForDirectory(LocalFactory.createLocal("~/.ssh").getAbsolute(),
                    null, this.window(), this.id(),
                    Foundation.selector("pkSelectionPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            passField.setEnabled(true);
            pkCheckbox.setState(NSCell.NSOffState);
            pkLabel.setStringValue(Locale.localizedString("No private key selected"));
            pkLabel.setTextColor(NSColor.disabledControlTextColor());
        }
    }

    public void pkSelectionPanelDidEnd_returnCode_contextInfo(NSOpenPanel window, int returncode, ID contextInfo) {
        if(NSPanel.NSOKButton == returncode) {
            NSArray selected = window.filenames();
            final NSEnumerator enumerator = selected.objectEnumerator();
            NSObject next;
            while(null != (next = enumerator.nextObject())) {
                pkLabel.setStringValue(LocalFactory.createLocal(
                        Rococoa.cast(next, NSString.class).toString()).getAbbreviatedPath());
                pkLabel.setTextColor(NSColor.textColor());
            }
            passField.setEnabled(false);
        }
        if(NSPanel.NSCancelButton == returncode) {
            passField.setEnabled(true);
            pkCheckbox.setState(NSCell.NSOffState);
            pkLabel.setStringValue(Locale.localizedString("No private key selected"));
            pkLabel.setTextColor(NSColor.disabledControlTextColor());
        }
        publicKeyPanel = null;
    }

    @Outlet
    private NSTextField urlLabel;

    public void setUrlLabel(NSTextField urlLabel) {
        this.urlLabel = urlLabel;
        this.urlLabel.setAllowsEditingTextAttributes(true);
        this.urlLabel.setSelectable(true);
    }

    @Outlet
    private NSPopUpButton encodingPopup;

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemWithTitle(DEFAULT);
        this.encodingPopup.menu().addItem(NSMenuItem.separatorItem());
        this.encodingPopup.addItemsWithTitles(NSArray.arrayWithObjects(MainController.availableCharsets()));
        this.encodingPopup.selectItemWithTitle(DEFAULT);
    }

    @Outlet
    private NSPopUpButton connectmodePopup;

    private static final String CONNECTMODE_ACTIVE = Locale.localizedString("Active");
    private static final String CONNECTMODE_PASSIVE = Locale.localizedString("Passive");

    public void setConnectmodePopup(NSPopUpButton connectmodePopup) {
        this.connectmodePopup = connectmodePopup;
        this.connectmodePopup.removeAllItems();
        this.connectmodePopup.addItemWithTitle(DEFAULT);
        this.connectmodePopup.menu().addItem(NSMenuItem.separatorItem());
        this.connectmodePopup.addItemWithTitle(CONNECTMODE_ACTIVE);
        this.connectmodePopup.addItemWithTitle(CONNECTMODE_PASSIVE);
        this.connectmodePopup.selectItemWithTitle(DEFAULT);
    }

    @Outlet
    private NSButton toggleOptionsButton;

    public void setToggleOptionsButton(NSButton b) {
        this.toggleOptionsButton = b;
    }

    private static final Map<WindowController, ConnectionController> controllers
            = new HashMap<WindowController, ConnectionController>();

    public static ConnectionController instance(final WindowController parent) {
        if(!controllers.containsKey(parent)) {
            final ConnectionController c = new ConnectionController(parent) {
                @Override
                protected void invalidate() {
                    controllers.remove(parent);
                    super.invalidate();
                }
            };
            c.loadBundle();
            controllers.put(parent, c);
        }
        final ConnectionController c = controllers.get(parent);
        c.init();
        return c;
    }

    protected void init() {
        passField.setStringValue("");
        final boolean enabled = Preferences.instance().getBoolean("connection.login.useKeychain");
        keychainCheckbox.setEnabled(enabled);
        if(!enabled) {
            keychainCheckbox.setState(NSCell.NSOffState);
        }
    }

    @Override
    protected void invalidate() {
        hostField.setDelegate(null);
        hostField.setDataSource(null);
        super.invalidate();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * @param parent
     */
    private ConnectionController(final WindowController parent) {
        super(parent);
    }

    @Override
    protected String getBundleName() {
        return "Connection";
    }

    @Override
    public void awakeFromNib() {
        this.protocolSelectionDidChange(null);
        this.setState(toggleOptionsButton, Preferences.instance().getBoolean("connection.toggle.options"));
        super.awakeFromNib();
    }

    /**
     * Updating the password field with the actual password if any
     * is avaialble for this hostname
     */
    public void readPasswordFromKeychain() {
        if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
            if(StringUtils.isBlank(hostField.stringValue())) {
                return;
            }
            if(StringUtils.isBlank(portField.stringValue())) {
                return;
            }
            if(StringUtils.isBlank(usernameField.stringValue())) {
                return;
            }
            final Protocol protocol = Protocol.forName(protocolPopup.selectedItem().representedObject());
            this.updateField(this.passField, KeychainFactory.instance().getPassword(protocol.getScheme(),
                    Integer.parseInt(portField.stringValue()),
                    hostField.stringValue(), usernameField.stringValue()));
        }
    }

    /**
     */
    private void updateURLLabel() {
        if(StringUtils.isNotBlank(hostField.stringValue())) {
            final Protocol protocol = Protocol.forName(protocolPopup.selectedItem().representedObject());
            final String url = protocol.getScheme() + "://" + usernameField.stringValue()
                    + "@" + hostField.stringValue() + ":" + portField.stringValue()
                    + Path.normalize(pathField.stringValue());
            urlLabel.setStringValue(url);
        }
        else {
            urlLabel.setStringValue(hostField.stringValue());
        }
    }

    public void helpButtonClicked(final ID sender) {
        final Protocol protocol = Protocol.forName(protocolPopup.selectedItem().representedObject());
        NSWorkspace.sharedWorkspace().openURL(
                NSURL.URLWithString(Preferences.instance().getProperty("website.help")
                        + "/" + protocol.getIdentifier())
        );
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.window().endEditingFor(null);
            final Protocol protocol = Protocol.forName(protocolPopup.selectedItem().representedObject());
            Host host = new Host(
                    protocol,
                    hostField.stringValue(),
                    Integer.parseInt(portField.stringValue()),
                    pathField.stringValue());
            if(protocol.equals(Protocol.FTP) ||
                    protocol.equals(Protocol.FTP_TLS)) {
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
            credentials.setUseKeychain(keychainCheckbox.state() == NSCell.NSOnState);
            if(protocol.equals(Protocol.SFTP)) {
                if(pkCheckbox.state() == NSCell.NSOnState) {
                    credentials.setIdentity(LocalFactory.createLocal(pkLabel.stringValue()));
                }
            }
            if(encodingPopup.titleOfSelectedItem().equals(DEFAULT)) {
                host.setEncoding(null);
            }
            else {
                host.setEncoding(encodingPopup.titleOfSelectedItem());
            }
            ((BrowserController) parent).mount(host);
        }
        Preferences.instance().setProperty("connection.toggle.options", this.toggleOptionsButton.state());
    }
}
