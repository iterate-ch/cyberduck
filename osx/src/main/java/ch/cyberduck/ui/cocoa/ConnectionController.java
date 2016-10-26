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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.HyperlinkAttributedStringFactory;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSColor;
import ch.cyberduck.binding.application.NSComboBox;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSPanel;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.*;
import ch.cyberduck.core.diagnostics.ReachabilityFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.AbstractBackgroundAction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSSize;

public class ConnectionController extends SheetController {
    private static final Logger log = Logger.getLogger(ConnectionController.class);

    private final HostPasswordStore keychain
            = PasswordStoreFactory.get();

    private final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final Preferences preferences
            = PreferencesFactory.get();

    @Override
    public void invalidate() {
        hostField.setDelegate(null);
        hostField.setDataSource(null);
        notificationCenter.removeObserver(this.id());
        super.invalidate();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public ConnectionController(final WindowController parent) {
        super(parent);
        this.loadBundle();
    }

    @Override
    protected String getBundleName() {
        return "Connection";
    }

    @Override
    public void awakeFromNib() {
        this.protocolSelectionDidChange(null);
        this.setState(toggleOptionsButton, preferences.getBoolean("connection.toggle.options"));
        super.awakeFromNib();
    }

    @Override
    protected void beginSheet(final NSWindow window) {
        // Reset password input
        passField.setStringValue(StringUtils.EMPTY);
        super.beginSheet(window);
    }

    @Override
    public void setWindow(final NSWindow window) {
        window.setContentMinSize(window.frame().size);
        window.setContentMaxSize(new NSSize(600, window.frame().size.height.doubleValue()));
        super.setWindow(window);
    }

    @Outlet
    private NSPopUpButton protocolPopup;

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionDidChange:"));
        this.protocolPopup.removeAllItems();
        for(Protocol protocol : ProtocolFactory.getEnabledProtocols()) {
            final String title = protocol.getDescription();
            this.protocolPopup.addItemWithTitle(title);
            final NSMenuItem item = this.protocolPopup.itemWithTitle(title);
            item.setRepresentedObject(String.valueOf(protocol.hashCode()));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed(protocol.icon(), 16));
        }
        final Protocol defaultProtocol
                = ProtocolFactory.forName(preferences.getProperty("connection.protocol.default"));
        this.protocolPopup.selectItemAtIndex(
                protocolPopup.indexOfItemWithRepresentedObject(String.valueOf(defaultProtocol.hashCode()))
        );
    }

    public void protocolSelectionDidChange(final NSPopUpButton sender) {
        log.debug("protocolSelectionDidChange:" + sender);
        final Protocol protocol = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
        portField.setIntValue(protocol.getDefaultPort());
        portField.setEnabled(protocol.isPortConfigurable());
        if(!protocol.isHostnameConfigurable()) {
            hostField.setStringValue(protocol.getDefaultHostname());
            hostField.setEnabled(false);
            pathField.setEnabled(true);
        }
        else {
            if(!hostField.isEnabled()) {
                // Was previously configured with a static configuration
                hostField.setStringValue(protocol.getDefaultHostname());
            }
            if(!pathField.isEnabled()) {
                // Was previously configured with a static configuration
                pathField.setStringValue(StringUtils.EMPTY);
            }
            if(StringUtils.isNotBlank(protocol.getDefaultHostname())) {
                // Prefill with default hostname
                hostField.setStringValue(protocol.getDefaultHostname());
            }
            usernameField.setEnabled(true);
            hostField.setEnabled(true);
            pathField.setEnabled(true);
            usernameField.cell().setPlaceholderString(StringUtils.EMPTY);
            passField.cell().setPlaceholderString(StringUtils.EMPTY);
        }
        hostField.cell().setPlaceholderString(protocol.getDefaultHostname());
        usernameField.cell().setPlaceholderString(protocol.getUsernamePlaceholder());
        passField.cell().setPlaceholderString(protocol.getPasswordPlaceholder());
        usernameLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                StringUtils.isNotBlank(protocol.getUsernamePlaceholder()) ? String.format("%s:",
                        protocol.getUsernamePlaceholder()) : StringUtils.EMPTY,
                LABEL_ATTRIBUTES
        ));
        passwordLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                StringUtils.isNotBlank(protocol.getPasswordPlaceholder()) ? String.format("%s:",
                        protocol.getPasswordPlaceholder()) : StringUtils.EMPTY,
                LABEL_ATTRIBUTES
        ));
        connectmodePopup.setEnabled(protocol.getType() == Protocol.Type.ftp);
        if(!protocol.isEncodingConfigurable()) {
            encodingPopup.selectItemWithTitle(DEFAULT);
        }
        encodingPopup.setEnabled(protocol.isEncodingConfigurable());
        anonymousCheckbox.setEnabled(protocol.isAnonymousConfigurable());

        this.updateIdentity();
        this.updateURLLabel();
        this.readPasswordFromKeychain();
        this.reachable();
    }

    /**
     * Update Private Key selection
     */
    private void updateIdentity() {
        final Protocol protocol = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
        pkCheckbox.setEnabled(protocol.getType() == Protocol.Type.sftp);
        if(StringUtils.isNotEmpty(hostField.stringValue())) {
            final Credentials credentials = CredentialsConfiguratorFactory.get(protocol).configure(new Host(protocol, hostField.stringValue()));
            if(credentials.isPublicKeyAuthentication()) {
                // No previously manually selected key
                pkLabel.setStringValue(credentials.getIdentity().getAbbreviatedPath());
                pkCheckbox.setState(NSCell.NSOnState);
            }
            else {
                pkCheckbox.setState(NSCell.NSOffState);
                pkLabel.setStringValue(LocaleFactory.localizedString("No private key selected"));
            }
            if(StringUtils.isNotBlank(credentials.getUsername())) {
                usernameField.setStringValue(credentials.getUsername());
            }
        }
    }

    private NSComboBox hostField;
    private final ProxyController hostFieldModel = new HostFieldModel();

    public void setHostPopup(NSComboBox hostPopup) {
        this.hostField = hostPopup;
        this.hostField.setTarget(this.id());
        this.hostField.setAction(Foundation.selector("hostPopupSelectionDidChange:"));
        this.hostField.setUsesDataSource(true);
        this.hostField.setDataSource(hostFieldModel.id());
        notificationCenter.addObserver(this.id(),
                Foundation.selector("hostFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.hostField);
    }

    private static class HostFieldModel extends ProxyController implements NSComboBox.DataSource {
        @Override
        public NSInteger numberOfItemsInComboBox(final NSComboBox sender) {
            return new NSInteger(BookmarkCollection.defaultCollection().size());
        }

        @Override
        public NSObject comboBox_objectValueForItemAtIndex(final NSComboBox sender, final NSInteger row) {
            return NSString.stringWithString(
                    BookmarkNameProvider.toString(BookmarkCollection.defaultCollection().get(row.intValue()))
            );
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
        for(Host h : BookmarkCollection.defaultCollection()) {
            if(BookmarkNameProvider.toString(h).equals(input)) {
                this.hostChanged(h);
                this.updateURLLabel();
                this.readPasswordFromKeychain();
                this.reachable();
                break;
            }
        }
    }

    public void hostFieldTextDidChange(final NSNotification sender) {
        if(Scheme.isURL(hostField.stringValue())) {
            this.hostChanged(HostParser.parse(hostField.stringValue()));
        }
        this.updateURLLabel();
        this.readPasswordFromKeychain();
        this.reachable();
    }

    private void hostChanged(final Host host) {
        this.updateField(hostField, host.getHostname());
        protocolPopup.selectItemAtIndex(
                protocolPopup.indexOfItemWithRepresentedObject(String.valueOf(host.getProtocol().hashCode()))
        );
        this.updateField(portField, String.valueOf(host.getPort()));
        this.updateField(usernameField, host.getCredentials().getUsername());
        this.updateField(pathField, host.getDefaultPath());
        anonymousCheckbox.setState(host.getCredentials().isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
        this.anonymousCheckboxClicked(anonymousCheckbox);
        this.updateIdentity();
    }

    /**
     * Run the connection reachability test in the background
     */
    private void reachable() {
        final String hostname = hostField.stringValue();
        if(StringUtils.isNotBlank(hostname)) {
            this.background(new AbstractBackgroundAction<Boolean>() {
                boolean reachable = false;

                @Override
                public Boolean run() throws BackgroundException {
                    if(!preferences.getBoolean("connection.hostname.check")) {
                        return reachable = true;
                    }
                    return reachable = ReachabilityFactory.get().isReachable(new Host(ProtocolFactory.forName(
                            protocolPopup.selectedItem().representedObject()),
                            hostname));
                }

                @Override
                public void cleanup() {
                    alertIcon.setEnabled(!reachable);
                    alertIcon.setImage(reachable ? null : IconCacheFactory.<NSImage>get().iconNamed("alert.tiff"));
                }
            });
        }
        else {
            alertIcon.setImage(IconCacheFactory.<NSImage>get().iconNamed("alert.tiff"));
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
        ReachabilityFactory.get().diagnose(HostParser.parse(urlLabel.stringValue()));
    }

    @Outlet
    private NSTextField pathField;

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("pathInputDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                this.pathField);
    }

    public void pathInputDidEndEditing(final NSNotification sender) {
        this.updateURLLabel();
    }

    @Outlet
    private NSTextField portField;

    public void setPortField(NSTextField portField) {
        this.portField = portField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("portFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.portField);
    }

    public void portFieldTextDidChange(final NSNotification sender) {
        if(StringUtils.isBlank(this.portField.stringValue())) {
            final Protocol protocol = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
            this.portField.setIntValue(protocol.getDefaultPort());
        }
        this.updateURLLabel();
        this.reachable();
    }

    @Outlet
    private NSTextField usernameField;

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
        this.usernameField.setStringValue(preferences.getProperty("connection.login.name"));
        notificationCenter.addObserver(this.id(),
                Foundation.selector("usernameFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.usernameField);
        notificationCenter.addObserver(this.id(),
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
    private NSTextField usernameLabel;

    public void setUsernameLabel(NSTextField usernameLabel) {
        this.usernameLabel = usernameLabel;
    }

    @Outlet
    private NSTextField passwordLabel;

    public void setPasswordLabel(NSTextField passwordLabel) {
        this.passwordLabel = passwordLabel;
    }

    @Outlet
    private NSTextField pkLabel;

    public void setPkLabel(NSTextField pkLabel) {
        this.pkLabel = pkLabel;
        this.pkLabel.setStringValue(LocaleFactory.localizedString("No private key selected"));
        this.pkLabel.setTextColor(NSColor.disabledControlTextColor());
    }

    @Outlet
    private NSButton keychainCheckbox;

    public void setKeychainCheckbox(NSButton keychainCheckbox) {
        this.keychainCheckbox = keychainCheckbox;
        this.keychainCheckbox.setState(preferences.getBoolean("connection.login.useKeychain")
                && preferences.getBoolean("connection.login.addKeychain") ? NSCell.NSOnState : NSCell.NSOffState);
        this.keychainCheckbox.setTarget(this.id());
        this.keychainCheckbox.setAction(Foundation.selector("keychainCheckboxClicked:"));
    }

    public void keychainCheckboxClicked(final NSButton sender) {
        final boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("connection.login.addKeychain", enabled);
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
            this.usernameField.setStringValue(preferences.getProperty("connection.login.anon.name"));
            this.passField.setEnabled(false);
            this.passField.setStringValue(StringUtils.EMPTY);
        }
        if(sender.state() == NSCell.NSOffState) {
            this.usernameField.setEnabled(true);
            this.usernameField.setStringValue(preferences.getProperty("connection.login.name"));
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
            publicKeyPanel.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
            publicKeyPanel.setPrompt(LocaleFactory.localizedString("Choose"));
            publicKeyPanel.beginSheetForDirectory(LocalFactory.get("~/.ssh").getAbsolute(),
                    null, this.window(), this.id(),
                    Foundation.selector("pkSelectionPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            passField.setEnabled(true);
            pkCheckbox.setState(NSCell.NSOffState);
            pkLabel.setStringValue(LocaleFactory.localizedString("No private key selected"));
            pkLabel.setTextColor(NSColor.disabledControlTextColor());
        }
    }

    public void pkSelectionPanelDidEnd_returnCode_contextInfo(NSOpenPanel window, int returncode, ID contextInfo) {
        if(NSPanel.NSOKButton == returncode) {
            final NSObject selected = window.filenames().lastObject();
            if(selected != null) {
                pkLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                        LocalFactory.get(selected.toString()).getAbbreviatedPath(), TRUNCATE_MIDDLE_ATTRIBUTES));
                pkLabel.setTextColor(NSColor.textColor());
            }
            passField.setEnabled(false);
        }
        if(NSPanel.NSCancelButton == returncode) {
            passField.setEnabled(true);
            pkCheckbox.setState(NSCell.NSOffState);
            pkLabel.setStringValue(LocaleFactory.localizedString("No private key selected"));
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
        this.encodingPopup.addItemsWithTitles(NSArray.arrayWithObjects(new DefaultCharsetProvider().availableCharsets()));
        this.encodingPopup.selectItemWithTitle(DEFAULT);
    }

    @Outlet
    private NSPopUpButton connectmodePopup;

    public void setConnectmodePopup(NSPopUpButton connectmodePopup) {
        this.connectmodePopup = connectmodePopup;
        this.connectmodePopup.removeAllItems();
        for(FTPConnectMode m : FTPConnectMode.values()) {
            this.connectmodePopup.addItemWithTitle(m.toString());
            this.connectmodePopup.lastItem().setRepresentedObject(m.name());
            if(m.equals(FTPConnectMode.unknown)) {
                this.connectmodePopup.selectItem(this.connectmodePopup.lastItem());
                this.connectmodePopup.menu().addItem(NSMenuItem.separatorItem());
            }
        }
    }

    @Outlet
    private NSButton toggleOptionsButton;

    public void setToggleOptionsButton(NSButton b) {
        this.toggleOptionsButton = b;
    }

    /**
     * Updating the password field with the actual password if any
     * is available for this hostname
     */
    public void readPasswordFromKeychain() {
        if(preferences.getBoolean("connection.login.useKeychain")) {
            if(StringUtils.isBlank(hostField.stringValue())) {
                return;
            }
            if(StringUtils.isBlank(portField.stringValue())) {
                return;
            }
            if(StringUtils.isBlank(usernameField.stringValue())) {
                return;
            }
            final Protocol protocol = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
            final String password = keychain.getPassword(protocol.getScheme(),
                    NumberUtils.toInt(portField.stringValue(), -1),
                    hostField.stringValue(), usernameField.stringValue());
            if(StringUtils.isNotBlank(password)) {
                this.updateField(passField, password);
            }
        }
    }

    private void updateURLLabel() {
        if(StringUtils.isNotBlank(hostField.stringValue())) {
            final Protocol protocol = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
            final String url = String.format("%s://%s@%s:%d%s",
                    protocol.getScheme(),
                    usernameField.stringValue(),
                    hostField.stringValue(),
                    NumberUtils.toInt(portField.stringValue(), -1),
                    PathNormalizer.normalize(pathField.stringValue()));
            urlLabel.setAttributedStringValue(HyperlinkAttributedStringFactory.create(url));
        }
        else {
            urlLabel.setStringValue(StringUtils.EMPTY);
        }
    }

    public void helpButtonClicked(final ID sender) {
        new DefaultProviderHelpService().help(
                ProtocolFactory.forName(protocolPopup.selectedItem().representedObject())
        );
    }

    @Override
    protected boolean validateInput() {
        if(StringUtils.isBlank(hostField.stringValue())) {
            return false;
        }
        if(StringUtils.isBlank(usernameField.stringValue())) {
            return false;
        }
        return true;
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.window().endEditingFor(null);
            final Protocol protocol = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
            final Host host = new Host(
                    protocol,
                    hostField.stringValue(),
                    NumberUtils.toInt(portField.stringValue(), -1),
                    pathField.stringValue());
            if(protocol.getType() == Protocol.Type.ftp) {
                host.setFTPConnectMode(FTPConnectMode.valueOf(connectmodePopup.selectedItem().representedObject()));
            }
            final Credentials credentials = host.getCredentials();
            credentials.setUsername(usernameField.stringValue());
            credentials.setPassword(passField.stringValue());
            credentials.setSaved(keychainCheckbox.state() == NSCell.NSOnState);
            if(protocol.getScheme().equals(Scheme.sftp)) {
                if(pkCheckbox.state() == NSCell.NSOnState) {
                    credentials.setIdentity(LocalFactory.get(pkLabel.stringValue()));
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
        preferences.setProperty("connection.toggle.options", this.toggleOptionsButton.state());
    }
}
